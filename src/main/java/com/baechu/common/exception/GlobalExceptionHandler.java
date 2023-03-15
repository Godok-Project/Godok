package com.baechu.common.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.baechu.common.dto.BaseResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(value = {CustomException.class})
	protected ResponseEntity<BaseResponse> handleCustomException(CustomException e) {
		log.error("handleDataException throw Exception : {}", e.getErrorCode());
		return BaseResponse.toResponseEntity(e.getErrorCode());
	}
}