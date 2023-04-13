package com.baechu.book.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.baechu.book.entity.Book;

public interface BookRepository extends JpaRepository<Book, Long> {

	Optional<Book> findById(Long id);
	List<Book> findAll();
}