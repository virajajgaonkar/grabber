package com.viraj.grabber.component;

import com.viraj.grabber.service.ThreadSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.TimeUnit;

@Component
public class ContextClosedHandler implements ApplicationListener<ContextClosedEvent> {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private final ThreadPoolTaskExecutor taskExecutor;
	private final ThreadSyncService threadSyncService;

	@Autowired
	public ContextClosedHandler(ThreadPoolTaskExecutor taskExecutor, ThreadSyncService threadSyncService) {
		this.taskExecutor = taskExecutor;
		this.threadSyncService = threadSyncService;
	}

	@Override
	public void onApplicationEvent(ContextClosedEvent event) {
		LOGGER.info("ContextClosedHandler.onApplicationEvent Staring");
		threadSyncService.setShutdownDetected(true);

		int retryCount = 0;
		while (taskExecutor.getActiveCount() > 0 && ++retryCount < 51) {
			try {
				LOGGER.info("Executer " + taskExecutor.getThreadNamePrefix() + " is still working with active " + taskExecutor.getActiveCount() + " work. Retry count is " + retryCount);
				Thread.sleep(TimeUnit.SECONDS.toMillis(10));
			} catch (InterruptedException e) {
				LOGGER.error("InterruptedException", e);
			}
		}
		if (!(retryCount < 51)) {
			LOGGER.warn("Executer {} is still working. Retry count {} exceeded max value. Thread count {} killed.", taskExecutor.getThreadNamePrefix(), retryCount, taskExecutor.getActiveCount());
		} else {
			LOGGER.info("Executer {} shutdown gracefully. Retry count {}. Threads count {} killed.", taskExecutor.getThreadNamePrefix(), retryCount, taskExecutor.getActiveCount());
		}
		taskExecutor.shutdown();
		LOGGER.info("Executer {} with active work {} have been killed", taskExecutor.getThreadNamePrefix(), taskExecutor.getActiveCount());
		LOGGER.info("ContextClosedHandler.onApplicationEvent Completed");
	}
}