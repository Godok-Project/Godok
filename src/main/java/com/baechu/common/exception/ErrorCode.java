package com.baechu.common.exception;

import static org.springframework.http.HttpStatus.*;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

	/* 400 BAD_REQUEST : 잘못된 요청 */
	INVALIDATION_PASSWORD(BAD_REQUEST, "비밀번호가 일치하지 않습니다."),

	/* 403 Forbidden : 권한 없음 */
	Forbidden(FORBIDDEN, "권한이 없습니다"),

	/* 404 NOT_FOUND : Resource 를 찾을 수 없음 */
	MEMBER_NOT_FOUND(NOT_FOUND, "해당 유저 정보를 찾을 수 없습니다"),


	/* 409 CONFLICT : Resource 의 현재 상태와 충돌. 보통 중복된 데이터 존재 */
	DUPLICATE_RESOURCE(CONFLICT, "데이터가 이미 존재합니다"),
	DUPLICATE_MEMBER(CONFLICT, "중복된 사용자가 존재합니다"),

	;

	private final HttpStatus httpStatus;
	private final String detail;
}


