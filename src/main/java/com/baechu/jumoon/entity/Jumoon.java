package com.baechu.jumoon.entity;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.ColumnDefault;

import com.baechu.book.entity.Book;
import com.baechu.member.entity.Member;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Jumoon {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Integer quantity;

	//주문이 들어가면 true, 취소되면 false 로 한다.
	@Column(nullable = false)
	@ColumnDefault(value = "true")
	private boolean onc;

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
		this.onc = true;
	}

	public void cancel(){
		this.onc = false;
	}

}
