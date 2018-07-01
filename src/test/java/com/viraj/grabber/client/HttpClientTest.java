package com.viraj.grabber.client;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.viraj.grabber.client.exception.ApiException;
import com.viraj.grabber.client.exception.MalformedResponseException;
import com.viraj.grabber.client.response.UsageDataHttpResult;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.invoke.MethodHandles;
import java.net.URI;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
public class HttpClientTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final URI URI_CONNECTION_ERROR = URI.create("http://doesnotexist:9345/nodes/doesnotexist/usage");
	private static final URI URI_200_1 = URI.create("http://localhost:8081/nodes/200/usage");
	private static final URI URI_400 = URI.create("http://localhost:8081/nodes/400/usage");
	private static final URI URI_429 = URI.create("http://localhost:8081/nodes/429/usage");
	private static final URI URI_500 = URI.create("http://localhost:8081/nodes/500/usage");
	@Rule
	public WireMockRule wireMockRule = new WireMockRule(8081);
	@Mock
	private MetricRegistry metricRegistry;
	@Mock
	private Timer timer;
	@Mock
	private Counter counter;
	private HttpClient client;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		client = new HttpClient(metricRegistry);
		Mockito.when(metricRegistry.timer(Mockito.anyString())).thenReturn(timer);
		Mockito.when(metricRegistry.counter(Mockito.anyString())).thenReturn(counter);
		Mockito.doNothing().when(timer).update(Mockito.anyLong(), Mockito.any());
		Mockito.doNothing().when(counter).inc();
	}

	@Test
	public void test200() throws ApiException, MalformedResponseException {
		stubFor(get(urlPathMatching("/nodes/200/usage"))
				.willReturn(aResponse().withStatus(200).withBody("5,6,7")));
		UsageDataHttpResult actual = client.fetchUsageData(200, URI_200_1);
		UsageDataHttpResult expected = UsageDataHttpResult.builder().nodeId(200).timestamp(5).kb(6).build();
		assertEquals(expected, actual);
		verify(exactly(1), getRequestedFor(urlEqualTo("/nodes/200/usage")));
	}

	@Test()
	public void test400() throws MalformedResponseException {
		stubFor(get(urlPathMatching("/nodes/400/usage"))
				.willReturn(aResponse().withStatus(400).withBody("5,6,7")));
		UsageDataHttpResult actual = null;
		try {
			actual = client.fetchUsageData(400, URI_400);
			fail("ApiException expected!!");
		} catch (ApiException e) {
			LOGGER.info("ApiException expected!!");
			assertEquals(400, e.getResponseStatus().value());
		}
		verify(exactly(1), getRequestedFor(urlEqualTo("/nodes/400/usage")));
	}

	@Test()
	public void test429() throws MalformedResponseException {
		stubFor(get(urlPathMatching("/nodes/429/usage"))
				.willReturn(aResponse().withStatus(429).withBody("Too many requests!")));
		UsageDataHttpResult actual = null;
		try {
			actual = client.fetchUsageData(429, URI_429);
			fail("ApiException expected!!");
		} catch (ApiException e) {
			LOGGER.info("ApiException expected!!");
			assertEquals(429, e.getResponseStatus().value());
		}
		verify(exactly(5), getRequestedFor(urlEqualTo("/nodes/429/usage")));
	}

	@Test()
	public void test500() throws MalformedResponseException {
		stubFor(get(urlPathMatching("/nodes/500/usage"))
				.willReturn(aResponse().withStatus(500).withBody("Too many requests!")));
		UsageDataHttpResult actual = null;
		try {
			actual = client.fetchUsageData(500, URI_500);
			fail("ApiException expected!!");
		} catch (ApiException e) {
			LOGGER.info("ApiException expected!!");
			assertEquals(500, e.getResponseStatus().value());
		}
		verify(exactly(5), getRequestedFor(urlEqualTo("/nodes/500/usage")));
	}
}