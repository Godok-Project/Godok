package com.baechu.book.dto;

import com.querydsl.core.annotations.QueryProjection;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CursorBookDto {
	private long id;
	private Integer price;
	private Double score;
	private String title;

	@QueryProjection
	public CursorBookDto(long id, Integer price, Double score, String title) {
		this.id = id;
		this.price = price;
		this.score = score;
		this.title = title;
	}
}
