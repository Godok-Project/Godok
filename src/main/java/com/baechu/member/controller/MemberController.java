package com.baechu.member.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.baechu.common.dto.BaseResponse;
import com.baechu.member.dto.LoginDto;
import com.baechu.member.dto.SigninDto;
import com.baechu.member.service.MemberService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class MemberController {

	private final MemberService memberService;

	@PostMapping("/signin")
	@ResponseBody
	public ResponseEntity<BaseResponse> signin(@RequestBody SigninDto signinDto) {
		return memberService.signin(signinDto);
	}

	@GetMapping("signin")
	public String signinPage() {
		return "signin";
	}

	@PostMapping("/login")
	@ResponseBody
	public ResponseEntity<BaseResponse> login(@RequestBody LoginDto loginDto, HttpServletRequest request) {

		return memberService.login(loginDto, request);
	}

	@GetMapping("/login")
	public String loginPage() {
		return "login";
	}

	@PostMapping("/logout")
	public ResponseEntity<BaseResponse> logout(HttpServletRequest request) {
		return memberService.logout(request);
	}

	@GetMapping("/")
	public String main() {
		return "redirect:/main";
	}
}