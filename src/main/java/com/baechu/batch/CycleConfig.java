package com.baechu.batch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.baechu.book.entity.Book;
import com.baechu.book.repository.BookRepository;
import com.baechu.common.exception.CustomException;
import com.baechu.common.exception.ErrorCode;
import com.baechu.jumoon.entity.Jumoon;
import com.baechu.jumoon.repository.JumoonRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CycleConfig {

	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private final BookRepository bookRepository;
	private final JumoonRepository jumoonRepository;
	private final RedisTemplate redisTemplate;

	@Bean
	public Job jobs() {
		Job job = jobBuilderFactory.get("job")
			.start(StepA())
			.on("FAILED")
			.end()
			.from(StepA())
			.on("*")
			.to(StepB())
			.on("FAILED")
			.end()
			.from(StepB())
			.on("*")
			.to(StepC())
			.on("FAILED")
			.end()
			.from(StepC())
			.on("*")
			.to(StepD())
			.end()
			.build();
		return job;
	}

	List<Long> bookidkeys = new ArrayList<>();
	List<Jumoon> jumoons = new ArrayList<>();

	@Bean
	public Step StepA() {
		return stepBuilderFactory.get("StepA")
			.tasklet((contribution, chunkContext) -> {
				log.info("Step1. Jumoontable에서 fine가 false인 책들만 가져오기");

				jumoons = jumoonRepository.findAllByFine(false);

				return RepeatStatus.FINISHED;
			})
			.build();
	}

	@Bean
	public Step StepB() {
		return stepBuilderFactory.get("StepB")
			.tasklet((contribution, chunkContext) -> {
				log.info("Step2. 어떤 책이 얼마나 팔렸나 계산");


				//어떤 책이 얼마나 팔렸는지 map으로 저장
				Map<Long, Integer> soldbooks = new HashMap<>();
				for(Jumoon i : jumoons){
					Long bookid = i.getBook().getId();
					Integer quantity = i.getQuantity();

					if (soldbooks.containsKey(bookid)){
						soldbooks.put(bookid,(soldbooks.get(bookid)+quantity));
					}else {
						soldbooks.put(bookid,quantity);
					}
				}

				//map의 value 순서대로 정렬하기
				bookidkeys = new ArrayList<>(soldbooks.keySet());
				Collections.sort(bookidkeys, ((o1, o2) -> (soldbooks.get(o2).compareTo(soldbooks.get(o1)))));

				//어제 주문된 책 종류가 8개 미만인 경우 100,101,102... 순으로 책을 채워준다.
				Long cnt = 100L;
				while (bookidkeys.size()<8){
					bookidkeys.add(cnt);
					soldbooks.put(cnt,0);
					cnt++;
				}

				//이제 랭크 순서대로 넣어준다. "rank" : {"1,9", "2,8"....} 책id와 판매량은 쉼표로 구분하고 각각 리스트의 원소로 넣자

				List<String> bookrankAndsold = new ArrayList<>();
				for (int i = 0; i < 8; i++) {
					String rankvalue = bookidkeys.get(i)+","+soldbooks.get(bookidkeys.get(i));
					bookrankAndsold.add(rankvalue);
				}

				//rankvalue를 redis에 저장시키기
				ValueOperations<String, List<String>> values = redisTemplate.opsForValue();
				//레디스 초기화 이후
				redisTemplate.getConnectionFactory().getConnection().flushAll();
				//랭크값 넣어주기
				values.set("rank",bookrankAndsold);

				return RepeatStatus.FINISHED;
			})
			.build();
	}

	@Bean
	public Step StepC() {
		return stepBuilderFactory.get("StepC")
			.tasklet((contribution, chunkContext) -> {
				log.info("Step3. 오늘 주문들 다 endFine로 true로 바꿔주기");

				for(Jumoon i : jumoons){
					i.endFine();
				}

				return RepeatStatus.FINISHED;
			})
			.build();
	}

	@Bean
	public Step StepD() {
		return stepBuilderFactory.get("StepD")
			.tasklet((contribution, chunkContext) -> {
				log.info("Step4. 재고량 채워 주기");

				//이 부분 Query DSL로 바꾸는게 좋을듯
				for (Long i: bookidkeys){
					Book book = bookRepository.findById(i).orElseThrow(
						()->new CustomException(ErrorCode.BOOK_NOT_FOUND)
					);
					if(book.getOutOfPrint() != 1){ // 절판이 아닐 때만 재고를 올려줌
						if(book.getInventory() == 0){ // 기존 상태가 품절이었으면 modifiedAt까지 함께 변경
							book.orderbook(20L);
						}else {
							book.batchBook(20L); // 품절이 아니었으면 재고만 변경
						}
					}
				}
				return RepeatStatus.FINISHED;
			})
			.build();
	}

}
