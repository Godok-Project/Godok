package com.baechu.jumoon.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baechu.book.entity.Book;
import com.baechu.book.repository.BookRepository;
import com.baechu.common.dto.BaseResponse;
import com.baechu.common.exception.CustomException;
import com.baechu.common.exception.ErrorCode;
import com.baechu.common.exception.SuccessCode;
import com.baechu.jumoon.dto.JumoonResponseDto;
import com.baechu.jumoon.entity.Jumoon;
import com.baechu.jumoon.repository.JumoonRepository;
import com.baechu.member.entity.Member;
import com.baechu.session.SessionConst;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JumoonService {

	private final BookRepository bookRepository;

	private final JumoonRepository jumoonRepository;

	private final RedisTemplate redisTemplate;

	private Long redisbookId = 10000000L;

	@Transactional
	public void resetRedisBookId(){
		redisbookId = 10000000L;
	}


	@Transactional
	public void fakebookorder(Book book, Member member, Integer quantity){

		ValueOperations<String, Object> values = redisTemplate.opsForValue();
		String bookkey = "b"+book.getId();
		if (values.get(bookkey)==null){ //기존 이 책에 대한 주문이 없는 경우
			//책 주문량,재고량 에대한 데이터를 Redis에 저장
			Long left = book.getInventory()-quantity;
			String bookdata = quantity+","+left;
			values.set(bookkey,bookdata);
		}else { //기존에 이 책에대한 주문이 있는경우
			Long nowQ = Long.valueOf(values.get(bookkey).toString().split(",")[0])+quantity;
			Long nowLeft = Long.valueOf(values.get(bookkey).toString().split(",")[1])-quantity;
			String bookdata = nowQ+","+nowLeft;
			values.set(bookkey,bookdata);
		}

		//주문 데이터 넣기
		String bookjumoon = redisbookId+","+book.getId()+","+quantity+","+ LocalDateTime.now();
		redisbookId+=1;
		if (values.get(member.getId().toString())==null){ //해당 멤버가 오늘 주문안한 경우
			List<String> jumoons = new ArrayList<>();
			jumoons.add(bookjumoon);
			values.set(member.getId().toString(),jumoons);
		}else {  //해당 멤버가 오늘 주문한 경우
			List<String> jumoons = (List<String>)values.get(member.getId().toString());
			jumoons.add(bookjumoon);
			values.set(member.getId().toString(),jumoons);
		}
	}


	//주문 하기
	@Transactional
	public ResponseEntity<BaseResponse> bookdemand(Long bookId, Integer quantity, HttpServletRequest request) {

		HttpSession session = request.getSession(false);
		if (session == null) {
			return BaseResponse.toResponseEntity(ErrorCode.Forbidden);
		} else {

			Member member = (Member)request.getSession()
				.getAttribute(SessionConst.LOGIN_MEMBER);

			Book book = bookRepository.findById(bookId).orElseThrow(
				() -> new CustomException(ErrorCode.BOOK_NOT_FOUND));

			//Redis에 같은 bookid의 주문이 있는지 확인하기
			//ex) "b17":"3,7" >> "주문량, 재고량"
			ValueOperations<String, Object> values = redisTemplate.opsForValue();
			String bookkey = "b"+bookId;
			if (values.get(bookkey)==null){ //기존 이 책에 대한 주문이 없는 경우
				//책 주문량,재고량 에대한 데이터를 Redis에 저장
				Long left = book.getInventory()-quantity;
				if(left<0){
					return BaseResponse.toResponseEntity(ErrorCode.INVALIDATION_NOT_ENOUGH);
				}else {
					String bookdata = quantity+","+left;
					values.set(bookkey,bookdata);
				}
			}else { //기존에 이 책에대한 주문이 있는경우
				Long nowQ = Long.valueOf(values.get(bookkey).toString().split(",")[0])+quantity;
				Long nowLeft = Long.valueOf(values.get(bookkey).toString().split(",")[1])-quantity;
				if (nowLeft<0){
					return BaseResponse.toResponseEntity(ErrorCode.INVALIDATION_NOT_ENOUGH);
				}else {
					String bookdata = nowQ+","+nowLeft;
					values.set(bookkey,bookdata);
				}
			}

			//주문 데이터 넣기
			String bookjumoon = redisbookId+","+bookId+","+quantity+","+ LocalDateTime.now();
			redisbookId+=1;
			if (values.get(member.getId().toString())==null){ //해당 멤버가 오늘 주문안한 경우
				List<String> jumoons = new ArrayList<>();
				jumoons.add(bookjumoon);
				values.set(member.getId().toString(),jumoons);
			}else {  //해당 멤버가 오늘 주문한 경우
				List<String> jumoons = (List<String>)values.get(member.getId().toString());
				jumoons.add(bookjumoon);
				values.set(member.getId().toString(),jumoons);
			}

			return BaseResponse.toResponseEntity(SuccessCode.ORDER_SUCCESS);
		}
	}


	//레디스 안에 있는 주문 취소하기
	@Transactional
	public ResponseEntity<BaseResponse> cancelbook(Long jumoonId, HttpServletRequest request) {
		Member member = (Member)request.getSession()
			.getAttribute(SessionConst.LOGIN_MEMBER);

		ValueOperations<String,Object> values = redisTemplate.opsForValue();

		//멤버의 주문 내역 불러와서 원하는 주문 내역 삭제하기
		List<String> jumoons = (List<String>)values.get(member.getId().toString());
		for(String i : jumoons){
			if(i.split(",")[0].equals(jumoonId.toString())){
				String bookid = i.split(",")[1];
				Long bookQ = Long.valueOf(i.split(",")[2]);
				System.out.println("bookid = " + bookid);
				String bookstate = (String)values.get("b"+bookid);
				System.out.println("bookstate = " + bookstate);
				Long f1 = Long.valueOf(bookstate.split(",")[0])-bookQ;
				Long f2 = Long.valueOf(bookstate.split(",")[1])+bookQ;
				String nstate = f1+","+f2;
				values.set("b"+bookid,nstate);
				jumoons.remove(i);
				break;
			}
		}
		values.set(member.getId().toString(),jumoons);

		return BaseResponse.toResponseEntity(SuccessCode.ORDER_SUCCESS);
	}





	//주문 리스트 만들기
	@Transactional
	public List<JumoonResponseDto> jumoonlist(HttpServletRequest request) {

		HttpSession session = request.getSession(false);
		if (session == null) {
			new CustomException(ErrorCode.MEMBER_NOT_FOUND);
		}

		Member member = (Member)request.getSession()
			.getAttribute(SessionConst.LOGIN_MEMBER);

		List<Jumoon> jumoons = jumoonRepository.findAllByMember(member);

		List<JumoonResponseDto> jumoonList = new ArrayList<>();

		ValueOperations<String,Object> values = redisTemplate.opsForValue();
		if (values.get(member.getId().toString())!=null){
			List<String> redislist = (List)values.get(member.getId().toString());
			for (String i : redislist){
				Book inbook = bookRepository.findById(Long.valueOf(i.split(",")[1])).orElseThrow(
					()->new CustomException(ErrorCode.BOOK_NOT_FOUND)
				);
				jumoonList.add(new JumoonResponseDto(Long.parseLong(i.split(",")[0]),inbook,Integer.parseInt(i.split(",")[2]), i.split(",")[3], "Y"));
			}
		}

		for (Jumoon i : jumoons){
			String paLDT = i.getJumoonat().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
			jumoonList.add(new JumoonResponseDto(i.getId(), i.getBook(), i.getQuantity(), paLDT, "N"));
		}

		return jumoonList;

	}
}
