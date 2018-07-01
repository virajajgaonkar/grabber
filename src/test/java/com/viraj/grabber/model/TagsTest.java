package com.viraj.grabber.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TagsTest {
	@Test
	public void test() {
		assertEquals("abc", Tags.builder().build().toMetricName("abc"));
		assertEquals("k1.v1.k2.v2.abc", Tags.builder()
				.tag("k1", "v1")
				.tag("k2", "v2")
				.build().toMetricName("abc"));
	}

}