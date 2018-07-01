package com.viraj.grabber.client;

import com.codahale.metrics.MetricRegistry;
import com.viraj.grabber.client.exception.ApiException;
import com.viraj.grabber.model.Tags;
import lombok.Builder;
import lombok.Data;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@Data
@Builder
public class FetchUsageDataCallable implements Callable<Response> {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
			.connectTimeout(2, TimeUnit.MINUTES)
			.readTimeout(2, TimeUnit.MINUTES)
			.writeTimeout(2, TimeUnit.MINUTES)
			.build();

	private final long nodeId;
	private final URI uri;
	private final MetricRegistry metricRegistry;

	@Override
	public Response call() throws Exception {
		LOGGER.debug("uri = {}", uri);

		Request.Builder builder = new Request.Builder()
				.url(uri.toURL())
				.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE).get();
		Response response = HTTP_CLIENT.newCall(builder.build()).execute();
		HttpStatus status = HttpStatus.valueOf(response.code());
		LOGGER.debug("Request = {}, Response Code = {}", uri, status);
		//Send Metric
		Tags.TagsBuilder bldr = Tags.builder()
				.tag("nodeId", String.valueOf(nodeId))
				.tag("httpStatus", String.valueOf(status.value()))
				.tag("httpStatusFamily", status.series().toString());
		metricRegistry.counter(bldr.build().toMetricName("stats.counter.fetchUsageData"))
				.inc();
		if (status.is2xxSuccessful()) {
			return response;
		}
		throw ApiException.getApiErrorMessage(status, response.body().byteStream());
	}
}