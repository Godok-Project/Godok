package com.baechu.jumoon.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	private final JumoonRepository jumoonRepository;

	private final EntityManager entityManager;

	private final BookRepository bookRepository;

	protected static final Logger orderLogger = LoggerFactory.getLogger("OrderLog");

	private final RedissonClient redissonClient;


	//테스트 할 때만 주석 해제해서 사용하세요
	// public void testbookorder(Long bookid, Integer quantity, Member member) {
	//
	// 	final String lockname = bookid + ":lock";
	// 	final RLock lock = redissonClient.getLock(lockname);
	//
	// 	try {
	// 		if (!lock.tryLock(3, 1, TimeUnit.SECONDS)) {
	// 			throw new RuntimeException("Rock fail");
	// 		}
	//
	// 		Book book = bookRepository.findById(bookid).orElseThrow(
	// 			() -> new CustomException(ErrorCode.BOOK_NOT_FOUND)
	// 		);
	//
	// 		intrymethod(book,quantity,member);
	//
	// 	} catch (Exception e) {
	// 		e.printStackTrace();
	// 	} finally {
	// 		if (lock != null && lock.isLocked()) {
	// 			lock.unlock();
	// 		}
	// 	}
	// }
	// @Transactional
	// public void intrymethod(Book book, Integer qqq, Member member){
	// 	Long inven = book.getInventory() - qqq;
	//
	// 	if (inven < 0) {
	// 		throw new CustomException(ErrorCode.INVALIDATION_NOT_ENOUGH);
	//
	// 	} else {
	// 		book.batchBook(inven);
	// 		bookRepository.save(book);
	// 		jumoonRepository.save(new Jumoon(member, book, qqq));
	// 	}
	// }

	//주문 하기
	public ResponseEntity<BaseResponse> bookdemand(Long bookId, Integer quantity, HttpServletRequest request) {
		final String lockname = bookId + "lock";
		final RLock lock = redissonClient.getLock(lockname);

		HttpSession session = request.getSession(false);
		if (session == null) {
			return BaseResponse.toResponseEntity(ErrorCode.Forbidden);
		} else {

			try{
				if (!lock.tryLock(1,3, TimeUnit.SECONDS)){
					System.out.println("키 점거 실패");
					orderLogger.info("키 점거 실패");
					return BaseResponse.toResponseEntity(ErrorCode.CONFLICT_KEY);
				}

				Member member = (Member)request.getSession()
					.getAttribute(SessionConst.LOGIN_MEMBER);

				Book book = entityManager.find(Book.class, bookId);

				bookorder(book,quantity,member);


				orderLogger.info("bookId:{}, memberId:{}, state:{}, quantity:{}, totalPrice:{}",bookId,member.getId(),"order",quantity,book.getPrice()*quantity);

			}catch (Exception e){
				e.printStackTrace();
			}finally {
				if (lock != null && lock.isLocked()){
					lock.unlock();
				}
			}

			return BaseResponse.toResponseEntity(SuccessCode.ORDER_SUCCESS);
		}
	}

	@Transactional
	public void bookorder(Book book, Integer quantity, Member member){
		Long inven = book.getInventory()-quantity;

		if (inven<0){ // 재고가 0보다 작으면 잘 못된 주문
			throw new CustomException(ErrorCode.INVALIDATION_NOT_ENOUGH);
		}else if(inven ==0 ){ // 재고가 0이면 품절 -> modifiedAt 변경
			jumoonRepository.save(new Jumoon(member, book, quantity));
			book.inventoryChangeBook(inven);
		}else { // 품절되지 않으면 inventory만 바꿔줌
			jumoonRepository.save(new Jumoon(member, book, quantity));
			book.batchBook(inven);
		}
	}


	public ResponseEntity<BaseResponse> cancelbook(Long jumoonId, HttpServletRequest request) {
		Member member = (Member)request.getSession()
			.getAttribute(SessionConst.LOGIN_MEMBER);

		Jumoon jumoon = jumoonRepository.findByIdAndMember(jumoonId, member).orElseThrow(
			()-> new CustomException(ErrorCode.Forbidden)
		);

		Long bookid = jumoon.getBook().getId();

		final String lockname = bookid + "lock";
		final RLock lock = redissonClient.getLock(lockname);

		try{
			if (!lock.tryLock(1,3, TimeUnit.SECONDS)){
				System.out.println("키 점거 실패");
				orderLogger.info("키 점거 실패");
				return BaseResponse.toResponseEntity(ErrorCode.CONFLICT_KEY);
			}

			Book book = entityManager.find(Book.class, bookid);

			Long inven = book.getInventory()+jumoon.getQuantity();
			book.inventoryChangeBook(inven);

			bookcancle(book,inven,jumoon);

			orderLogger.info("bookId:{}, memberId:{}, state:{}, quantity:{}, totalPrice:{}",book.getId(),member.getId(),"cancel",jumoon.getQuantity(),book.getPrice()*jumoon.getQuantity());

		}catch (Exception e){
			e.printStackTrace();
		}finally {
			if (lock != null && lock.isLocked()){
				lock.unlock();
			}
		}
		return BaseResponse.toResponseEntity(SuccessCode.ORDER_SUCCESS);
	}

	@Transactional
	public void bookcancle(Book book, Long inven, Jumoon jumoon){
		book.inventoryChangeBook(inven);
		jumoonRepository.delete(jumoon);

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
