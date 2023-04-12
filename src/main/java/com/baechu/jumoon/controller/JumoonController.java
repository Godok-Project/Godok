package com.baechu.jumoon.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.baechu.book.entity.Book;
import com.baechu.jumoon.dto.JumoonResponseDto;
import com.baechu.jumoon.entity.Jumoon;
import com.baechu.jumoon.service.JumoonService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class JumoonController {

	private final JumoonService jumoonService;

	@GetMapping("/detail/buybooks/{bookId}/{quantity}")
	public String buyBook(@PathVariable Long bookId, @PathVariable Integer quantity, HttpServletRequest request) {

		HttpStatus result = jumoonService.bookdemand(bookId, quantity, request).getStatusCode();

		if (result.isError()) {
			return "redirect:/login";
		} else {
			return "redirect:/jumoon";
		}

	}

	@GetMapping("/jumoon")
	public String jumoonList(Model model, HttpServletRequest request) {

		List<JumoonResponseDto> list = jumoonService.jumoonlist(request);
		model.addAttribute("list", list);

		return "jumoon";
	}

	@GetMapping("/jumoon/{jumoonId}")
	public String cancelbook(@PathVariable Long jumoonId, HttpServletRequest request){
		jumoonService.cancelbook(jumoonId, request);
		//페이지는 새로고침으로  html에서 해주자
		return "redirect:/jumoon";

	}

}
