package com.baechu.jumoon.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baechu.book.entity.Book;
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

	private final JumoonRepository jumoonRepository;

	private final EntityManager entityManager;

	protected static final Logger orderLogger = LoggerFactory.getLogger("OrderLog");


	@Transactional
	public void testbookorder(Long bookid, Integer quantity, Member member){

		Book book = entityManager.find(Book.class,bookid,LockModeType.PESSIMISTIC_WRITE);

		Long inven = book.getInventory()-quantity;
		if (inven<0){
			throw new CustomException(ErrorCode.INVALIDATION_NOT_ENOUGH);
		}else {
			book.inventoryChangeBook(inven);
			jumoonRepository.save(new Jumoon(member,book,quantity));
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

			Book book = entityManager.find(Book.class, bookId, LockModeType.PESSIMISTIC_WRITE);

			Long inven = book.getInventory()-quantity;
			if (inven<0){ // 재고가 0보다 작으면 잘 못된 주문
				return BaseResponse.toResponseEntity(ErrorCode.INVALIDATION_NOT_ENOUGH);
			}else if(inven ==0 ){ // 재고가 0이면 품절 -> modifiedAt 변경
				Jumoon save = jumoonRepository.save(new Jumoon(member, book, quantity));
				book.inventoryChangeBook(inven);
			}else { // 품절되지 않으면 inventory만 바꿔줌
				Jumoon save = jumoonRepository.save(new Jumoon(member, book, quantity));
				book.batchBook(inven);
			}
			orderLogger.info("bookId:{}, memberId:{}, state:{}, quantity:{}, totalPrice:{}",bookId,member.getId(),"order",quantity,book.getPrice()*quantity);
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

		Book book = entityManager.find(Book.class, jumoon.getBook().getId(), LockModeType.PESSIMISTIC_WRITE);

		Long inven = book.getInventory()+jumoon.getQuantity();
		book.inventoryChangeBook(inven);

		jumoonRepository.delete(jumoon);
		orderLogger.info("bookId:{}, memberId:{}, state:{}, quantity:{}, totalPrice:{}",book.getId(),member.getId(),"cancel",jumoon.getQuantity(),book.getPrice()*jumoon.getQuantity());
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
