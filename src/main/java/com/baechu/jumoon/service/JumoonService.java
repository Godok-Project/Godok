package com.baechu.jumoon.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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


	@Transactional
	public void fakebookorder(Book book, Member member, Integer quantity){
		jumoonRepository.save(new Jumoon(member,book,quantity));

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

			Long inven = book.getInventory()-quantity;
			if (inven<0){
				return BaseResponse.toResponseEntity(ErrorCode.INVALIDATION_NOT_ENOUGH);
			}else {
				jumoonRepository.save(new Jumoon(member,book,quantity));
				book.orderbook(inven);
			}

			return BaseResponse.toResponseEntity(SuccessCode.ORDER_SUCCESS);
		}
	}

	@Transactional
	public ResponseEntity<BaseResponse> cancelbook(Long jumoonId, HttpServletRequest request) {
		Member member = (Member)request.getSession()
			.getAttribute(SessionConst.LOGIN_MEMBER);

		Jumoon jumoon = jumoonRepository.findByIdAndMember(jumoonId, member).orElseThrow(
			()-> new CustomException(ErrorCode.Forbidden)
		);
		jumoonRepository.delete(jumoon);

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
		Collections.reverse(jumoons);

		List<JumoonResponseDto> jumoonList = new ArrayList<>();

		for (Jumoon i : jumoons){
			if (i.isFine()){
				jumoonList.add(new JumoonResponseDto(i.getId(), i.getBook(), i.getQuantity(), i.getJumoonat().toString(), "N"));
			}else {
				jumoonList.add(new JumoonResponseDto(i.getId(), i.getBook(), i.getQuantity(), i.getJumoonat().toString(), "Y"));
			}
		}

		return jumoonList;

	}

}
