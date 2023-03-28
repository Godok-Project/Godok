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

	private String jumoonat;

	@Builder
	public JumoonResponseDto(long id, Book book, Integer quantity, String jumoonat) {
		this.id = id;
		this.title = book.getTitle();
		this.quantity = quantity;
		this.jumoonat = jumoonat;
	}

}
