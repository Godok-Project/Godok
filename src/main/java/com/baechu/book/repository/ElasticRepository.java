package com.baechu.book.repository;

import static com.baechu.elastic.custom.CustomQueryBuilders.*;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Repository;

import com.baechu.book.dto.BookDto;
import com.baechu.book.dto.CursorBookDto;
import com.baechu.book.dto.FilterDto;
import com.baechu.elastic.custom.CustomBoolQueryBuilder;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ElasticRepository {

	private final ElasticsearchOperations operations;

	// 검색
	public List<BookDto> searchByEs(FilterDto filter) {
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
			// .withSearchAfter(getCursor(cursor))
			.withSorts(sortQuery(filter.getSort()))
			.build();

		SearchHits<BookDto> search = operations.search(build, BookDto.class);
		List<SearchHit<BookDto>> searchHits = search.getSearchHits();
		System.out.println(search.getTotalHits());
		List<BookDto> result = searchHits.stream().map(hit -> hit.getContent()).collect(Collectors.toList());
		return result;
	}

	/**
	 * 커서 찾아서 페이징 하려고 했던 흔적입니다.
	 */
	// 커서 찾기
	private void getCursor(Long id) {
		// ES에서 id로 데이터 조회하기
		NativeSearchQuery search = new NativeSearchQueryBuilder()
			.withQuery(matchPhraseQuery("id", id + ""))
			.build();
		SearchHits<CursorBookDto> searchResult = operations.search(search, CursorBookDto.class);
		// sort별로 커서 입력하기
	}
}
