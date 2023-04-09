package com.baechu.book.repository;

import static com.baechu.elastic.custom.CustomQueryBuilders.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Repository;

import com.baechu.book.dto.BookDto;
import com.baechu.book.dto.BookListDto;
import com.baechu.book.dto.FilterDto;
import com.baechu.elastic.custom.CustomBoolQueryBuilder;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
@Getter
public class ElasticRepository {

	private final ElasticsearchOperations operations;

	// 검색
	public BookListDto searchByEsAfter(FilterDto filter) {

		List<Object> searchAfter = initSearchAfter(filter.getSearchAfterSort(), filter.getSearchAfterId());

		NativeSearchQuery build = new NativeSearchQueryBuilder()
			.withQuery(new CustomBoolQueryBuilder()
				.must(multiMatchQuery(filter.getQuery(), "title", "author", "publish"))
				.filter(matchQuery("category.keyword", filter.getCategory()))
				.filter(matchQuery("baby_category.keyword", filter.getBabyCategory()))
				.filter(priceQuery(filter.getMinPrice(), filter.getMaxPrice()))
				.filter(starQuery(filter.getStar()))
				.filter(yearQuery(filter.getYear()))
				.should(matchPhraseQuery("title", filter.getQuery()))
				.should(matchQuery("author", filter.getAuthor()))
				.should(matchQuery("publish", filter.getPublish())))
			.withSearchAfter(searchAfter)
			.withSorts(sortQuery(filter.getSort()))
			.build();

		SearchHits<BookDto> search = operations.search(build, BookDto.class);
		List<SearchHit<BookDto>> searchHits = search.getSearchHits();
		System.out.println(search.getTotalHits());
		List<BookDto> bookDtoList = searchHits.stream().map(hit -> hit.getContent()).collect(Collectors.toList());

		searchAfter = setSearchAfter(searchHits, filter);
		String searchAfterSort = String.valueOf(searchAfter.get(0));
		Long searchAfterId = Long.parseLong(String.valueOf(searchAfter.get(1)));

		BookListDto result = new BookListDto(bookDtoList, searchAfterSort, searchAfterId);
		return result;
	}

	List<Object> initSearchAfter(String searchAfterSort, Long searchAfterId) {
		List<Object> searchAfter = new ArrayList<>();

		if (searchAfterSort == null || searchAfterId == null)
			return null;

		searchAfter.add(searchAfterSort);
		searchAfter.add(searchAfterId);
		return searchAfter;
	}

	public List<Object> setSearchAfter(List<SearchHit<BookDto>> searchHits, FilterDto filter) {
		List<Object> lists = new ArrayList<>();
		if (searchHits.size() == 0) {
			lists.add("-1");
			lists.add("-1");
			return lists;
		} else if (searchHits.size() < filter.getTotalRow()) {
			lists.add("0");
			lists.add("-1");
			return lists;
		}
		return searchHits.get(searchHits.size() - 1).getSortValues();
	}

}
