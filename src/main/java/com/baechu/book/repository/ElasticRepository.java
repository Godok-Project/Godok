package com.baechu.book.repository;

import static com.baechu.elastic.custom.CustomQueryBuilders.*;

import java.util.List;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchPhraseQueryBuilder;
import org.elasticsearch.index.query.PrefixQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Repository;

import com.baechu.book.dto.BookDto;
import com.baechu.book.dto.FilterDto;
import com.baechu.book.dto.autoMakerDto;
import com.baechu.elastic.custom.CustomBoolQueryBuilder;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
@Getter
public class ElasticRepository {

	private final ElasticsearchOperations operations;

	// keyword 검색
	public SearchHits<BookDto> keywordSearchByElastic(FilterDto filter) {
		NativeSearchQuery build = new NativeSearchQueryBuilder()
			.withMinScore(10f)
			.withQuery(new CustomBoolQueryBuilder()
				.must(multiMatchQuery(filter.getQuery(), "title", "author"))
				.filter(matchQuery("category.keyword", filter.getCategory()))
				.filter(matchQuery("baby_category.keyword", filter.getBabyCategory()))
				.should(matchPhraseQuery("title", filter.getQuery()))
			)
			.withSorts(sortQuery(filter.getSort()))
			.build();

		return operations.search(build, BookDto.class);
	}

	// filter 검색
	public SearchHits<BookDto> filterSearchByElastic(FilterDto filter, List<Object> searchAfter) {
		NativeSearchQuery build = new NativeSearchQueryBuilder()
			.withMinScore(10f)
			.withQuery(new CustomBoolQueryBuilder()
				.must(multiMatchQuery(filter.getQuery(), "title", "author"))
				.mustNot(inventoryQuery(filter.getInventory()))
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

		return operations.search(build, BookDto.class);
	}

	// 자동 완성
	public SearchHits<autoMakerDto> autoMaker(String query) {
		NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
			.withQuery(new BoolQueryBuilder()
				.should(new MatchPhraseQueryBuilder("title", query))
				.should(new PrefixQueryBuilder("title.keyword", query))
			)
			.withCollapseField("title.keyword")
			.build();

		return operations.search(searchQuery, autoMakerDto.class);
	}
}
