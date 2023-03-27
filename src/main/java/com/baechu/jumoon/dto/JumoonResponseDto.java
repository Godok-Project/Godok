package com.baechu.jumoon.dto;

import com.baechu.book.entity.Book;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class JumoonResponseDto {


	private long id;
	private String title;
	private Integer quantity;

	@Builder
	public JumoonResponseDto(long id, Book book, Integer quantity) {
		this.id = id;
		this.title = book.getTitle();
		this.quantity = quantity;
	}

}
