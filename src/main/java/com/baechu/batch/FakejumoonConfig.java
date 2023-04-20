package com.baechu.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
				long[] rbs = {2415062,387099,886063,1820350,1957841,1984839,1984355,21504};
				int[] quantities = {348,302,284,274,210,195,140,121};

				for (int i = 1; i < 9; i++) {
					Member member = memberRepository.findById(Long.valueOf(i)).orElseThrow(
						()-> new CustomException(ErrorCode.MEMBER_NOT_FOUND)
					);
					int ranq = quantities[i-1]+((int)(Math.random()-0.5)*15);
					System.out.println(rbs[i-1]+", "+ranq);
					jumoonService.testbookorder(rbs[i-1],ranq,member);
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