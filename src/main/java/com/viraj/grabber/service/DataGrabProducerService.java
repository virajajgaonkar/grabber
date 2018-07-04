package com.viraj.grabber.service;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Stopwatch;
import com.viraj.grabber.client.HttpClient;
import com.viraj.grabber.client.exception.ApiException;
import com.viraj.grabber.client.exception.MalformedResponseException;
import com.viraj.grabber.client.response.UsageDataHttpResult;
import com.viraj.grabber.model.Databag;
import com.viraj.grabber.model.Tags;
import com.viraj.grabber.model.UsageData;
import org.joda.time.DateTime;
import org.joda.time.Interval;
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
	private final Map<Long, Databag> previousResults = new HashMap<>();
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

	private long getNextNodeId() {
		long nextNodeId = (count.incrementAndGet() % nodesCount) + 1;
		return nextNodeId;
	}

	private static final boolean DELAYED = true;
	private static final boolean NOT_DELAYED = false;

	private boolean isDelayed(DateTime prevResultsTime, long nodeId) throws InterruptedException {
		if (prevResultsTime == null) {
			return NOT_DELAYED;
		}
		DateTime currentTime = DateTime.now();
		Interval interval = new Interval(prevResultsTime, currentTime);
		long elapsedTime = interval.toDurationMillis();
		long refreshIntervalInMillis = refreshIntervalTimeUnit.toMillis(refreshInterval);

		if (elapsedTime > refreshIntervalInMillis) {
			//Send additional delay alert timer
			healthService.recordDelayedGrab(nodeId);
			LOGGER.debug("Fetch Delayed {}", nodeId);
			return DELAYED;
		}
		LOGGER.debug("Node Id {} Sleeping for {}", nodeId, (refreshIntervalInMillis - elapsedTime));
		Thread.sleep(refreshIntervalInMillis - elapsedTime);
		return NOT_DELAYED;
	}


	@Async
	public void doAsync() throws InterruptedException {
		LOGGER.info("DataGrabProducerService thread is starting!");

		while (!threadSyncService.isShutdownDetected()) {
			Stopwatch sw = Stopwatch.createStarted();
			boolean success = true;
			long nodeId = getNextNodeId();
			LOGGER.debug("nodeId = {}", nodeId);

			Databag bag = previousResults.getOrDefault(nodeId, Databag.builder().uri(URI.create(String.format(URI_FORMAT, nodeId))).build());
			UsageDataHttpResult prevResult = bag.getPreviousResult();

			try {
				isDelayed(bag.getPreviousFetchTime(), nodeId);
				Optional<UsageDataHttpResult> optionalUsageDataHttpResult = getUsageData(nodeId, bag.getUri());
				if (!optionalUsageDataHttpResult.isPresent()) {
					success = false;
					continue;
				}
				UsageDataHttpResult currentResult = optionalUsageDataHttpResult.get();
				bag.updatePreviousResult(currentResult);
				if (prevResult == null) {
					success = true;
					previousResults.put(nodeId, bag);
					continue;
				}

				Optional<UsageData> optionalUsageData = usageDataConverterService.convert(prevResult, currentResult);
				if (!optionalUsageData.isPresent()) {
					//Current Result is duplicate or older
					success = false;
					continue;
				}
				queueService.add(optionalUsageData.get());
				previousResults.put(nodeId, bag);
			} catch (RuntimeException ex) {
				LOGGER.error("Encountered runtime exception!!", ex);
				success = false;
			} finally {
				if (success) {
					healthService.recordSuccessfulGrab(nodeId);
				}
				Tags.TagsBuilder bldr = Tags.builder()
						.tag("nodeId", String.valueOf(nodeId))
						.tag("success", String.valueOf(success));
				metricRegistry.timer(bldr.build().toMetricName("stats.timer.fetchData"))
						.update(sw.elapsed(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS);
			}
		}
		LOGGER.info("DataGrabProducerService thread is stopping!");
	}
}
