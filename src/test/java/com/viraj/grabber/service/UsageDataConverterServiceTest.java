package com.viraj.grabber.service;

import com.viraj.grabber.client.response.UsageDataHttpResult;
import com.viraj.grabber.model.UsageData;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class UsageDataConverterServiceTest {
	public static final UsageDataHttpResult RESULT_1 = UsageDataHttpResult.builder().nodeId(1).timestamp(Double.parseDouble("1530480062233")).kb(Long.parseLong("1070678365938")).build();
	public static final UsageDataHttpResult RESULT_2 = UsageDataHttpResult.builder().nodeId(1).timestamp(Double.parseDouble("1530480064196")).kb(Long.parseLong("1070678369024")).build();
	public static final UsageDataHttpResult RESULT_3 = UsageDataHttpResult.builder().nodeId(1).timestamp(Double.parseDouble("1530480065966")).kb(Long.parseLong("1070678370820")).build();

	public static final UsageData EXPECTED_1 = UsageData.builder().nodeId(1).timestamp(Double.parseDouble("1530480064196")).kb(3086).build();
	public static final UsageData EXPECTED_2 = UsageData.builder().nodeId(1).timestamp(Double.parseDouble("1530480065966")).kb(1796).build();


	UsageDataConverterService converterService = new UsageDataConverterService();

	@Test
	public void duplicateEvents() {
		assertFalse(converterService.convert(RESULT_1, RESULT_1).isPresent());
	}

	@Test
	public void olderEvents() {
		assertFalse(converterService.convert(RESULT_2, RESULT_1).isPresent());
	}

	@Test
	public void validEvents() {
		assertEquals(EXPECTED_1, converterService.convert(RESULT_1, RESULT_2).get());
		assertEquals(EXPECTED_2, converterService.convert(RESULT_2, RESULT_3).get());
	}

}