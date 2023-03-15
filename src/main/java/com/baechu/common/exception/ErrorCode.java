package com.baechu.common.exception;

import static org.springframework.http.HttpStatus.*;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
	/* 409 CONFLICT : Resource 의 현재 상태와 충돌. 보통 중복된 데이터 존재 */
	DUPLICATE_RESOURCE(CONFLICT, "데이터가 이미 존재합니다"),
	DUPLICATE_MEMBER(CONFLICT, "중복된 사용자가 존재합니다"),
	;

	private final HttpStatus httpStatus;
	private final String detail;
}


