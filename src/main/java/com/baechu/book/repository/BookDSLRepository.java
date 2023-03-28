package com.baechu.book.repository;

import static com.baechu.book.entity.QBook.*;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.stereotype.Repository;

import com.baechu.book.dto.FilterDto;
import com.baechu.book.entity.Book;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;

@Repository
public class BookDSLRepository {

	private final JPAQueryFactory queryFactory;

	public BookDSLRepository(EntityManager entityManager) {
		this.queryFactory = new JPAQueryFactory(entityManager);
	}

	public List<Book> searchByCursor(FilterDto filter, Book lastBook) {
		return queryFactory.selectFrom(book)
			.where(
				categoryResult(filter.getCategory()),
				babyCategoryResult(filter.getBabyCategory()),
				// titleResult(filter.getQuery()),
				// full-text query
				fulltextTitle(filter.getQuery()).gt(0),
				starResult(filter.getStar()),
				yearResult(filter.getYear()),
				PriceResult(filter.getMinPrice(), filter.getMaxPrice()),
				publishResult(filter.getPublish()),
				authorResult(filter.getAuthor()),
				cursorPaging(filter, lastBook)
			)
			.orderBy(getCursorSort(filter.getSort()))
			.limit(filter.getTotalRow())
			.fetch();
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

	// full-text query
	private NumberTemplate fulltextTitle(String query) {
		NumberTemplate template = Expressions.numberTemplate(Double.class,
			"function('match',{0},{1})", book.title, "+" + query + "*");

		return template;
	}

	private Predicate starResult(Integer star) {
		if (star == null || star == 0)
			return null;
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
		if (minPrice == null & maxPrice == null) {
			return null;
		}
		if (minPrice == null) {
			return book.price.loe(maxPrice);
		} else if (maxPrice == null) {
			return book.price.goe(minPrice);
		} else {
			return book.price.between(minPrice, maxPrice);
		}
	}

	private Predicate publishResult(String publish) {
		return publish.isEmpty() ? null : book.publish.contains(publish);
	}

	private Predicate authorResult(String author) {
		return author.isEmpty() ? null : book.author.contains(author);
	}

	private Predicate cursorPaging(FilterDto filter, Book lastBook) {
		if (filter.getCursor() == 1) {
			return null;
		}
		Integer sort = filter.getSort();
		Long cursor = filter.getCursor();
		if (sort == 0) {
			return book.id.gt(cursor);
		} else if (sort == 1) {
			return book.title.lt(lastBook.getTitle()).or(book.title.eq(lastBook.getTitle()).and(book.id.lt(cursor)));
		} else if (sort == 2) {
			return book.price.lt(lastBook.getPrice()).or(book.price.eq(lastBook.getPrice()).and(book.id.lt(cursor)));
		} else if (sort == 3) {
			return book.price.gt(lastBook.getPrice()).or(book.price.eq(lastBook.getPrice()).and(book.id.gt(cursor)));
		}
		return null;
	}

	private OrderSpecifier<?>[] getCursorSort(Integer sort) {
		List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();
		if (sort == null || sort == 0) {
			orderSpecifiers.add(
				new OrderSpecifier(Order.ASC, book.id));
			return orderSpecifiers.toArray(OrderSpecifier<?>[]::new);
		}

		if (sort == 1) {
			orderSpecifiers.add(new OrderSpecifier(Order.DESC, book.title));
			orderSpecifiers.add(new OrderSpecifier(Order.DESC, book.id));
		} else if (sort == 2) {
			orderSpecifiers.add(new OrderSpecifier(Order.DESC, book.price));
			orderSpecifiers.add(new OrderSpecifier(Order.DESC, book.id));
		} else if (sort == 3) {
			orderSpecifiers.add(new OrderSpecifier(Order.ASC, book.price));
			orderSpecifiers.add(new OrderSpecifier(Order.ASC, book.id));
		}
		return orderSpecifiers.toArray(OrderSpecifier<?>[]::new);
	}
}
