package com.baechu.member.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.baechu.common.dto.BaseResponse;
import com.baechu.member.dto.SigninDto;
import com.baechu.member.service.MemberService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class MemberController {

	private final MemberService memberService;

	@PostMapping("/login")
	public void login() {

	}

	@PostMapping("/signin")
	@ResponseBody
	public ResponseEntity<BaseResponse> signin(@RequestBody SigninDto signinDto) {
		return memberService.signin(signinDto);
	}

	@GetMapping("signin")
	public String signinPage() {
		return "signin";
	}
}

// 10 대부터 0 시작
// 비밀번호 4 ~ 12이하
// 주소 지우기