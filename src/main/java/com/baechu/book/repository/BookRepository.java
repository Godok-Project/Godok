package com.baechu.book.repository;

import java.util.Optional;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.baechu.book.entity.Book;

public interface BookRepository extends JpaRepository<Book, Long> {

	Optional<Book> findById(Long id);

	@Query(value = "select * from book as b where b.title like concat('%',:query,'%') limit 10", nativeQuery = true)
	List<Book> findBooksByWord(@Param("query") String query);
}