package com.baechu.batch;

import java.time.LocalDateTime;
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

import com.baechu.jumoon.entity.Jumoon;
import com.baechu.jumoon.repository.JumoonRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class NoRankCycle {

	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private final JumoonRepository jumoonRepository;
	private final RedisTemplate redisTemplate;

	@Bean
	public Job norankjobs() {
		Job job = jobBuilderFactory.get("job")
			.start(StepA1())
			.on("FAILED")
			.end()
			.from(StepA1())
			.on("*")
			.to(StepB2())
			.end()
			.build();
		return job;
	}

	List<Long> bookidkeys = new ArrayList<>();
	List<Jumoon> jumoons = new ArrayList<>();

	@Bean
	public Step StepA1() {
		return stepBuilderFactory.get("StepA")
			.tasklet((contribution, chunkContext) -> {
				log.info("Step1. Jumoontable에서 어제팔린책 가져오기");

				//오늘이 8일 17시 라면 7일 02시 부터 8일 02시 까지 팔린 책 가져와야함
				//오늘이 8일 1시 라면 6일 02시 부터 7일 02시 까지 팔린 책 가져와야함
				LocalDateTime now = LocalDateTime.now();

				LocalDateTime starttime;
				LocalDateTime endtime;

				if (now.getHour()>=2){
					LocalDateTime ytd = now.minusDays(1);
					starttime = LocalDateTime.of(ytd.getYear(),ytd.getMonth(), ytd.getDayOfMonth(), ytd.getHour(), 0);
					endtime = LocalDateTime.of(now.getYear(),now.getMonth(),now.getDayOfMonth(), now.getHour(), 0);
				}else {
					LocalDateTime yytd = now.minusDays(2);
					LocalDateTime ytd = now.minusDays(1);
					starttime = LocalDateTime.of(yytd.getYear(),yytd.getMonth(), yytd.getDayOfMonth(), yytd.getHour(), 0);
					endtime = LocalDateTime.of(ytd.getYear(),ytd.getMonth(), ytd.getDayOfMonth(), ytd.getHour(), 0);
				}
				jumoons = jumoonRepository.findAllByJumoonatBetween(starttime,endtime);

				return RepeatStatus.FINISHED;
			})
			.build();
	}

	@Bean
	public Step StepB2() {
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

}
