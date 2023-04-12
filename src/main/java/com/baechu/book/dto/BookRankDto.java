package com.baechu.book.dto;

import com.baechu.book.entity.Book;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BookRankDto {

	private long id;
	private String image;
	private Integer price;
	private String author;
	private String title;
	private String publish;
	private Integer star;
	private Integer year;
	private Integer month;
	private Integer sold;
	private Long inventory;
	private int outOfPrint;
	private String rank;

	@Builder
	public BookRankDto(Book book, Integer sold, String rank) {
		this.id = book.getId();
		this.image = book.getImage();
		this.price = book.getPrice();
		this.author = book.getAuthor();
		this.title = book.getTitle();
		this.publish = book.getPublish();
		this.star = book.getStar();
		this.year = book.getYear();
		this.month = book.getMonth();
		this.sold = sold;
		this.inventory = book.getInventory();
		this.outOfPrint= book.getOutOfPrint();
		this.rank = rank;
	}


}
