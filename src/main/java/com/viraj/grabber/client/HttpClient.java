package com.viraj.grabber.client;

import com.codahale.metrics.MetricRegistry;
import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.base.Predicate;
import com.viraj.grabber.client.exception.ApiException;
import com.viraj.grabber.client.exception.MalformedResponseException;
import com.viraj.grabber.client.response.UsageDataHttpResult;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.net.ConnectException;
import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
public class HttpClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public static final Retryer<Response> DEFAULT_RETRYER = RetryerBuilder.<Response>newBuilder()
			.retryIfExceptionOfType(ConnectException.class)
			.retryIfException(RetryPredicate.INSTANCE)
			.withWaitStrategy(WaitStrategies.exponentialWait(100, 1, TimeUnit.MINUTES))
			.withStopStrategy(StopStrategies.stopAfterAttempt(5))
			.build();


	enum RetryPredicate implements Predicate<Throwable> {
		INSTANCE {
			@Override
			public boolean apply(@Nullable Throwable input) {
				if (input instanceof ApiException) {
					ApiException apiException = (ApiException) input;
					if (apiException.getResponseStatus().is5xxServerError()) {
						return true;
					}
					if (apiException.getResponseStatus() == HttpStatus.TOO_MANY_REQUESTS) {
						return true;
					}
				}
				return false;
			}
		};
	}

	private final MetricRegistry metricRegistry;

	@Autowired
	public HttpClient(MetricRegistry metricRegistry) {
		this.metricRegistry = metricRegistry;
	}

	/**
	 * Common function which processes
	 *
	 * @param callable Callable called by the Retryer
	 * @return List of Vehicle Ids
	 * @throws ApiException
	 */
	private UsageDataHttpResult process(final FetchUsageDataCallable callable) throws ApiException, MalformedResponseException {
		try (Response response = DEFAULT_RETRYER.call(callable);
			 InputStream stream = response.body().byteStream()) {
			return UsageDataHttpResult.parseUsageData(callable.getNodeId(), HttpStatus.valueOf(response.code()), stream);
		} catch (ExecutionException | RetryException e) {
			// The retryer returns either ExecutionException or RetryException
			// This wraps the real exception in it.
			// We check the cause & if the cause is of type ApiException, we throw ApiException & discard the wrapper
			if (e.getCause() instanceof ApiException) {
				throw (ApiException) e.getCause();
			}
			// If the cause is not of the type ApiException, we wrap it in RuntimeException.
			// I prefer this, as the exception is unexpected, we cannot handle it, so no point making it checked, also keeps interfaces clean.
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public UsageDataHttpResult fetchUsageData(long nodeId, URI uri) throws MalformedResponseException, ApiException {
		FetchUsageDataCallable callable = FetchUsageDataCallable.builder()
				.uri(uri).nodeId(nodeId).metricRegistry(metricRegistry).build();
		return process(callable);
	}
}