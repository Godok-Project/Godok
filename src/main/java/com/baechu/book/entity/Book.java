package com.baechu.book.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.hibernate.annotations.ColumnDefault;

import com.baechu.jumoon.entity.Jumoon;

import lombok.Getter;

@Entity
@Getter
public class Book {

	@Id
	@GeneratedValue
	private Long id;

	@Column(nullable = false)
	private String category;

	@Column(nullable = false)
	private String babyCategory;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false)
	private Integer price;

	@Column
	private Integer star;

	@Column(nullable = false)
	private String author;

	@Column(nullable = false)
	private String publish;

	@Column(columnDefinition = "TEXT")
	private String image;

	@Column(nullable = false)
	private Integer year;

	@Column(nullable = false)
	private Integer month;

	@Column(nullable = false)
	@ColumnDefault("4")
	private Long inventory;

	@Column
	private LocalDateTime modifiedAt;

	@Column(nullable = false)
	@ColumnDefault("0")
	private int outOfPrint;

	@OneToMany(mappedBy = "book")
	private List<Jumoon> jumoons = new ArrayList<>();

	public void orderbook(Long inventory){
		this.inventory = inventory;
		this.modifiedAt = LocalDateTime.now();
	}

	public void batchBook(Long inventory){
		this.inventory = inventory;
	}

}