package com.baechu.jumoon.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.baechu.book.entity.Book;
import com.baechu.member.entity.Member;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Jumoon {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Integer quantity;

	@Column(nullable = false)
	private LocalDateTime jumoonat;

	@Column(nullable = false)
	private boolean fine;

	@ManyToOne
	@JoinColumn(name =  "member_id", nullable = false)
	private Member member;

	@ManyToOne
	@JoinColumn(name =  "book_id", nullable = false)
	private Book book;

	public Jumoon(Member member, Book book, Integer quantity) {
		this.member = member;
		this.book = book;
		this.quantity = quantity;
		this.jumoonat = LocalDateTime.now();
		this.fine = false;
	}

	public void endFine(){
		this.fine = true;
	}
}
