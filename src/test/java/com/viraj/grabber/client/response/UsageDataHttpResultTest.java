package com.viraj.grabber.client.response;

import com.viraj.grabber.client.exception.MalformedResponseException;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;

public class UsageDataHttpResultTest {
	@Test(expected = MalformedResponseException.class)
	public void testEmptyResponse() throws IOException, MalformedResponseException {
		try (InputStream targetStream = new ByteArrayInputStream("".getBytes())) {
			UsageDataHttpResult.parseUsageData(1, HttpStatus.OK, targetStream);
		}
	}

	@Test(expected = MalformedResponseException.class)
	public void testPartialResponse() throws IOException, MalformedResponseException {
		try (InputStream targetStream = new ByteArrayInputStream("a".getBytes())) {
			UsageDataHttpResult.parseUsageData(1, HttpStatus.OK, targetStream);
		}
	}

	@Test(expected = MalformedResponseException.class)
	public void testInvalidResponse1() throws IOException, MalformedResponseException {
		try (InputStream targetStream = new ByteArrayInputStream("a,b".getBytes())) {
			UsageDataHttpResult.parseUsageData(1, HttpStatus.OK, targetStream);
		}
	}

	@Test(expected = MalformedResponseException.class)
	public void testInvalidResponse2() throws IOException, MalformedResponseException {
		try (InputStream targetStream = new ByteArrayInputStream("1,b".getBytes())) {
			UsageDataHttpResult.parseUsageData(1, HttpStatus.OK, targetStream);
		}
	}

	@Test(expected = MalformedResponseException.class)
	public void testInvalidResponse3() throws IOException, MalformedResponseException {
		try (InputStream targetStream = new ByteArrayInputStream("a,1".getBytes())) {
			UsageDataHttpResult.parseUsageData(1, HttpStatus.OK, targetStream);
		}
	}

	@Test()
	public void testValidResponse1() throws IOException, MalformedResponseException {
		try (InputStream targetStream = new ByteArrayInputStream("1,2".getBytes())) {
			UsageDataHttpResult expected = UsageDataHttpResult.builder().nodeId(1).timestamp(1).kb(2).build();
			UsageDataHttpResult actual = UsageDataHttpResult.parseUsageData(1, HttpStatus.OK, targetStream);
			assertEquals(expected, actual);
		}
	}

	@Test()
	public void testValidResponse2() throws IOException, MalformedResponseException {
		try (InputStream targetStream = new ByteArrayInputStream("1,2,3".getBytes())) {
			UsageDataHttpResult expected = UsageDataHttpResult.builder().nodeId(1).timestamp(1).kb(2).build();
			UsageDataHttpResult actual = UsageDataHttpResult.parseUsageData(1, HttpStatus.OK, targetStream);
			assertEquals(expected, actual);
		}
	}

	@Test()
	public void testValidResponse3() throws IOException, MalformedResponseException {
		try (InputStream targetStream = new ByteArrayInputStream("1530480062233,1070678365938,1047322270051".getBytes())) {
			UsageDataHttpResult expected = UsageDataHttpResult.builder().nodeId(1).timestamp(Double.parseDouble("1530480062233")).kb(Long.parseLong("1070678365938")).build();
			UsageDataHttpResult actual = UsageDataHttpResult.parseUsageData(1, HttpStatus.OK, targetStream);
			assertEquals(expected, actual);
		}

		try (InputStream targetStream = new ByteArrayInputStream("1530480064196,1070678369024,1047322270051".getBytes())) {
			UsageDataHttpResult expected = UsageDataHttpResult.builder().nodeId(1).timestamp(Double.parseDouble("1530480064196")).kb(Long.parseLong("1070678369024")).build();
			UsageDataHttpResult actual = UsageDataHttpResult.parseUsageData(1, HttpStatus.OK, targetStream);
			assertEquals(expected, actual);
		}

		try (InputStream targetStream = new ByteArrayInputStream("1530480065966,1070678370820,1047322270051".getBytes())) {
			UsageDataHttpResult expected = UsageDataHttpResult.builder().nodeId(1).timestamp(Double.parseDouble("1530480065966")).kb(Long.parseLong("1070678370820")).build();
			UsageDataHttpResult actual = UsageDataHttpResult.parseUsageData(1, HttpStatus.OK, targetStream);
			assertEquals(expected, actual);
		}
	}

}