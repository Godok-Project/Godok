package com.baechu;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import com.baechu.book.entity.Book;
import com.baechu.book.repository.BookRepository;
import com.baechu.common.exception.CustomException;
import com.baechu.common.exception.ErrorCode;
import com.baechu.jumoon.entity.Jumoon;
import com.baechu.jumoon.repository.JumoonRepository;
import com.baechu.jumoon.service.JumoonService;
import com.baechu.member.dto.SigninDto;
import com.baechu.member.entity.Member;
import com.baechu.member.repository.MemberRepository;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ConcurrencyOrderTest {

	@Autowired
	MemberRepository memberRepository;

	@Autowired
	BookRepository bookRepository;

	@Autowired
	JumoonRepository jumoonRepository;

	@Autowired
	JumoonService jumoonService;

	@Autowired
	EntityManager entityManager;

	@RepeatedTest(10)
	void testConnection() throws InterruptedException {


		// 방법1: Book 생성자를 만들어 주어야 한다.
		// Book book = new Book("큰카테", "작은카테", "제목스", 9999, 9
		// 	, "작가스", "출판스", null, 1993, 5, 20L);
		// bookRepository.save(book);

		// 방법2: 기존 DB에 있는 책을 이용하고 재고량을 20으로 설정하고 시작한다.
		Book book = bookRepository.findById(1L).orElseThrow(
			()-> new CustomException(ErrorCode.BOOK_NOT_FOUND)
		);
		book.orderbook(20L);

		List<Member> members = new ArrayList<>();
		for (int i = 0; i < 20; i++) {
			String email = "testman" + i + "@ntest.com";
			SigninDto signinDto = new SigninDto();
			signinDto.setEmail(email);
			signinDto.setPassword("111111");
			signinDto.setSex(1);
			signinDto.setAge(1993);
			Member member = new Member(signinDto);
			memberRepository.save(member);
			members.add(member);
		}

		CountDownLatch countDownLatch = new CountDownLatch(20);

		List<JumoonWorker> workers = Stream.
			generate(() -> new JumoonWorker(members.get(0), book, 3, countDownLatch))
			.limit(20)
			.collect(Collectors.toList());

		workers.forEach(worker -> new Thread(worker).start());


		countDownLatch.await(1, TimeUnit.SECONDS);

		Book updatebook = bookRepository.findById(book.getId()).orElseThrow(
			() -> new CustomException(ErrorCode.BOOK_NOT_FOUND)
		);

		List<Jumoon> jumoons = jumoonRepository.findAllByBook_Id(updatebook.getId());
		System.out.println("jumoons.size() = " + jumoons.size());

		assertEquals(6, jumoons.size());
		assertEquals(2, updatebook.getInventory());

	}

	private class JumoonWorker implements Runnable {

		private Member member;
		private Book book;
		private int quantity;
		private CountDownLatch countDownLatch;

		public JumoonWorker(Member member, Book book, int quantity, CountDownLatch countDownLatch) {
			this.member = member;
			this.book = book;
			this.quantity = quantity;
			this.countDownLatch = countDownLatch;
		}

		@Override
		public void run() {
			jumoonService.testbookorder(book.getId(), quantity, member);
			countDownLatch.countDown();
		}
	}
}

