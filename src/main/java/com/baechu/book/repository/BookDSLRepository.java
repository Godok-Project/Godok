package com.baechu.book.repository;

import static com.baechu.book.entity.QBook.*;

import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.baechu.book.dto.FilterDto;
import com.baechu.book.entity.Book;
import com.querydsl.core.types.NullExpression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;

@Repository
public class BookDSLRepository {

	private final JPAQueryFactory queryFactory;

	public BookDSLRepository(EntityManager entityManager) {
		this.queryFactory = new JPAQueryFactory(entityManager);
	}

	public List<Book> searchBooks(FilterDto filter, Pageable pageable) {
		return queryFactory.selectFrom(book)
			.where(
				categoryResult(filter.getCategory()),
				babyCategoryResult(filter.getBabyCategory()),
				titleResult(filter.getQuery()),
				starResult(filter.getStar()),
				yearResult(filter.getYear()),
				PriceResult(filter.getMinPrice(), filter.getMaxPrice()),
				publishResult(filter.getPublish()),
				authorResult(filter.getAuthor())
			)
			.orderBy(getSort(filter.getSort()))
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();
	}

	public Long getCount(FilterDto filter) {
		return queryFactory.select(book.count())
			.from(book)
			.where(
				titleResult(filter.getQuery()),
				starResult(filter.getStar()),
				yearResult(filter.getYear()),
				PriceResult(filter.getMinPrice(), filter.getMaxPrice()),
				publishResult(filter.getPublish()),
				authorResult(filter.getAuthor())
			)
			.fetchOne();
	}

	private Predicate categoryResult(String category) {
		if (category == null || category.isEmpty()) {
			return null;
		}
		return book.category.eq(category);
	}

	private Predicate babyCategoryResult(String babyCategory) {
		if (babyCategory == null || babyCategory.isEmpty()) {
			return null;
		}
		return book.babyCategory.eq(babyCategory);
	}

	private Predicate titleResult(String query) {
		return book.title.contains(query);
	}

	private Predicate starResult(Integer star) {
		star = star == null ? 0 : star;
		return book.star.goe(star);
	}

	private Predicate yearResult(Integer year) {
		if (year == null || year == 0)
			return null;
		else if (year == 1899) {
			return book.year.loe(year);
		}
		return book.year.between(year, year + 9);
	}

	private Predicate PriceResult(Integer minPrice, Integer maxPrice) {
		// 둘 다 null
		if (minPrice == null & maxPrice == null) {
			return null;
		}
		if (minPrice == null) {
			// 최대만 존재
			return book.price.loe(maxPrice);
		} else if (maxPrice == null) {
			// 최소만 존재
			return book.price.goe(minPrice);
		} else {
			//둘 다 있음
			return book.price.between(minPrice, maxPrice);
		}
	}

	private Predicate publishResult(String publish) {
		return publish.isEmpty() ? null : book.publish.contains(publish);
	}

	private Predicate authorResult(String author) {
		return author.isEmpty() ? null : book.author.contains(author);
	}

	private OrderSpecifier<?> getSort(Integer sort) {
		if (sort == null || sort == 0)
			return new OrderSpecifier(Order.ASC, NullExpression.DEFAULT, OrderSpecifier.NullHandling.Default);
		else if (sort == 1)
			return book.title.asc();
		else if (sort == 2)
			return book.price.desc();
		else if (sort == 3)
			return book.price.asc();
		else
			return new OrderSpecifier(Order.ASC, NullExpression.DEFAULT, OrderSpecifier.NullHandling.Default);
	}
}
