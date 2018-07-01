package com.viraj.grabber.repository;

import com.viraj.grabber.model.UsageData;
import org.jooq.BatchBindStep;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Optional;

/**
 * Repository encapsulates logic for UsageData & sqlite db.
 */
@Repository
public class UsageDataSqlRepository implements UsageDataRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private final DSLContext dslContext;

	@Autowired
	public UsageDataSqlRepository(DSLContext dslContext) {
		this.dslContext = dslContext;
	}

	public void saveUsageData(final UsageData usageData) {
		dslContext.insertInto(UsageData.TABLE, UsageData.NODE_ID, UsageData.TIMESTAMP, UsageData.KB)
				.values(usageData.getNodeId(), usageData.getTimestamp(), usageData.getKb())
				.execute();
	}

	public void bulkSaveUsageData(final List<UsageData> usageDataList) {
		BatchBindStep step = dslContext.batch(dslContext.insertInto(UsageData.TABLE, UsageData.NODE_ID, UsageData.TIMESTAMP, UsageData.KB)
				.values((Long) null, null, (Long) null));

		for (UsageData currentUsageData : usageDataList) {
			step = step.bind(currentUsageData.getNodeId(), currentUsageData.getTimestamp(), currentUsageData.getKb());
		}
		step.execute();
	}

	public Optional<UsageData> getLastUsageData(final int nodeId) {
		UsageData usageData = dslContext.selectOne().select(UsageData.NODE_ID, UsageData.TIMESTAMP, UsageData.KB)
				.from(UsageData.TABLE)
				.orderBy(UsageData.TIMESTAMP.desc())
				.fetchOne(record -> {
					return UsageData.builder()
							.nodeId(record.get(UsageData.NODE_ID))
							.timestamp(record.get(UsageData.TIMESTAMP))
							.kb(record.get(UsageData.KB)).build();
				});
		return Optional.ofNullable(usageData);
	}
}