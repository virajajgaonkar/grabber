package com.viraj.grabber.service;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Stopwatch;
import com.viraj.grabber.model.Tags;
import com.viraj.grabber.model.UsageData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class DataGrabConsumerService {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private final QueueService queueService;
	private final UsageDataService usageDataService;
	private final ThreadSyncService threadSyncService;
	private final MetricRegistry metricRegistry;
	private final HealthService healthService;

	@Autowired
	public DataGrabConsumerService(QueueService queueService, UsageDataService usageDataService,
								   ThreadSyncService threadSyncService, MetricRegistry metricRegistry, HealthService healthService) {
		this.queueService = queueService;
		this.usageDataService = usageDataService;
		this.threadSyncService = threadSyncService;
		this.metricRegistry = metricRegistry;
		this.healthService = healthService;
	}

	@Async
	public void doAsync(int batchSize) throws InterruptedException {
		LOGGER.info("DataGrabConsumerService thread is starting! batchSize = {}", batchSize);
		while (!threadSyncService.isShutdownDetected()) {
			Stopwatch sw = Stopwatch.createStarted();
			boolean success = true;
			try {
				List<UsageData> batch = queueService.getBatch(batchSize);
				if (!CollectionUtils.isEmpty(batch)) {
					usageDataService.bulkSaveUsageData(batch);
				}
				Thread.sleep(10);
			} catch (RuntimeException ex) {
				LOGGER.error("Encountered runtime exception!!", ex);
				success = false;
			} finally {
				//Save timing to metric
				Tags.TagsBuilder bldr = Tags.builder()
						.tag("success", String.valueOf(success))
						.tag("batchSize", String.valueOf(batchSize));
				metricRegistry.timer(bldr.build().toMetricName("stats.timer.dbwrite"))
						.update(sw.elapsed(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS);
				if(success){
					healthService.recordSuccessfulDBWrite();
				} else {
					healthService.recordFailedDBWrite();
				}
			}
		}
		LOGGER.info("DataGrabConsumerService thread is stopping!");
	}
}
