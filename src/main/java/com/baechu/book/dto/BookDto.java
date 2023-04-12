package com.baechu.book.dto;

import org.springframework.data.elasticsearch.annotations.Document;

import com.baechu.book.entity.Book;
import com.querydsl.core.annotations.QueryProjection;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Document(indexName = "book")
public class BookDto {
	private long id;
	private String image;
	private Integer price;
	private String author;
	private String title;
	private String publish;
	private Integer star;
	private Integer year;
	private Integer month;
	private Double score;
	private Long inventory;
	private int out_of_print;

	@Builder
	public BookDto(Book book) {
		this.id = book.getId();
		this.image = book.getImage();
		this.price = book.getPrice();
		this.author = book.getAuthor();
		this.title = book.getTitle();
		this.publish = book.getPublish();
		this.star = book.getStar();
		this.year = book.getYear();
		this.month = book.getMonth();
		this.inventory=book.getInventory();
		this.out_of_print = book.getOutOfPrint();
	}

	@QueryProjection
	public BookDto(long id, String image, Integer price, String author, String title, String publish, Integer star,
		Integer year, Integer month, Double score, Long inventory, int outOfPrint) {
		this.id = id;
		this.image = image;
		this.price = price;
		this.author = author;
		this.title = title;
		this.publish = publish;
		this.star = star;
		this.year = year;
		this.month = month;
		this.score = score;
		this.inventory = inventory;
		this.out_of_print = outOfPrint;
	}
}
