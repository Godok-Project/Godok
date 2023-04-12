package com.baechu.book.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baechu.batch.CycleConfig;
import com.baechu.batch.NoRankCycle;
import com.baechu.book.dto.BookDto;
import com.baechu.book.dto.BookListDto;
import com.baechu.book.dto.BookRankDto;
import com.baechu.book.dto.CursorBookDto;
import com.baechu.book.dto.FilterDto;
import com.baechu.book.entity.Book;
import com.baechu.book.repository.BookDSLRepository;
import com.baechu.book.repository.BookRepository;
import com.baechu.book.repository.ElasticRepository;
import com.baechu.common.exception.CustomException;
import com.baechu.common.exception.ErrorCode;
import com.baechu.jumoon.entity.Jumoon;
import com.baechu.jumoon.repository.JumoonRepository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookService {
	private final BookRepository bookRepository;
	private final BookDSLRepository bookDSLRepository;
	private final RedisTemplate redisTemplate;
	private final ElasticRepository elasticRepository;
	private final JumoonRepository jumoonRepository;



	@Transactional(readOnly = true)
	public Map<String, Object> bookdetail(Long bookid) {
		Map<String, Object> info = new HashMap<>();
		Book book = bookRepository.findById(bookid).orElseThrow(
			() -> new IllegalArgumentException("해당 번호의 책 없음")
		);
		info.put("bookid", bookid);
		info.put("title", book.getTitle());
		info.put("image", book.getImage());
		info.put("price", book.getPrice());
		info.put("author", book.getAuthor());
		info.put("publish", book.getPublish());
		String birth = book.getYear() + "년 " + book.getMonth() + "월";
		info.put("birth", birth);

		String bookKey = "b" + bookid;
		ValueOperations<String, String> values = redisTemplate.opsForValue();
		if (values.get(bookKey) == null) {
			info.put("inventory", book.getInventory());
		} else {
			info.put("inventory", values.get(bookKey).split(",")[1]);
		}
		return info;
	}

	// Cursor 기반 페이징
	@Transactional(readOnly = true)
	public BookListDto searchByCursor(FilterDto filter, Throwable t) {
		try {
			log.info("Elastic down : " + t.getMessage());
			List<BookDto> books = bookDSLRepository.searchByCursor(filter);
			List<Object> cursors = getCursor(books, filter.getTotalRow(), filter.getSort());
			return new BookListDto(books, (String)cursors.get(0), (Long)cursors.get(1), filter.getPage(), false);
		} catch (Exception e) {
			log.warn("Mysql SQLException 발생");
			return new BookListDto(new ArrayList<>(), null, null, null, false);
		}
	}

	// ES 검색
	@Transactional(readOnly = true)
	@CircuitBreaker(name = "ElasticError", fallbackMethod = "searchByCursor")
	public BookListDto afterSearchByES(FilterDto filter) {
		BookListDto books = elasticRepository.searchByEsAfter(filter);
		return books;
	}


	@Transactional
	public List<BookRankDto> bookList() {

		List<BookRankDto> bookList = new ArrayList<>();
		ValueOperations<String, List<String>> values = redisTemplate.opsForValue();

		if (values.get("rank") == null) {

			//일단 랭킹을 만들어 달라고 하자
			//오래 걸릴 수 도 있으닌까 일단 아무거내 내뱉고 다음에 얻어가자
			//이걸 쓴다는건 배치가 터졌거나 redis 서버가 어떠한 이유로 초기화 되었단 뜻
			try {
				summonRank();
			}catch (Exception e){

			}


			Long random;
			Random r = new Random();

			for (int i = 0; i < 8; i++) {
				random = (long)r.nextInt(4000000);
				Optional<Book> book = bookRepository.findById(random);
				if (book.isPresent()) {
					bookList.add(new BookRankDto(book.get(), 0));
				} else
					i--;
			}
		} else {
			List<String> topbooks = values.get("rank");
			for (String i : topbooks) {
				Long bookid = Long.valueOf(i.split(",")[0]);
				Integer booksold = Integer.valueOf(i.split(",")[1]);

				Optional<Book> book = bookRepository.findById(bookid);
				bookList.add(new BookRankDto(book.get(),booksold));
			}
		}

		return bookList;
	}

	//트랜잭션 어노테이션 안에 배치를 사용할 수 없다.
	private void summonRank(){

		List<Long> bookidkeys = new ArrayList<>();
		List<Jumoon> jumoons = new ArrayList<>();

		LocalDateTime now = LocalDateTime.now();

		LocalDateTime starttime;
		LocalDateTime endtime;

		if (now.getHour()>=2){
			LocalDateTime ytd = now.minusDays(1);
			starttime = LocalDateTime.of(ytd.getYear(),ytd.getMonth(), ytd.getDayOfMonth(), ytd.getHour(), 0);
			endtime = LocalDateTime.of(now.getYear(),now.getMonth(),now.getDayOfMonth(), now.getHour(), 0);
		}else {
			LocalDateTime yytd = now.minusDays(2);
			LocalDateTime ytd = now.minusDays(1);
			starttime = LocalDateTime.of(yytd.getYear(),yytd.getMonth(), yytd.getDayOfMonth(), yytd.getHour(), 0);
			endtime = LocalDateTime.of(ytd.getYear(),ytd.getMonth(), ytd.getDayOfMonth(), ytd.getHour(), 0);
		}
		jumoons = jumoonRepository.findAllByJumoonatBetween(starttime,endtime);

		//어떤 책이 얼마나 팔렸는지 map으로 저장
		Map<Long, Integer> soldbooks = new HashMap<>();
		for(Jumoon i : jumoons){
			Long bookid = i.getBook().getId();
			Integer quantity = i.getQuantity();

			if (soldbooks.containsKey(bookid)){
				soldbooks.put(bookid,(soldbooks.get(bookid)+quantity));
			}else {
				soldbooks.put(bookid,quantity);
			}
		}

		//map의 value 순서대로 정렬하기
		bookidkeys = new ArrayList<>(soldbooks.keySet());
		Collections.sort(bookidkeys, ((o1, o2) -> (soldbooks.get(o2).compareTo(soldbooks.get(o1)))));

		//어제 주문된 책 종류가 8개 미만인 경우 100,101,102... 순으로 책을 채워준다.
		Long cnt = 100L;
		while (bookidkeys.size()<8){
			bookidkeys.add(cnt);
			soldbooks.put(cnt,0);
			cnt++;
		}

		//이제 랭크 순서대로 넣어준다. "rank" : {"1,9", "2,8"....} 책id와 판매량은 쉼표로 구분하고 각각 리스트의 원소로 넣자

		List<String> bookrankAndsold = new ArrayList<>();
		for (int i = 0; i < 8; i++) {
			String rankvalue = bookidkeys.get(i)+","+soldbooks.get(bookidkeys.get(i));
			bookrankAndsold.add(rankvalue);
		}

		//rankvalue를 redis에 저장시키기
		ValueOperations<String, List<String>> values = redisTemplate.opsForValue();
		//레디스 초기화 이후
		redisTemplate.getConnectionFactory().getConnection().flushAll();
		//랭크값 넣어주기
		values.set("rank",bookrankAndsold);


	}

	/**
	 * - Mysql 커서 선택 메서드
	 * 	- getCursor : 가져온 책 리스트의 수량에 따라 필요한 값 배치 (페이지 버튼 존재 확인용)
	 * 		- search.html 332줄에 페이지 버튼 로직 있음
	 * 	- selectSortCursor : 검색 결과가 필요양만큼 있을 때 다음 페이지를 위한 커서를 선택하는 메서드
	 */
	private List<Object> getCursor(List<BookDto> books, Integer totalRow, Integer sort) {
		List<Object> cursors = new ArrayList<>();
		if (books.isEmpty()) {	// 검색 결과가 없는 경우
			cursors.add("-1");
			cursors.add(-1L);
		} else if (books.size() < totalRow) {	// 검색결과가 필요한 양보다 적은 경우 ()
			cursors.add("0");
			cursors.add(-1L);
		} else {
			BookDto lastBook = books.get(books.size() - 1);		// 마지막 책 데이터
			cursors.add(selectSortCursor(lastBook, sort));        // 정렬에 맞는 cursor 추가
			cursors.add(lastBook.getId());        // id cursor 추가
		}
		return cursors;
	}

	private String selectSortCursor(BookDto lastBook, Integer sort) {
		if (sort == null)	// null이면 0과같이 처리
			return String.valueOf(lastBook.getScore());
		if (sort == 0) {    // sort == 0 이면, score 반환
			return String.valueOf(lastBook.getScore());
		} else if (sort == 1) {  // sort == 1 이면, title 반환
			return lastBook.getTitle();
		} else {    // sort == 2 or 3 이면, price 반환
			return String.valueOf(lastBook.getPrice());
		}
	}
}