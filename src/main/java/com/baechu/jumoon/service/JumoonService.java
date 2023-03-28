package com.baechu.jumoon.service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baechu.book.entity.Book;
import com.baechu.book.repository.BookDSLRepository;
import com.baechu.book.repository.BookRepository;
import com.baechu.common.dto.BaseResponse;
import com.baechu.common.exception.CustomException;
import com.baechu.common.exception.ErrorCode;
import com.baechu.common.exception.SuccessCode;
import com.baechu.jumoon.dto.JumoonResponseDto;
import com.baechu.jumoon.entity.Jumoon;
import com.baechu.jumoon.repository.JumoonRepository;
import com.baechu.member.entity.Member;
import com.baechu.member.repository.MemberRepository;
import com.baechu.session.SessionConst;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JumoonService {

	private final BookRepository bookRepository;

	private final JumoonRepository jumoonRepository;



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

			Long inventory = book.getInventory();
			Long restOver = inventory - quantity;
			if (restOver >= 0) {
				book.setInventory(restOver);
			} else {
				throw new CustomException(ErrorCode.INVALIDATION_ORDER);
			}

			Jumoon jumoon = new Jumoon(member,book,quantity);
			jumoonRepository.save(jumoon);

			return BaseResponse.toResponseEntity(SuccessCode.ORDER_SUCCESS);
		}


	}

	@Transactional
	public ResponseEntity<BaseResponse> cancelbook(Long jumoonId, HttpServletRequest request) {
		Member member = (Member)request.getSession()
			.getAttribute(SessionConst.LOGIN_MEMBER);

		Jumoon jumoon = jumoonRepository.findByIdAndMember(jumoonId,member).orElseThrow(
			() -> new CustomException(ErrorCode.JUMOON_NOT_FOUND));

		if (jumoon.isJumoonconfirm()){
			jumoon.cancel();
			return BaseResponse.toResponseEntity(SuccessCode.ORDER_SUCCESS);
		}else {
			return BaseResponse.toResponseEntity(ErrorCode.INVALIDATION_JUMOON);
		}



	}

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
		for (Jumoon i : jumoons){
			if(i.isJumoonconfirm()){
				String paLDT = i.getJumoonat().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
				jumoonList.add(new JumoonResponseDto(i.getId(), i.getBook(), i.getQuantity(), paLDT));
			}
		}

		return jumoonList;

	}
}
