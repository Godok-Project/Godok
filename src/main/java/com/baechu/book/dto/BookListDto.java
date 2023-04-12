package com.baechu.book.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BookListDto {

	private Integer page;
	private List<BookDto> books = new ArrayList<>();
	private String searchAfterSort;
	private Long searchAfterId;
	private Boolean status;

	public BookListDto(List<BookDto> entities, String searchAfterSort, Long searchAfterId, Integer page, Boolean status) {
		this.searchAfterSort = searchAfterSort;
		this.searchAfterId = searchAfterId;
		this.books = entities;
		this.page = page;
		this.status = status;
	}
}
