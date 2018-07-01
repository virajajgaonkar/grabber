package com.viraj.grabber.config;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import metrics_influxdb.InfluxdbReporter;
import metrics_influxdb.UdpInfluxdbProtocol;
import metrics_influxdb.api.measurements.KeyValueMetricMeasurementTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.TimeUnit;

/**
 * Manages all configuration related to Metrics/Stats Server.
 * For this assignment I am using StatsD/Influx DB using UDP Protocol. (Fire & Forget)
 */
@Configuration
public class MonitoringConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final String METRICS_HOST_VALUE_DEFAULT = "localhost";
	private static final int METRICS_PORT_VALUE_DEFAULT = 8293;
	private static final int METRICS_AGGREGATION_TIME_IN_SECS_DEFAULT = 60;

	@Autowired
	public MonitoringConfig() {
	}

	@Bean
	public String metricsHost() {
		LOGGER.info("metricsHost = {}", METRICS_HOST_VALUE_DEFAULT);
		return METRICS_HOST_VALUE_DEFAULT;
	}

	@Bean("metricsPort")
	public int metricsPort() {
		LOGGER.info("metricsPort = {}", METRICS_PORT_VALUE_DEFAULT);
		return METRICS_PORT_VALUE_DEFAULT;
	}

	@Bean
	public long metricsAggregationTimeInSecs() {
		LOGGER.info("metricsAggregationTimeInSecs = {}", METRICS_AGGREGATION_TIME_IN_SECS_DEFAULT);
		return METRICS_AGGREGATION_TIME_IN_SECS_DEFAULT;
	}

	@PostConstruct
	public void init() {
		ScheduledReporter reporter = InfluxdbReporter.forRegistry(metricRegistry())
				.protocol(new UdpInfluxdbProtocol(metricsHost(), metricsPort()))
				.convertRatesTo(TimeUnit.SECONDS)
				.convertDurationsTo(TimeUnit.MILLISECONDS)
				.filter(MetricFilter.ALL)
				.skipIdleMetrics(false)
				.transformer(new KeyValueMetricMeasurementTransformer())
				.build();
		reporter.start(metricsAggregationTimeInSecs(), TimeUnit.SECONDS);
	}

	@Bean
	public MetricRegistry metricRegistry() {
		MetricRegistry metricRegistry = new MetricRegistry();
		return metricRegistry;
	}
}