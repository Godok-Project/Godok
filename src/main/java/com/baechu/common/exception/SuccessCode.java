package com.baechu.common.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
// ResponseStatusException 과 비슷해 보이지만, 개발자가 한번에 관리,재사용 할 수 있게 정리.
public enum SuccessCode {
	SIGNUP_SUCCESS(HttpStatus.OK, "회원가입 성공."),
	LOGIN_SUCCESS(HttpStatus.OK, "로그인 성공."),
	LOGOUT_SUCCESS(HttpStatus.OK, "로그아웃 성공."),
	ORDER_SUCCESS(HttpStatus.OK, "주문 성공.");

	private final HttpStatus httpStatus;
	private final String detail;
}
