package com.baechu.batch;

import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class BatchScheduler {

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private FakejumoonConfig fakejumoonConfig;

	@Autowired CycleConfig cycleConfig;


	// @Scheduled(cron = "0 0/10 * * * *")
	// @Scheduled(cron = "0 15 * * * *")
	// public void runJob(){
	//
	// 	//Job parameter 설정
	// 	Map<String, JobParameter> confMap = new HashMap<>();
	// 	confMap.put("time", new JobParameter(System.currentTimeMillis()));
	// 	JobParameters jobParameters = new JobParameters(confMap);
	//
	// 	try{
	// 		jobLauncher.run(fakejumoonConfig.job(), jobParameters);
	// 	}catch (JobExecutionAlreadyRunningException | JobInstanceAlreadyCompleteException | JobParametersInvalidException
	// 	| org.springframework.batch.core.repository.JobRestartException e){
	// 		log.error(e.getMessage());
	// 	}
	// }

	// @Scheduled(cron = "0 0 2 * * *")
	// @Scheduled(cron = "0 5,15,25,35,45,55 * * * *")
	// @Scheduled(cron = "30 15 * * * *")
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
