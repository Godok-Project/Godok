package com.baechu.book.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.ColumnDefault;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
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
	@ColumnDefault("작가 미상")
	private String author;

	@Column(nullable = false)
	@ColumnDefault("출판사 미상")
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
}