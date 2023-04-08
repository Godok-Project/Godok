package com.baechu.batch;

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class BatchScheduler {

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private FakejumoonConfig fakejumoonConfig;

	@Autowired
	private CycleConfig cycleConfig;

	@Autowired
	private NoRankCycle noRankCycle;

	// @Scheduled(cron = "30 5 * * * *")
	// public void runJob(){
	//
	// 	//Job parameter 설정
	// 	Map<String, JobParameter> confMap = new HashMap<>();
	// 	confMap.put("time", new JobParameter(System.currentTimeMillis()));
	// 	JobParameters jobParameters = new JobParameters(confMap);
	//
	// 	try{
	// 		jobLauncher.run(fakejumoonConfig.job(), jobParameters);
	// 	}catch (JobExecutionAlreadyRunningException | JobInstanceAlreadyCompleteException |
	// 			JobParametersInvalidException
	// 			| org.springframework.batch.core.repository.JobRestartException e){
	// 		log.error(e.getMessage());
	// 	}
	// }
	//
	// @Scheduled(cron = "30 7 * * * *")
	// public void resetAndRank(){
	//
	// 	//Job parameter 설정
	// 	Map<String, JobParameter> confMap = new HashMap<>();
	// 	confMap.put("time", new JobParameter(System.currentTimeMillis()));
	// 	JobParameters jobParameters = new JobParameters(confMap);
	//
	// 	try{
	// 		jobLauncher.run(cycleConfig.jobs(), jobParameters);
	// 	}catch (JobExecutionAlreadyRunningException | JobInstanceAlreadyCompleteException | JobParametersInvalidException
	// 			| org.springframework.batch.core.repository.JobRestartException e){
	// 		log.error(e.getMessage());
	// 	}
	// }
}
