package com.viraj.grabber.service;

import com.viraj.grabber.model.UsageData;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class QueueServiceTest {
	private static UsageData buildUsageData(int i) {
		return UsageData.builder()
				.nodeId(i + 1)
				.timestamp((new Date()).getTime())
				.kb(i + 1).build();
	}

	private static List<UsageData> buildUsageDataList(int count) {
		List<UsageData> list = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			list.add(buildUsageData(i));
		}
		return list;
	}

	@Test
	public void test() {
		QueueService service = new QueueService(new ConcurrentLinkedQueue<UsageData>());
		Assert.assertEquals(service.getBatch(100).size(), 0);

		List<UsageData> list = buildUsageDataList(200);
		list.stream().forEach(i -> service.add(i));
		Assert.assertEquals(service.getBatch(200).size(), 200);
		Assert.assertEquals(service.getBatch(200).size(), 0);

		list = buildUsageDataList(100);
		list.stream().forEach(i -> service.add(i));
		Assert.assertEquals(service.getBatch(200).size(), 100);
		Assert.assertEquals(service.getBatch(200).size(), 0);

		list = buildUsageDataList(200);
		list.stream().forEach(i -> service.add(i));
		Assert.assertEquals(service.getBatch(100).size(), 100);
		Assert.assertEquals(service.getBatch(100).size(), 100);
		Assert.assertEquals(service.getBatch(200).size(), 0);
	}

}