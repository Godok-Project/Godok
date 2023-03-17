package com.baechu.book.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.baechu.book.dto.BookDto;
import com.baechu.book.entity.Book;
import com.baechu.book.repository.BookRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookService {
	private final BookRepository bookRepository;


	public Map<String, Object> bookdetail(Long bookid){
		Map<String, Object> info = new HashMap<>();
		Book book = bookRepository.findById(bookid).orElseThrow(
			()-> new IllegalArgumentException("해당 번호의 책 없음")
		);
		info.put("bookid",bookid);
		info.put("title", book.getTitle());
		info.put("image", book.getImage());
		info.put("price", book.getPrice());
		info.put("author", book.getAuthor());
		info.put("publish", book.getPublish());
		String birth = book.getYear()+"년 "+book.getMonth()+"월";
		info.put("birth", birth);
		info.put("inventory", 7);

		return info;
	}

	public List<BookDto> searchByWord(String query) {
		List<BookDto> dtos = new ArrayList<>();
		if (!query.isEmpty()) {
			List<Book> books = bookRepository.findBooksByWord(query);
			dtos = books.stream().map(BookDto::new).collect(Collectors.toList());
		}
		return dtos;
	}
}