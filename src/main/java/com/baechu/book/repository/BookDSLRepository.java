package com.baechu.book.repository;

import static com.baechu.book.entity.QBook.*;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.stereotype.Repository;

import com.baechu.book.dto.BookDto;
import com.baechu.book.dto.FilterDto;
import com.baechu.book.dto.QBookDto;
import com.querydsl.core.types.NullExpression;
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

	public List<BookDto> searchByCursor(FilterDto filter) {
		String query = filter.getQuery();
		return queryFactory
			.select(
				new QBookDto(book.id, book.image, book.price, book.author, book.title, book.publish, book.star,
					book.year,
					book.month, fulltextTitle(query)))
			.from(book)
			.where(
				categoryResult(filter.getCategory()),
				babyCategoryResult(filter.getBabyCategory()),
				fulltextTitle(query).gt(0),
				starResult(filter.getStar()),
				yearResult(filter.getYear()),
				PriceResult(filter.getMinPrice(), filter.getMaxPrice()),
				publishResult(filter.getPublish()),
				authorResult(filter.getAuthor()),
				cursorPaging(filter)
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

	// full-text query
	private NumberTemplate fulltextTitle(String query) {
		NumberTemplate template = Expressions.numberTemplate(Double.class,
			"function('match',{0},{1},{2})", book.title, book.author, query + "*");

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
		return publish == null || publish.isEmpty() ? null : book.publish.contains(publish);
	}

	private Predicate authorResult(String author) {
		return author == null || author.isEmpty() ? null : book.author.contains(author);
	}

	private Predicate cursorPaging(FilterDto filter) {
		if (filter.getSearchAfterId() == null) {
			return null;
		}
		Integer sort = filter.getSort();
		Long searchId = filter.getSearchAfterId();
		String searchSort = filter.getSearchAfterSort();
		if(searchSort == null || searchId == null)
			return null;
		if (sort == 0) {
			return fulltextTitle(filter.getQuery()).lt(Double.valueOf(searchSort))
				.or(fulltextTitle(filter.getQuery()).eq(Double.valueOf(searchSort)).and(book.id.gt(searchId)));
		} else if (sort == 1) {
			return book.title.gt(searchSort).or(book.title.eq(searchSort).and(book.id.lt(searchId)));
		} else if (sort == 2) {
			return book.price.lt(Integer.valueOf(searchSort))
				.or(book.price.eq(Integer.valueOf(searchSort)).and(book.id.lt(searchId)));
		} else if (sort == 3) {
			return book.price.gt(Integer.valueOf(searchSort))
				.or(book.price.eq(Integer.valueOf(searchSort)).and(book.id.gt(searchId)));
		}
		return null;
	}

	private OrderSpecifier<?>[] getCursorSort(Integer sort) {
		List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();
		if (sort == null || sort == 0) {
			orderSpecifiers.add(
				new OrderSpecifier(Order.ASC, NullExpression.DEFAULT, OrderSpecifier.NullHandling.Default));
			return orderSpecifiers.toArray(OrderSpecifier<?>[]::new);
		}

		if (sort == 1) {
			orderSpecifiers.add(new OrderSpecifier(Order.ASC, book.title));
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
