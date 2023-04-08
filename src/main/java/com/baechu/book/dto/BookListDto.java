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

	private Long cursor;
	private List<BookDto> books = new ArrayList<>();
	private String searchAfterSort;
	private Long searchAfterId;
	public BookListDto(List<Book> entities, Long cursor) {
		this.cursor = cursor;
		this.books = entities.stream().map(BookDto::new).collect(Collectors.toList());
	}

	public BookListDto(List<BookDto> entities, String searchAfterSort, Long searchAfterId) {
		this.searchAfterSort = searchAfterSort;
		this.searchAfterId =  searchAfterId;
		this.books = entities;
	}
}
