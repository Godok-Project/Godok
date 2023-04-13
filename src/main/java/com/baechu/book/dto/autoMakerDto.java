package com.baechu.book.dto;

import org.springframework.data.elasticsearch.annotations.Document;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Document(indexName = "book")
public class autoMakerDto {
	private String title;

	public autoMakerDto(String title) {
		this.title = title;
	}
}
