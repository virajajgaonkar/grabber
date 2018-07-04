package com.viraj.grabber.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.invoke.MethodHandles;

/**
 * Manages the configuration of the Thread Pool
 */
@Configuration
public class ThreadConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final int DEFAULT_PRODUCER_THREADS_COUNT = 600;

	@Bean("producerThreadsCount")
	public int producerThreadsCount(@Qualifier("nodesCount") int nodesCount) {
		int producerThreadsCount = (nodesCount >= DEFAULT_PRODUCER_THREADS_COUNT) ? DEFAULT_PRODUCER_THREADS_COUNT : nodesCount;
		LOGGER.info("producerThreadsCount = {}", producerThreadsCount);
		return producerThreadsCount;
	}

	@Bean(destroyMethod = "shutdown")
	public ThreadPoolTaskExecutor taskExecutor(@Qualifier("producerThreadsCount") int producerThreadsCount) {
		ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
		pool.setCorePoolSize(producerThreadsCount + 25);
		pool.setMaxPoolSize(producerThreadsCount + 25);
		pool.setWaitForTasksToCompleteOnShutdown(true);
		pool.afterPropertiesSet();
		return pool;
	}

}