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
			.start(StepZero())
			.on("FAILED")
			.end()
			.on("*")
			.to(StepA())
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
	public Step StepZero() {
		return stepBuilderFactory.get("StepZero")
			.tasklet((contribution, chunkContext) -> {
				log.info("Step1. RedisServer와 연동되는지 확인하기");
				log.info("만약에 연동이 안된다면 StepZero에서 멈추고 아무것도 안한다.");
				log.info("일단 주문들은 취소가능한 상태(주문 미완료)로 두고 다음날 02시에 재시도를 한다.");
				log.info("StepA에서 fine가 false인 책들만 가져와서 쌓인 주문들도 처리하고 쌓인 책들로 한번에 랭킹을 만들어 줄 것이다.");
				log.info("Service로직에서는 서킷 브레이커를 통해 redis에 접속이 되면 ranking을 만들고 안되면 추천 책만 main에서 보여줄 것이다.");

				ValueOperations<String, List<String>> values = redisTemplate.opsForValue();
				if (values.get("rank") == null){
					log.info("랭킹 데이터가 없습니다.");
				}else {
					log.info("랭킹 데이터가 있습니다.");
				}

				return RepeatStatus.FINISHED;
			})
			.build();
	}

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
				if (bookidkeys.size()<8){
					long[] rbs = {2415062,387099,886063,1820350,1957841,1984839,1984355,21504};
					int cnt = 0;

					while (bookidkeys.size()<8){
						bookidkeys.add(rbs[cnt]);
						soldbooks.put(rbs[cnt],0);
						cnt++;
					}
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
					jumoonRepository.save(i);
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

				for (Long i: bookidkeys){
					Book book = bookRepository.findById(i).orElseThrow(
						()->new CustomException(ErrorCode.BOOK_NOT_FOUND)
					);
					if(book.getOutOfPrint() != 1){ // 절판이 아닐 때만 재고를 올려줌
						if(book.getInventory() == 0){ // 기존 상태가 품절이었으면 modifiedAt까지 함께 변경
							book.inventoryChangeBook(20L);
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
