package com.viraj.grabber.service;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class HealthServiceTest {

	@Test
	public void test() {
		HealthService service = new HealthService(5);
		//1 Only Successful
		service.recordSuccessfulGrab(1);

		//2 Only Delayed
		service.recordDelayedGrab(2);

		//3 S F S
		service.recordSuccessfulGrab(3);
		service.recordDelayedGrab(3);
		service.recordSuccessfulGrab(3);

		//4 F S F
		service.recordDelayedGrab(4);
		service.recordSuccessfulGrab(4);
		service.recordDelayedGrab(4);

		assertNotNull(service.getNodeStatuses());
		assertEquals(4, service.getNodeStatuses().size());

		assertNotNull(service.getNodeStatuses().get(1L));
		assertEquals(1, service.getNodeStatuses().get(1L).getTotalGrabs());
		assertEquals(1, service.getNodeStatuses().get(1L).getSuccessfulGrabs());
		assertEquals(0, service.getNodeStatuses().get(1L).getDelayedGrabs());
		assertEquals(false, service.getNodeStatuses().get(1L).isCurrentlyFailing());

		assertNotNull(service.getNodeStatuses().get(2L));
		assertEquals(1, service.getNodeStatuses().get(2L).getTotalGrabs());
		assertEquals(0, service.getNodeStatuses().get(2L).getSuccessfulGrabs());
		assertEquals(1, service.getNodeStatuses().get(2L).getDelayedGrabs());
		assertEquals(true, service.getNodeStatuses().get(2L).isCurrentlyFailing());

		assertNotNull(service.getNodeStatuses().get(3L));
		assertEquals(3, service.getNodeStatuses().get(3L).getTotalGrabs());
		assertEquals(2, service.getNodeStatuses().get(3L).getSuccessfulGrabs());
		assertEquals(1, service.getNodeStatuses().get(3L).getDelayedGrabs());
		assertEquals(false, service.getNodeStatuses().get(3L).isCurrentlyFailing());

		assertNotNull(service.getNodeStatuses().get(4L));
		assertEquals(3, service.getNodeStatuses().get(4L).getTotalGrabs());
		assertEquals(1, service.getNodeStatuses().get(4L).getSuccessfulGrabs());
		assertEquals(2, service.getNodeStatuses().get(4L).getDelayedGrabs());
		assertEquals(true, service.getNodeStatuses().get(4L).isCurrentlyFailing());

		HealthService.Summary actual = service.getSummary();
		HealthService.Summary expected1 = HealthService.Summary.builder()
				.dbWritesCurrentlyFailing(false)
				.nodesCountCurrentlyFailing(2)
				.nodesCountCurrentlySuccessful(2)
				.nodesCountNeverFetched(1)
				.totalNodesCount(5).build();
		assertEquals(expected1, actual);
		HealthService.DBWriteStatus dbWriteStatus1 = HealthService.DBWriteStatus.builder()
				.currentlyFailing(false)
				.totalWrites(0).successfulWrites(0).failedWrites(0).build();
		assertEquals(service.getDbWriteStatus(), dbWriteStatus1);


		//DB Writes failed
		service.recordFailedDBWrite();
		actual = service.getSummary();
		HealthService.Summary expected2 = expected1.toBuilder().dbWritesCurrentlyFailing(true).build();
		assertEquals(expected2, actual);
		HealthService.DBWriteStatus dbWriteStatus2 = HealthService.DBWriteStatus.builder()
				.currentlyFailing(true)
				.totalWrites(1).successfulWrites(0).failedWrites(1).build();
		assertEquals(service.getDbWriteStatus(), dbWriteStatus2);


		//DB Writes recovered
		service.recordSuccessfulDBWrite();
		actual = service.getSummary();
		assertEquals(expected1, actual);
		HealthService.DBWriteStatus dbWriteStatus3 = HealthService.DBWriteStatus.builder()
				.currentlyFailing(false)
				.totalWrites(2).successfulWrites(1).failedWrites(1).build();
		assertEquals(service.getDbWriteStatus(), dbWriteStatus3);
	}

}