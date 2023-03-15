package com.baechu.member.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.baechu.member.dto.SigninDto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Member {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long memberId;

	@Column(nullable = false)
	private String email;

	@Column(nullable = false)
	private String password;

	@Column(nullable = false)
	private Integer sex;

	@Column(nullable = false)
	private Integer age;

	public Member(SigninDto signinDto) {
		this.email = signinDto.getEmail();
		this.password = signinDto.getPassword();
		this.sex = signinDto.getSex();
		this.age = signinDto.getAge();
	}
}
