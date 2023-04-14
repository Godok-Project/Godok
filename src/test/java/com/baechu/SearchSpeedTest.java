package com.baechu;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import com.baechu.book.dto.FilterDto;
import com.baechu.book.service.BookService;

@SpringBootTest
public class SearchSpeedTest {

	@Autowired
	private BookService service;

	private List<FilterDto> dtos = new ArrayList<>();

	private static final String[] QUERY_LIST = {"여름밤 열 시 반", "이방인", "마음", "농담", "나무 위의 남작", "모모", "성", "사양"};
	private static final String CATEGORY = "소설 시 희곡";
	private static final String BABY_CATEGORY = "세계의 문학";
	private static final Integer STAR = 8;
	private static final Integer MIN_PRICE = 5000;
	private static final Integer MAX_PRICE = 30000;

	@BeforeEach
	public void getFilterDtoList() {
		dtos = new ArrayList<FilterDto>();
		for (String query : QUERY_LIST) {
			dtos.add(new FilterDto(query, STAR, MIN_PRICE, MAX_PRICE, CATEGORY, BABY_CATEGORY, null, null));
		}
	}

	@Test
	public void elasticSpeedTest() throws InterruptedException {
		List<Object> result = new ArrayList<>();
		for (FilterDto dto : dtos) {
			System.out.println(dto.toString());
			List<Object> list = getResult(dto, "elastic");
			result.addAll(list);
			Thread.sleep(1000);
		}
		System.out.println(result.toString());
	}

	@Test
	public void fulltextSpeedTest() throws InterruptedException {
		List<Object> result = new ArrayList<>();
		for (FilterDto dto : dtos) {
			System.out.println(dto.toString());
			List<Object> list = getResult(dto, "fulltext");
			result.addAll(list);
			Thread.sleep(1000);
		}
		System.out.println(result.toString());
	}

	@Test
	public void likeSpeedTest() throws InterruptedException {
		List<Object> result = new ArrayList<>();
		for (FilterDto dto : dtos) {
			System.out.println(dto.toString());
			List<Object> list = getResult(dto, "like");
			result.addAll(list);
			Thread.sleep(1000);
		}
		System.out.println(result.toString());
	}

	private List<Object> getResult(FilterDto filter, String method) throws InterruptedException {
		List<Double> timeResults = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			StopWatch watch = new StopWatch();
			watch.start();
			switch (method) {
				case "like":
					service.filterSearchByLike(filter);
					break;
				case "fulltext":
					service.filterSearchBySql(filter, null);
					break;
				case "elastic":
					service.filterSearchByElastic(filter);
					break;
			}
			watch.stop();
			timeResults.add(watch.getTotalTimeMillis() / 1000d);
		}
		List<Object> result = new ArrayList<>();
		// 결과 반환. 쿼리명, 최저 시간, 평균 시간, 최대 시간
		result.add(filter.getQuery());
		result.add(timeResults.stream().mapToDouble(i -> i).min().getAsDouble());
		result.add(timeResults.stream().mapToDouble(i -> i).sum()/timeResults.size());
		result.add(timeResults.stream().mapToDouble(i -> i).max().getAsDouble());
		return result;
	}
}
