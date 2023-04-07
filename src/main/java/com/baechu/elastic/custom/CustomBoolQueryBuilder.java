package com.baechu.elastic.custom;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.stereotype.Component;

@Component
public class CustomBoolQueryBuilder extends BoolQueryBuilder {

	@Override
	public BoolQueryBuilder must(QueryBuilder queryBuilder) {
		if (queryBuilder == null)
			return this;
		return super.must(queryBuilder);
	}

	@Override
	public BoolQueryBuilder filter(QueryBuilder queryBuilder) {
		if (queryBuilder == null)
			return this;
		return super.filter(queryBuilder);
	}

	@Override
	public BoolQueryBuilder should(QueryBuilder queryBuilder) {
		if (queryBuilder == null)
			return this;
		return super.should(queryBuilder);
	}

}