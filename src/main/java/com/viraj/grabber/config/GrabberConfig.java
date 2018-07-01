package com.viraj.grabber.config;

import com.viraj.grabber.model.UsageData;
import com.viraj.grabber.service.QueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * Manages all application related config. e.g. Nodes Count, Grab Interval etc.
 */
@Configuration
public class GrabberConfig {
	private final ApplicationArguments args;

	@Autowired
	public GrabberConfig(ApplicationArguments args) {
		this.args = args;
	}

	@Bean("nodesCount")
	public int nodesCount() {
		return Integer.parseInt(this.args.getSourceArgs()[0]);
	}

	@Bean("queueService")
	public QueueService queueService() {
		return new QueueService(new ConcurrentLinkedQueue<UsageData>());
	}

	@Bean("refreshInterval")
	public int refreshInterval() {
		return 5;
	}

	@Bean("refreshIntervalTimeUnit")
	public TimeUnit refreshIntervalTimeUnit() {
		return TimeUnit.SECONDS;
	}
}