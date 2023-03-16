package com.baechu.book.controller;

import org.springframework.stereotype.Controller;

import com.baechu.book.service.BookService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class BookController {
	private final BookService bookService;
}