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
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.ValueOperations;

import com.baechu.book.entity.Book;
import com.baechu.book.repository.BookRepository;
import com.baechu.common.exception.CustomException;
import com.baechu.common.exception.ErrorCode;
import com.baechu.jumoon.entity.Jumoon;
import com.baechu.jumoon.repository.JumoonRepository;
import com.baechu.jumoon.service.JumoonService;
import com.baechu.member.entity.Member;
import com.baechu.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CycleConfig {

	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;

	private final BookRepository bookRepository;

	private final MemberRepository memberRepository;

	private final JumoonRepository jumoonRepository;

	private final JumoonService jumoonService;

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
			.on("FAILED")
			.end()
			.from(StepD())
			.on("*")
			.to(StepE())
			.end()
			.build();
		return job;
	}

	//필요한스텝
	//key 값들 분리하기, 주문 넣기, 랭킹 매기기,재고량 채워주기, Redis 초기화

	//key 별로 데이터 분리하기
	List<String> bookidkeys = new ArrayList<>();
	List<String> membersJumoonkeys = new ArrayList<>();
	List<Integer> rankYesterday = new ArrayList<>();

	@Bean
	public Step StepA() {
		return stepBuilderFactory.get("StepA")
			.tasklet((contribution, chunkContext) -> {
				log.info("Step1. key 값들 분리시키기");

				ScanOptions scanOptions = ScanOptions.scanOptions().match("*").build();
				Cursor<byte[]> keys = redisTemplate.getConnectionFactory().getConnection().scan(scanOptions);

				while (keys.hasNext()){
					String key = new String(keys.next());
					key = key.substring(7); //key를 가져올 때 경로까지 가져오고 이거를 byte에서 string으로 바꾸면서 앞에 쓸때 없는 정보가 붙는다, 이것을 제거해주어야한다
					System.out.println("key = " + key);
					if (key.charAt(0)==98){
						bookidkeys.add(key);
					} else if (key.equals("rank")) {
					} else {
						membersJumoonkeys.add(key);
					}

				}

				return RepeatStatus.FINISHED;
			})
			.build();
	}

	@Bean
	public Step StepB() {
		return stepBuilderFactory.get("StepB")
			.tasklet((contribution, chunkContext) -> {
				ValueOperations<String, List<String>> values = redisTemplate.opsForValue();
				log.info("Step2. 주문들 DB로 옮기기");

				for (String i : membersJumoonkeys){
					List<String> mj = values.get(i);
					Member member = memberRepository.findById(Long.valueOf(i)).orElseThrow(
						()->new CustomException(ErrorCode.MEMBER_NOT_FOUND)
					);
					for (String j : mj){
						String[] contents = j.split(",");
						Book book = bookRepository.findById(Long.valueOf(contents[1])).orElseThrow(
							()-> new CustomException(ErrorCode.BOOK_NOT_FOUND)
						);
						int quantity = Integer.parseInt(contents[2]);
						jumoonRepository.save(new Jumoon(member,book,quantity,contents[3]));
						Long bookinven = book.getInventory()-quantity;
						book.orderbook(bookinven);
					}
				}

				return RepeatStatus.FINISHED;
			})
			.build();
	}

	@Bean
	public Step StepC() {
		return stepBuilderFactory.get("StepC")
			.tasklet((contribution, chunkContext) -> {
				log.info("Step3. 랭킹 매기기");
				System.out.println("bookidkeys = " + bookidkeys);
				Map<Integer, Integer> rankmap = new HashMap<>();

				ValueOperations<String, String> values = redisTemplate.opsForValue();
				for (String i: bookidkeys){
					String temp = values.get(i).split(",")[0];
					rankmap.put(Integer.parseInt(i.substring(1)), Integer.valueOf(temp));
				}

				// 내림차순 key 값을 가지는 keySetList 생성
				List<Integer> keySetList = new ArrayList<>(rankmap.keySet());
				Collections.sort(keySetList, (o1, o2) -> (rankmap.get(o2).compareTo(rankmap.get(o1))));

				// 어제 주문된 책들이 8개 미만인 경우 1,2,3... 순으로 책으로 채워준다
				int cnt = 100;
				while (keySetList.size()<8){
					keySetList.add(cnt);
					cnt++;
				}

				//top8 bookid를 순서대로 rankYesterday에 넣어준다.
				for (int i = 0; i < 8; i++) {
					log.info("등수 매기기" + keySetList.get(i));
					System.out.println("등수 매기기" + keySetList.get(i));
					rankYesterday.add(keySetList.get(i));
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

				System.out.println("bookidkeys = " + bookidkeys);

				//이 부분 Query DSL로 바꾸는게 좋을듯
				for (String i: bookidkeys){
					log.info("i = " + i);
					log.info("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");
					Book book = bookRepository.findById(Long.valueOf(i.substring(1))).orElseThrow(
						()->new CustomException(ErrorCode.BOOK_NOT_FOUND)
					);
					log.info("book.getId() = " + book.getId());
					book.orderbook(20L);
				}
				log.info("Step4. FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");

				return RepeatStatus.FINISHED;
			})
			.build();
	}

	@Bean
	public Step StepE() {
		return stepBuilderFactory.get("StepE")
			.tasklet((contribution, chunkContext) -> {
				log.info("Step5. Redis초기화 시키고 랭킹기록 살리기");

				//레디스 초기화
				// redisTemplate.keys("*").stream().forEach(k->{redisTemplate.delete(k);});
				redisTemplate.getConnectionFactory().getConnection().flushAll();


				//레디스에 랭킹 기록 넣어주기
				ValueOperations<String, List<Integer>> values = redisTemplate.opsForValue();
				values.set("rank", rankYesterday);

				//레디스 오늘자 주문 id 초기화
				jumoonService.resetRedisBookId();

				return RepeatStatus.FINISHED;
			})
			.build();
	}

}
