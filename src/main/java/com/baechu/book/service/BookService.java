package com.baechu.book.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baechu.book.dto.BookListDto;
import com.baechu.book.dto.CursorBookDto;
import com.baechu.book.dto.FilterDto;
import com.baechu.book.entity.Book;
import com.baechu.book.repository.BookDSLRepository;
import com.baechu.book.repository.BookRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookService {
	private final BookRepository bookRepository;
	private final BookDSLRepository bookDSLRepository;
	private final RedisTemplate redisTemplate;

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

		String bookKey = "b"+bookid;
		ValueOperations<String, String> values = redisTemplate.opsForValue();
		if (values.get(bookKey)==null){
			info.put("inventory", book.getInventory());
		}else {
			info.put("inventory", values.get(bookKey).split(",")[1]);
		}
		return info;
	}

	// Cursor 기반 페이징
	@Transactional(readOnly = true)
	public BookListDto searchByCursor(FilterDto filter) {
		CursorBookDto lastBook = getLastBook(filter);
		List<Book> books = bookDSLRepository.searchByCursor(filter, lastBook);
		Long cursor = getCursor(books, filter.getTotalRow());
		return new BookListDto(books, cursor);
	}

	@Transactional(readOnly = true)
	public List<Book> bookList() {

		List<Book> bookList = new ArrayList<>();
		ValueOperations<String, List<Integer>> values = redisTemplate.opsForValue();

		if (values.get("rank")==null){
			Long random;
			Random r = new Random();

			for (int i = 0; i < 8; i++) {
				random = (long)r.nextInt(4000000);
				Optional<Book> book = bookRepository.findById(random);
				if (book.isPresent()) {
					bookList.add(book.get());
				} else
					i--;
			}
		}else {
			List<Integer> topbooks = values.get("rank");
			for (int i : topbooks){
				Optional<Book> book = bookRepository.findById((long)i);
				bookList.add(book.get());
			}
		}

		return bookList;
	}

	private CursorBookDto getLastBook(FilterDto filter) {
		return bookDSLRepository.findScore(filter.getCursor(), filter.getQuery());
	}

	private Long getCursor(List<Book> books, Integer totalRow) {
		if (books.isEmpty()) {
			return 0L;
		} else if (books.size() < totalRow)
			return -1L;
		return books.get(books.size() - 1).getId();
	}
}