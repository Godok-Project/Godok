package com.baechu.book.dto;

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
	private String searchAfterSort;
	private Long searchAfterId;
	private Integer page;

	public void checkParameterValid() {
		if (query == null || query.isEmpty())
			throw new CustomException(ErrorCode.QUERY_NOT_FOUND);
		totalRow = totalRow == null ? 10 : totalRow;
		page = page == null ? 1 : page;
	}
}
