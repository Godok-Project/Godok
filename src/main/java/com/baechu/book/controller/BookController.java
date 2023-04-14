package com.baechu.book.controller;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.baechu.book.dto.BookListDto;
import com.baechu.book.dto.BookRankDto;
import com.baechu.book.dto.FilterDto;
import com.baechu.book.service.BookService;
import com.baechu.common.ParamToDto;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class BookController {
	private final BookService bookService;

	@GetMapping("/detail/{id}")
	public String detailPage(Model model, @PathVariable Long id) {

		Map<String, Object> info = bookService.bookdetail(id);

		model.addAttribute("info", info);

		return "detail";
	}

	@GetMapping("/search")
	public String searchByWord(Model model, @ParamToDto FilterDto filter) {
		filter.checkParameterValid();
		BookListDto result = bookService.keywordSearchByElastic(filter);
		model.addAttribute("result", result);
		return "search";
	}

	@GetMapping("/search/filter")
	public String searchByFilter(Model model, @ParamToDto FilterDto filter) {
		filter.checkParameterValid();
		BookListDto result = bookService.filterSearchByElastic(filter);
		model.addAttribute("result", result);
		return "search";
	}

	@GetMapping("/main")
	public String bookList(Model model) {

		List<BookRankDto> list = bookService.bookList();
		model.addAttribute("list", list);

		return "main";
	}

	@GetMapping("/auto")
	public String autoMaker(Model model, @RequestParam String query) {
		List<String> result = bookService.autoMaker(query);
		model.addAttribute("makers", result);
		return "search::#autoMaker";
	}
}