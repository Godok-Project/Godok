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
	INVALIDATION_ORDER(BAD_REQUEST, "재고보다 많은 주문을 할 수 없습니다."),
	INVALIDATION_JUMOON(BAD_REQUEST, "이미 취소된 주문을 또 취소 할 수 없습니다."),
	INVALIDATION_NOT_ENOUGH(BAD_REQUEST, "재고량이 부족합니다."),
	INVALID_PARAMETERS(BAD_REQUEST, "잘못된 URL입니다."),

	/* 403 Forbidden : 권한 없음 */
	Forbidden(FORBIDDEN, "권한이 없습니다"),

	/* 404 NOT_FOUND : Resource 를 찾을 수 없음 */
	MEMBER_NOT_FOUND(NOT_FOUND, "해당 유저 정보를 찾을 수 없습니다"),
	BOOK_NOT_FOUND(NOT_FOUND, "해당 책 정보를 찾을 수 없습니다"),
	JUMOON_NOT_FOUND(NOT_FOUND, "해당 주문 정보를 찾을 수 없습니다."),
	QUERY_NOT_FOUND(NOT_FOUND, "검색어를 찾을 수 없습니다."),

	/* 409 CONFLICT : Resource 의 현재 상태와 충돌. 보통 중복된 데이터 존재 */
	DUPLICATE_RESOURCE(CONFLICT, "데이터가 이미 존재합니다"),
	DUPLICATE_MEMBER(CONFLICT, "중복된 사용자가 존재합니다"),

	;

	private final HttpStatus httpStatus;
	private final String detail;
}


