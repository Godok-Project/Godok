package com.baechu.redis.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baechu.redis.service.RedisService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/redis")
public class RedisController {

	private final RedisService redisService;

	private final RedisTemplate redisTemplate;


	@PostMapping("")
	public void startRedis(@RequestBody HashMap<String, Object> body){
		redisService.setValue((String)body.get("key"), body.get("data"));
	}

	@GetMapping("")
	public Object startRedis(@RequestParam String key){
		return redisService.getValues(key);
	}

	@GetMapping("/allkeys")
	public List<String> getallkeys(){
		Set<String> redisKeys = redisTemplate.keys("*");

		System.out.println("redisKeys = " + redisKeys);
		// Store the keys in a List
		List<String> keysList = new ArrayList<>();
		Iterator<String> it = redisKeys.iterator();
		while (it.hasNext()) {
			String data = it.next();
			keysList.add(data);
		}
		System.out.println("keysList = " + keysList);
		return keysList;
	}

}
