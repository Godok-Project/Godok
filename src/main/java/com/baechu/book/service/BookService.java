package com.baechu.book.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baechu.book.dto.BookListDto;
import com.baechu.book.entity.Book;
import com.baechu.book.repository.BookRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookService {
	private final BookRepository bookRepository;

	public Map<String, Object> bookdetail(Long bookid) {
		Map<String, Object> info = new HashMap<>();
		Book book = bookRepository.findById(bookid).orElseThrow(
			() -> new IllegalArgumentException("해당 번호의 책 없음")
		);
		info.put("bookid", bookid);
		info.put("title", book.getTitle());
		info.put("image", book.getImage());
		info.put("price", book.getPrice());
		info.put("author", book.getAuthor());
		info.put("publish", book.getPublish());
		String birth = book.getYear() + "년 " + book.getMonth() + "월";
		info.put("birth", birth);
		info.put("inventory", book.getInventory());

		return info;
	}

	public BookListDto searchByWord(String query, Integer page, Integer totalRow) {
		if (query.isEmpty()) {
			return new BookListDto();
		}
		PageRequest pageRequest = PageRequest.of(page, totalRow);
		Page<Book> bookPages = bookRepository.findBooksByWord(query, pageRequest);
		long totalCount = bookPages.getTotalElements();
		List<Book> books = bookPages.getContent();

		System.out.println("page = " + page);
		System.out.println("totalCount = " + totalCount);

		return new BookListDto(page, totalCount, books);
	}

	public Page<Book> bookList(Pageable pageable) {
		return bookRepository.findAll(pageable);
	}


	@Transactional
	public void bookOrder(Long bookid, Long bookcall) {
		Book book = bookRepository.findById(bookid).orElseThrow(
			() -> new IllegalArgumentException("해당 아이디의 책은 없습니다.")
		);

		Long inventory = book.getInventory();
		Long restover = inventory-bookcall;
		if (restover>=0){
			book.setInventory(restover);
		}else {
			System.out.println("프론트에서 한번 재고량 체크를 해줬지만, 책재고가 부족하다는 에러 내뱉어야함");
		}
	}
}