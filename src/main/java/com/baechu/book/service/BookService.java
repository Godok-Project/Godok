package com.baechu.book.service;

import java.util.ArrayList;
import java.util.List;
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

	public List<BookDto> searchByWord(String query) {
		List<BookDto> dtos = new ArrayList<>();
		if (!query.isEmpty()) {
			List<Book> books = bookRepository.findBooksByWord(query);
			dtos = books.stream().map(BookDto::new).collect(Collectors.toList());
		}
		return dtos;
	}
}