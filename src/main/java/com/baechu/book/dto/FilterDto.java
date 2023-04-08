package com.baechu.book.dto;

import java.util.List;

import com.baechu.common.exception.CustomException;
import com.baechu.common.exception.ErrorCode;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FilterDto {
	private String query;
	private Integer sort;
	private Integer year;
	private Integer star;
	private Integer minPrice;
	private Integer maxPrice;
	private String publish;
	private String author;
	private Integer totalRow;
	private String category;
	private String babyCategory;
	private Long cursor;
	private String searchAfterSort;
	private Long searchAfterId;

	public void checkParameterValid() {
		if (query == null || query.isEmpty())
			throw new CustomException(ErrorCode.QUERY_NOT_FOUND);
		totalRow = totalRow == null ? 10 : totalRow;
		cursor = cursor == null ? 1 : cursor;
	}
}
