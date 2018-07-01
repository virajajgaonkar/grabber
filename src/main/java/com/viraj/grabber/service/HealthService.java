package com.viraj.grabber.service;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Getter
public class HealthService {
	@Data
	@Builder
	static final class NodeStatus {
		private long successfulGrabs;
		private long delayedGrabs;
		private long totalGrabs;
		private boolean currentlyFailing;
	}

	@Data
	@Builder(toBuilder = true)
	static final class DBWriteStatus {
		private long successfulWrites;
		private long failedWrites;
		private long totalWrites;
		private boolean currentlyFailing;
	}

	private final Map<Long, NodeStatus> nodeStatuses = new HashMap<>();
	private DBWriteStatus dbWriteStatus = DBWriteStatus.builder().build();
	private final int nodesCount;

	@Autowired
	public HealthService(@Qualifier("nodesCount") int nodesCount) {
		this.nodesCount = nodesCount;
	}

	public void recordSuccessfulGrab(long nodeId) {
		if (!nodeStatuses.containsKey(nodeId)) {
			nodeStatuses.put(nodeId, NodeStatus.builder().build());
		}
		NodeStatus status = nodeStatuses.get(nodeId);
		status.setCurrentlyFailing(false);
		status.setTotalGrabs(status.getTotalGrabs() + 1);
		status.setSuccessfulGrabs(status.getSuccessfulGrabs() + 1);
	}

	public void recordDelayedGrab(long nodeId) {
		if (!nodeStatuses.containsKey(nodeId)) {
			nodeStatuses.put(nodeId, NodeStatus.builder().build());
		}
		NodeStatus status = nodeStatuses.get(nodeId);
		status.setCurrentlyFailing(true);
		status.setTotalGrabs(status.getTotalGrabs() + 1);
		status.setDelayedGrabs(status.getDelayedGrabs() + 1);
	}

	public void recordSuccessfulDBWrite() {
		dbWriteStatus.setCurrentlyFailing(false);
		dbWriteStatus.setTotalWrites(dbWriteStatus.getTotalWrites() + 1);
		dbWriteStatus.setSuccessfulWrites(dbWriteStatus.getSuccessfulWrites() + 1);
	}

	public void recordFailedDBWrite() {
		dbWriteStatus.setCurrentlyFailing(true);
		dbWriteStatus.setTotalWrites(dbWriteStatus.getTotalWrites() + 1);
		dbWriteStatus.setFailedWrites(dbWriteStatus.getFailedWrites() + 1);
	}

	@Data
	@Builder(toBuilder = true)
	public static final class Summary {
		private final Integer totalNodesCount;
		private final Integer nodesCountCurrentlySuccessful;
		private final Integer nodesCountCurrentlyFailing;
		private final Boolean dbWritesCurrentlyFailing;
		private final Integer nodesCountNeverFetched;
	}

	public Summary getSummary() {
		int nodesCountCurrentlySuccessful = 0;
		int nodesCountCurrentlyFailing = 0;
		boolean dbWritesCurrentlyFailing = dbWriteStatus.isCurrentlyFailing();
		int nodesCountNeverFetched = 0;
		for (long i = 1; i <= nodesCount; i++) {
			if (!nodeStatuses.containsKey(i)) {
				nodesCountNeverFetched++;
				continue;
			}
			if (nodeStatuses.get(i).currentlyFailing) {
				nodesCountCurrentlyFailing++;
			} else {
				nodesCountCurrentlySuccessful++;
			}
		}
		return Summary.builder()
				.totalNodesCount(nodesCount)
				.nodesCountCurrentlySuccessful(nodesCountCurrentlySuccessful)
				.nodesCountCurrentlyFailing(nodesCountCurrentlyFailing)
				.dbWritesCurrentlyFailing(dbWritesCurrentlyFailing)
				.nodesCountNeverFetched(nodesCountNeverFetched).build();
	}
}