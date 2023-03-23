package com.baechu.book.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.baechu.book.entity.Book;

public interface BookRepository extends JpaRepository<Book, Long> {

	Optional<Book> findById(Long id);
	List<Book> findAll();
}