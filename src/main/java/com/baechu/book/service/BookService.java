package com.baechu.book.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.baechu.book.dto.BookListDto;
import com.baechu.book.entity.Book;
import com.baechu.book.repository.BookRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookService {
	private final BookRepository bookRepository;

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
		info.put("inventory", 7);

		return info;
	}

	public BookListDto searchByWord(String query, Integer page, Integer totalRow) {
		if (query.isEmpty()) {
			return new BookListDto();
		}
		PageRequest pageRequest = PageRequest.of(page, totalRow);
		Page<Book> bookPages = bookRepository.findBooksByWord(query, pageRequest);
		long totalCount = bookPages.getTotalElements();
		List<Book> books = bookPages.getContent();

		System.out.println("page = " + page);
		System.out.println("totalCount = " + totalCount);

		return new BookListDto(page, totalCount, books);
	}
}