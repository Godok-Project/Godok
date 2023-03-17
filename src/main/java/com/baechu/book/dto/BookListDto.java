package com.baechu.book.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.baechu.book.entity.Book;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BookListDto {

	private int page;
	private long totalCount;
	private List<BookDto> books = new ArrayList<>();

	public BookListDto(int page, long totalCount, List<Book> entities) {
		this.page = page;
		this.totalCount = totalCount;
		this.books = entities.stream().map(BookDto::new).collect(Collectors.toList());
	}
}
