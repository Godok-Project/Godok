package com.baechu.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.baechu.book.entity.Book;
import com.baechu.book.repository.BookRepository;
import com.baechu.common.exception.CustomException;
import com.baechu.common.exception.ErrorCode;
import com.baechu.jumoon.service.JumoonService;
import com.baechu.member.entity.Member;
import com.baechu.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class FakejumoonConfig {

	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private final BookRepository bookRepository;
	private final MemberRepository memberRepository;
	private final JumoonService jumoonService;

	@Bean
	public Job job() {
		Job job = jobBuilderFactory.get("job")
			.start(stepNextConditionalStepA())
			.on("FAILED")
			.end()
			.from(stepNextConditionalStepA())
			.on("*")
			.to(stepNextConditionalStepB())
			.on("FAILED")
			.end()
			.from(stepNextConditionalStepB())
			.on("*")
			.to(stepNextConditionalStepC())
			.on("*")
			.end()
			.end()
			.build();
		return job;
	}

	@Bean
	public Step stepNextConditionalStepA() {
		return stepBuilderFactory.get("stepNextConditionalStepA")
			.tasklet((contribution, chunkContext) -> {
				log.info(">>>>> 가짜 주문 생성에 들어갑니다");
				// ExitStatus에 따라 Flow가 진행된다.
				// contribution.setExitStatus(ExitStatus.FAILED);
				return RepeatStatus.FINISHED;
			})
			.build();
	}

	@Bean
	public Step stepNextConditionalStepB() {
		return stepBuilderFactory.get("stepNextConditionalStepB")
			.tasklet((contribution, chunkContext) -> {
				int num = 1001;
				int bookorder = 9;

				for (int i = 1; i < 9; i++) {
					Member member = memberRepository.findById(Long.valueOf(i)).orElseThrow(
						()-> new CustomException(ErrorCode.MEMBER_NOT_FOUND)
					);
					Book book = bookRepository.findById(Long.valueOf(num++)).orElseThrow(
						()-> new CustomException(ErrorCode.BOOK_NOT_FOUND)
					);
					jumoonService.fakebookorder(book,member,(bookorder--));
				}
				return RepeatStatus.FINISHED;
			})
			.build();
	}

	@Bean
	public Step stepNextConditionalStepC() {
		return stepBuilderFactory.get("stepNextConditionalStepC")
			.tasklet((contribution, chunkContext) -> {
				log.info(">>>>> 가짜 주문 생성이 끝났습니다");
				return RepeatStatus.FINISHED;
			})
			.build();
	}

}