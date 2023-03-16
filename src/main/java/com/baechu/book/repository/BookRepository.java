package com.baechu.book.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.baechu.book.entity.Book;

public interface BookRepository extends JpaRepository<Book, Long> {
}