package com.baechu.common.dto;

import org.springframework.http.ResponseEntity;

import com.baechu.common.exception.ErrorCode;
import com.baechu.common.exception.SuccessCode;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
// 실제로 Client 에게 보내는 Format
public class BaseResponse {
	private final String msg;
	private final int statusCode;

	public static ResponseEntity<BaseResponse> toResponseEntity(ErrorCode errorCode) {
		return ResponseEntity
			.status(errorCode.getHttpStatus())
			.body(BaseResponse.builder()
				.msg(errorCode.getDetail())
				.statusCode(errorCode.getHttpStatus().value())
				.build());
	}

	public static ResponseEntity<BaseResponse> toResponseEntity(SuccessCode successCode) {
		return ResponseEntity
			.status(successCode.getHttpStatus())
			.body(BaseResponse.builder()
				.msg(successCode.getDetail())
				.statusCode(successCode.getHttpStatus().value())
				.build());
	}
}