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

	@Bean(destroyMethod = "shutdown")
	public ThreadPoolTaskExecutor taskExecutor() {
		ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
		pool.setCorePoolSize(500 + 25);
		pool.setMaxPoolSize(500 + 25);
		pool.setWaitForTasksToCompleteOnShutdown(true);
		pool.afterPropertiesSet();
		return pool;
	}

}