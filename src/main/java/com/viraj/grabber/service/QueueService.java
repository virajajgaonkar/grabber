package com.viraj.grabber.service;

import com.viraj.grabber.model.UsageData;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * Encapsulates the Queue Logic.
 * Ideally this should be an Interface & the implementation should be LocalQueueService.
 */
public class QueueService {
	private final Queue<UsageData> queue;

	@Autowired
	public QueueService(Queue<UsageData> queue) {
		this.queue = queue;
	}

	public void add(UsageData usageData) {
		queue.add(usageData);
	}

	public List<UsageData> getBatch(int batchSize) {
		List<UsageData> usageDataList = new ArrayList<>();
		for (int i = 0; i < batchSize; i++) {
			UsageData usageData = queue.poll();
			if (usageData == null) {
				return usageDataList;
			}
			usageDataList.add(usageData);
		}
		return usageDataList;
	}
}