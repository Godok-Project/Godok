package com.baechu.book.dto;

import com.baechu.book.entity.Book;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
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
	}

}
