package com.baechu.member.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.baechu.jumoon.entity.Jumoon;
import com.baechu.member.dto.SigninDto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Member {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String email;

	@Column(nullable = false)
	private String password;

	@Column(nullable = false)
	private Integer sex;

	@Column(nullable = false)
	private Integer age;

	@OneToMany(mappedBy = "member")
	private List<Jumoon> jumoons = new ArrayList<>();

	public Member(SigninDto signinDto) {
		this.email = signinDto.getEmail();
		this.password = signinDto.getPassword();
		this.sex = signinDto.getSex();
		this.age = signinDto.getAge();
	}
}
