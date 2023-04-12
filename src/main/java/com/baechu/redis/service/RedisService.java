package com.baechu.redis.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import com.baechu.book.entity.Book;
import com.baechu.book.repository.BookRepository;
import com.baechu.common.exception.CustomException;
import com.baechu.common.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisService {

	private final RedisTemplate redisTemplate;

	//데이터 넣기
	public void setValue(String key, Object data){
		ValueOperations<String, Object> values = redisTemplate.opsForValue();
		values.set(key, data);
	}

	//데이터 가져오기
	public Object getValues(String key){
		ValueOperations<String, Object> values = redisTemplate.opsForValue();
		return values.get(key);
	}

}
