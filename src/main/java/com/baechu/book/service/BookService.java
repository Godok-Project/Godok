package com.baechu.book.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baechu.book.dto.BookDto;
import com.baechu.book.dto.BookListDto;
import com.baechu.book.dto.BookRankDto;
import com.baechu.book.dto.FilterDto;
import com.baechu.book.dto.autoMakerDto;
import com.baechu.book.entity.Book;
import com.baechu.book.repository.BookDSLRepository;
import com.baechu.book.repository.BookRepository;
import com.baechu.book.repository.ElasticRepository;
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

	/**
	 *	 책 상세 정보
	 */

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
		info.put("inventory", book.getInventory());
		info.put("out_of_print", book.getOutOfPrint());

		return info;
	}

	/**
	 * 키워드 검색
	 */

	// ES 검색
	@Transactional(readOnly = true)
	@CircuitBreaker(name = "ElasticError", fallbackMethod = "keywordSearchBySql")
	public BookListDto keywordSearchByElastic(FilterDto filter) {
		SearchHits<BookDto> search = elasticRepository.keywordSearchByElastic(filter);
		BookListDto result = resultToDto(search, filter);
		return result;
	}

	// Cursor 기반 페이징
	@Transactional(readOnly = true)
	public BookListDto keywordSearchBySql(FilterDto filter, Throwable t) {
		try {
			log.warn("keyword Elastic Down : " + t.getMessage());
			List<BookDto> books = bookDSLRepository.keywordSearchByCursor(filter);
			List<Object> cursors = getCursor(books, filter.getTotalRow(), filter.getSort());
			return new BookListDto(books, (String)cursors.get(0), (Long)cursors.get(1), filter.getPage(), false);
		} catch (Exception e) {
			log.warn("keyword Mysql SQLException : " + e.getMessage());
			return new BookListDto(new ArrayList<>(), null, null, null, false);
		}
	}

	/**
	 * 필터 검색
	 */

	// ES 검색
	@Transactional(readOnly = true)
	@CircuitBreaker(name = "ElasticError", fallbackMethod = "filterSearchBySql")
	public BookListDto filterSearchByElastic(FilterDto filter) {
		List<Object> searchAfter = initSearchAfter(filter.getSearchAfterSort(), filter.getSearchAfterId());
		SearchHits<BookDto> search = elasticRepository.filterSearchByElastic(filter, searchAfter);
		BookListDto result = resultToDto(search, filter);
		return result;
	}

	// Cursor 기반 페이징
	@Transactional(readOnly = true)
	public BookListDto filterSearchBySql(FilterDto filter, Throwable t) {
		try {
			log.warn("filter Elastic Down : " + t.getMessage());
			List<BookDto> books = bookDSLRepository.filterSearchByCursor(filter);
			List<Object> cursors = getCursor(books, filter.getTotalRow(), filter.getSort());
			return new BookListDto(books, (String)cursors.get(0), (Long)cursors.get(1), filter.getPage(), false);
		} catch (Exception e) {
			log.warn("filter Mysql SQLException : " + e.getMessage());
			return new BookListDto(new ArrayList<>(), null, null, null, false);
		}
	}

	/**
	 *  자동 완성
	 */

	public List<String> autoMaker(String query) {
		SearchHits<autoMakerDto> searchHits = elasticRepository.autoMaker(query);
		return searchHits.getSearchHits()
			.stream()
			.map(i -> i.getContent().getTitle())
			.collect(Collectors.toList());
	}

	/**
	 *	메인 페이지 책 리스팅
	 */

	@Transactional
	@CircuitBreaker(name = "RedisError", fallbackMethod = "NonRedisBookList")
	public List<BookRankDto> bookList() {

		List<BookRankDto> bookList = new ArrayList<>();
		ValueOperations<String, List<String>> values = redisTemplate.opsForValue();

		int rankcnt = 0;
		int savebooksold =-99;

		if (values.get("rank") == null) {

			//일단 랭킹을 만들어 달라고 하자
			//오래 걸릴 수 도 있으닌까 일단 아무거내 내뱉고 다음에 얻어가자
			//이걸 쓴다는건 배치가 터졌거나 redis 서버가 어떠한 이유로 초기화 되었단 뜻
			try {
				summonRank();
			}catch (Exception e){
			}


			long[] rbs = {2415062,387099,886063,1820350,1957841,1984839,1984355,21504};

			for (int i = 0; i < 8; i++) {
				Optional<Book> book = bookRepository.findById(rbs[i]);
				if (book.isPresent()) {
					bookList.add(new BookRankDto(book.get(), 0,"추천 도서"));
				} else
					i--;
			}
		} else {
			List<String> topbooks = values.get("rank");
			for (String i : topbooks) {
				Long bookid = Long.valueOf(i.split(",")[0]);
				Integer booksold = Integer.valueOf(i.split(",")[1]);

				Optional<Book> book = bookRepository.findById(bookid);
				if(booksold==0){
					bookList.add(new BookRankDto(book.get(),booksold,"추천 도서"));

				}else{
					if (savebooksold!=booksold){
						rankcnt++;
					}
					bookList.add(new BookRankDto(book.get(),booksold,String.valueOf(rankcnt)+" 등"));
					savebooksold =booksold;
				}
			}
		}

		return bookList;
	}

	@Transactional
	public List<BookRankDto> NonRedisBookList(Throwable t) {

		try {
			log.warn("Redis Rank Down : " + t.getMessage());
			List<BookRankDto> bookList = new ArrayList<>();
			long[] rbs = {2415062,387099,886063,1820350,1957841,1984839,1984355,21504};

			for (int i = 0; i < 8; i++) {
				Optional<Book> book = bookRepository.findById(rbs[i]);
				if (book.isPresent()) {
					bookList.add(new BookRankDto(book.get(), 0,"추천 도서"));
				} else
					i--;
			}
			return bookList;

		} catch (Exception e) {
			log.warn("Redis Connection SQLException : " + e.getMessage());
			return null;
		}
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
			starttime = LocalDateTime.of(ytd.getYear(),ytd.getMonth(), ytd.getDayOfMonth(), 2, 0);
			endtime = LocalDateTime.of(now.getYear(),now.getMonth(),now.getDayOfMonth(), 2, 0);
		}else {
			LocalDateTime yytd = now.minusDays(2);
			LocalDateTime ytd = now.minusDays(1);
			starttime = LocalDateTime.of(yytd.getYear(),yytd.getMonth(), yytd.getDayOfMonth(), 2, 0);
			endtime = LocalDateTime.of(ytd.getYear(),ytd.getMonth(), ytd.getDayOfMonth(), 2, 0);
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

		//추천 책 채워주기
		if (bookidkeys.size()<8){
			long[] rbs = {2415062,387099,886063,1820350,1957841,1984839,1984355,21504};
			int cnt = 0;

			while (bookidkeys.size()<8){
				bookidkeys.add(rbs[cnt]);
				soldbooks.put(rbs[cnt],0);
				cnt++;
			}
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

	/**
	 * - ES의 SearchAfter 값을 결정하는 메서드. (SearchAfter는 mysql에서의 Cursor와 같은 개념이다.)
	 * 	- initSearchAfter : 요청받은 searchAfter를 list에 입력하는 메서드
	 * 	- resultToDto : 검색 결과를 Dto로 변환하는 메서드
	 * 		- setSearchAfter : 검색 결과에 따라 필요한 searchAfter를 반환하는 메서드
	 */
	private List<Object> initSearchAfter(String searchAfterSort, Long searchAfterId) {
		List<Object> searchAfter = new ArrayList<>();

		if (searchAfterSort == null || searchAfterId == null)
			return null;

		searchAfter.add(searchAfterSort);
		searchAfter.add(searchAfterId);
		return searchAfter;
	}

	private BookListDto resultToDto(SearchHits<BookDto> search, FilterDto filter) {
		List<SearchHit<BookDto>> searchHits = search.getSearchHits();
		System.out.println(search.getTotalHits());
		List<BookDto> bookDtoList = searchHits.stream().map(hit -> hit.getContent()).collect(Collectors.toList());

		List<Object> searchAfter = setSearchAfter(searchHits, filter);
		String searchAfterSort = String.valueOf(searchAfter.get(0));
		Long searchAfterId = Long.parseLong(String.valueOf(searchAfter.get(1)));

		return new BookListDto(bookDtoList, searchAfterSort, searchAfterId, filter.getPage(), true);
	}

	private List<Object> setSearchAfter(List<SearchHit<BookDto>> searchHits, FilterDto filter) {
		List<Object> lists = new ArrayList<>();
		if (searchHits.size() == 0) {		// 검색 결과가 없는 경우
			lists.add("-1");
			lists.add("-1");
			return lists;
		} else if (searchHits.size() < filter.getTotalRow()) {		// 검색 결과가 총 개수보다 작은 경우
			lists.add("0");
			lists.add("-1");
			return lists;
		}
		return searchHits.get(searchHits.size() - 1).getSortValues();
	}

	/**
	 *  테스트 용 like 쿼리
	 */

	@Transactional(readOnly = true)
	public void filterSearchByLike(FilterDto filter) {
		bookDSLRepository.filterSearchByLike(filter);
	}
}