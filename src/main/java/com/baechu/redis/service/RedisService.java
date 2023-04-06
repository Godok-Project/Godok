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

	private final BookRepository bookRepository;

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

	public void cleanField(){
		redisTemplate.keys("*").stream().forEach(k->{redisTemplate.delete(k);});
	}

	//Redis에 주문 넣기
	public void orderbook(Long bookid, Long memberid, Long quantity){

		String inorder = bookid+","+memberid+","+quantity+','+ LocalDateTime.now();
		ValueOperations<String, Object> values = redisTemplate.opsForValue();
		if (values.size(String.valueOf(memberid))==0){
			List<String> list = new ArrayList<>();
			list.add(inorder);
			values.set(String.valueOf(memberid),list);
		}else {
			List list = (List)values.get(memberid);
			list.add(inorder);
			values.set(String.valueOf(memberid),list);// 이부분 필요없을 수 도있음
		}
		String bookkey = "b"+bookid;
		Book book = bookRepository.findById(bookid).orElseThrow(
			()-> new CustomException(ErrorCode.BOOK_NOT_FOUND)
		);
		Long inven = book.getInventory() - quantity;


		if (values.size(bookkey)==0){
			values.set(bookkey,inven);
		}else {
			Long qqq = (Long)values.get(bookkey);
			values.set(bookkey,qqq-quantity);
		}
	}

	//Redis에 주문 취소
	public void deleteorder(String memberid, String order){
		ValueOperations<String, Object> values = redisTemplate.opsForValue();
		List list = (List)values.get(memberid);
		list.remove(order);
		values.set(memberid,list);// 이부분 필요없을 수 도 있음

		String bookkey = "b"+order.split(",")[0];
		Long nqqq = Long.valueOf(values.get(bookkey)+order.split(",")[0]);
		values.set(bookkey,nqqq);
	}





}
