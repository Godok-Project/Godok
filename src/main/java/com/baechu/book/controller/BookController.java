package com.baechu.book.controller;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.baechu.book.dto.BookDto;
import com.baechu.book.dto.BookListDto;
import com.baechu.book.entity.Book;
import com.baechu.book.service.BookService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class BookController {
	private final BookService bookService;

	@GetMapping("/detail/{id}")
	public ModelAndView detailPage(@PathVariable Long id) {

		ModelAndView modelAndView = new ModelAndView("detail");

		Map<String, Object> info = bookService.bookdetail(id);

		modelAndView.addObject("info", info);

		return modelAndView;
	}

	@GetMapping("/detail/buybooks/{bookid}/{quantity}")
	public ModelAndView Buybook(@PathVariable String bookid, @PathVariable String quantity) {

		ModelAndView modelAndView = new ModelAndView("main");

		String ans = bookid + "번 책을 " + quantity + " 권 주문한다용";
		System.out.println(ans);
		return modelAndView;
	}

	@GetMapping("/search")
	public String searchByWord(
		Model model,
		@RequestParam(value = "query", defaultValue = "") String query,
		@RequestParam(value = "page", defaultValue = "0") String page,
		@RequestParam(value = "totalRow", defaultValue = "10") String totalRow
	) {
		// 검색어만 적용한 검색
		BookListDto result = bookService.searchByWord(query, Integer.parseInt(page), Integer.parseInt(totalRow));
		model.addAttribute("result", result);
		return "search";
	}

	@GetMapping("/main")
	public String bookList(Model model, @PageableDefault(page=0,size=10, sort = "id",
	direction = Sort.Direction.DESC) Pageable pageable) {

		Page<Book> list = bookService.bookList(pageable);

		int nowPage = list.getPageable().getPageNumber() + 1;
		int startPage = Math.max(nowPage -4, 1);
		int endPage = Math.min(nowPage + 9, list.getTotalPages());

		model.addAttribute("list", list);
		model.addAttribute("nowPage", nowPage);
		model.addAttribute("startPage", startPage);
		model.addAttribute("endPage", endPage);

		return "main";
	}
}