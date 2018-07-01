package com.viraj.grabber.repository;

import com.google.common.base.Stopwatch;
import com.viraj.grabber.model.UsageData;
import org.jooq.DSLContext;
import org.jooq.util.sqlite.SQLiteDataType;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@Import(value = {UsageDataSqlRepository.class, DBTestConfig.class})
public class UsageDataSqlRepositoryTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final int BATCH_SIZE = 10;

	@Autowired
	private UsageDataSqlRepository usageDataRepository;
	@Autowired
	@Qualifier("getTempFile")
	private File tempFile;

	@Autowired
	public DSLContext dslContext;

	@Before
	public void setup() {
		dslContext.createTableIfNotExists(UsageData.TABLE_NAME)
				.column(UsageData.NODE_ID.getName(), SQLiteDataType.INTEGER)
				.column(UsageData.TIMESTAMP.getName(), SQLiteDataType.REAL)
				.column(UsageData.KB.getName(), SQLiteDataType.INTEGER).execute();

	}

	@AfterClass
	public static void teardown() throws IOException {
	}

	@Test
	public void test1() {
		Stopwatch watch = Stopwatch.createStarted();
		for (int j = 1; j <= BATCH_SIZE; j++) {
			float timeStamp = System.currentTimeMillis();
			for (int i = 1; i <= BATCH_SIZE; i++) {
				UsageData data = UsageData.builder()
						.nodeId(i).timestamp(timeStamp).kb(j).build();
				usageDataRepository.saveUsageData(data);
			}
		}
		watch.stop();
		LOGGER.info("Elapsed time = {}", watch.elapsed(TimeUnit.MILLISECONDS));
	}

	@Test
	public void test2() {
		Stopwatch watch = Stopwatch.createStarted();
		for (int j = 1; j <= BATCH_SIZE; j++) {
			float timeStamp = System.currentTimeMillis();
			List<UsageData> usageDataList = new ArrayList<>();
			for (int i = 1; i <= BATCH_SIZE; i++) {
				UsageData data = UsageData.builder()
						.nodeId(i).timestamp(timeStamp).kb(j).build();
				usageDataList.add(data);
			}
			usageDataRepository.bulkSaveUsageData(usageDataList);
		}
		watch.stop();
		LOGGER.info("Elapsed time = {}", watch.elapsed(TimeUnit.MILLISECONDS));
	}
}