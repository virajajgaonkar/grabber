package com.viraj.grabber.service;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Stopwatch;
import com.viraj.grabber.client.HttpClient;
import com.viraj.grabber.client.exception.ApiException;
import com.viraj.grabber.client.exception.MalformedResponseException;
import com.viraj.grabber.client.response.UsageDataHttpResult;
import com.viraj.grabber.model.Tags;
import com.viraj.grabber.model.UsageData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class DataGrabProducerService {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final String URI_FORMAT = "http://localhost/nodes/%d/usage";
	private volatile AtomicLong count = new AtomicLong();
	private final Map<Long, UsageDataHttpResult> previousResults = new HashMap<>();
	private final int nodesCount;
	private final QueueService queueService;
	private final int refreshInterval;
	private final TimeUnit refreshIntervalTimeUnit;
	private final UsageDataConverterService usageDataConverterService;

	private final HttpClient httpClient;
	private final ThreadSyncService threadSyncService;
	private final MetricRegistry metricRegistry;
	private final HealthService healthService;

	@Autowired
	public DataGrabProducerService(QueueService queueService,
								   @Qualifier("nodesCount") int nodesCount,
								   int refreshInterval,
								   TimeUnit refreshIntervalTimeUnit,
								   UsageDataConverterService usageDataConverterService, HttpClient httpClient,
								   ThreadSyncService threadSyncService, MetricRegistry metricRegistry, HealthService healthService) {
		this.queueService = queueService;
		this.nodesCount = nodesCount;
		this.refreshInterval = refreshInterval;
		this.refreshIntervalTimeUnit = refreshIntervalTimeUnit;
		this.usageDataConverterService = usageDataConverterService;
		this.httpClient = httpClient;
		this.threadSyncService = threadSyncService;
		this.metricRegistry = metricRegistry;
		this.healthService = healthService;
	}

	private Optional<UsageDataHttpResult> getUsageData(long nodeId, final URI uri) {
		UsageDataHttpResult data = null;
		try {
			data = httpClient.fetchUsageData(nodeId, uri);
		} catch (MalformedResponseException | ApiException | RuntimeException e) {
			LOGGER.error("Exception!!", e);
			data = null;
		}
		return Optional.ofNullable(data);
	}

	private void sleep(Stopwatch sw, long nodeId, boolean success) throws InterruptedException {
		long elapsedTime = sw.elapsed(TimeUnit.MILLISECONDS);
		long refreshIntervalInMillis = refreshIntervalTimeUnit.toMillis(refreshInterval);

		Tags.TagsBuilder bldr = Tags.builder()
				.tag("nodeId", String.valueOf(nodeId))
				.tag("success", String.valueOf(success));
		metricRegistry.timer(bldr.build().toMetricName("stats.timer.fetchData"))
				.update(elapsedTime, TimeUnit.MILLISECONDS);

		if (elapsedTime == refreshIntervalInMillis) {
			if(success) {
				healthService.recordSuccessfulGrab(nodeId);
			}
			return;
		} else if (elapsedTime > refreshIntervalInMillis) {
			//Send additional delay alert timer
			if(success) {
				healthService.recordDelayedGrab(nodeId);
			}
			return;
		}
		if(success) {
			healthService.recordSuccessfulGrab(nodeId);
		}
		Thread.sleep(refreshIntervalInMillis - elapsedTime);
	}

	private long getNextNodeId() {
		long nextNodeId = (count.incrementAndGet() % nodesCount) + 1;
		return nextNodeId;
	}


	@Async
	public void doAsync() throws InterruptedException {
		LOGGER.info("DataGrabProducerService thread is starting!");

		while (!threadSyncService.isShutdownDetected()) {
			Stopwatch sw = Stopwatch.createStarted();
			boolean success = true;
			long nodeId = getNextNodeId();
			LOGGER.debug("nodeId = {}", nodeId);
			URI uri = URI.create(String.format(URI_FORMAT, nodeId));
			UsageDataHttpResult prevResult = previousResults.getOrDefault(nodeId, null);
			try {
				Optional<UsageDataHttpResult> optionalUsageDataHttpResult = getUsageData(nodeId, uri);
				if (!optionalUsageDataHttpResult.isPresent()) {
					success = false;
					continue;
				}
				UsageDataHttpResult currentResult = optionalUsageDataHttpResult.get();
				if (prevResult == null) {
					success = true;
					previousResults.put(nodeId, currentResult);
					continue;
				}

				Optional<UsageData> optionalUsageData = usageDataConverterService.convert(prevResult, currentResult);
				if (!optionalUsageData.isPresent()) {
					//Current Result is duplicate or older
					success = false;
					continue;
				}
				queueService.add(optionalUsageData.get());
				previousResults.put(nodeId, currentResult);
			} catch (RuntimeException ex) {
				LOGGER.error("Encountered runtime exception!!", ex);
				success = false;
			} finally {
				sleep(sw, nodeId, success);
			}
		}
		LOGGER.info("DataGrabProducerService thread is stopping!");
	}
}
