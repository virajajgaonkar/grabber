package com.viraj.grabber.component;

import com.viraj.grabber.service.DataGrabConsumerService;
import com.viraj.grabber.service.DataGrabProducerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.TimeUnit;

@Component
public class AppRuner implements CommandLineRunner {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final DataGrabProducerService dataGrabProducerService;
	private final DataGrabConsumerService dataGrabConsumerService;

	@Autowired
	public AppRuner(DataGrabProducerService dataGrabProducerService, DataGrabConsumerService dataGrabConsumerService) {
		this.dataGrabProducerService = dataGrabProducerService;
		this.dataGrabConsumerService = dataGrabConsumerService;
	}

	@Override
	public void run(String... args) throws Exception {
		LOGGER.info("AppRunner Starting");

		dataGrabConsumerService.doAsync(999);

		for (int i = 0; i < 500; ++i) {
			LOGGER.info("AppRunner Starting Async for thread {}", i);
			dataGrabProducerService.doAsync();
			Thread.sleep(TimeUnit.MILLISECONDS.toMillis(5));
		}

		LOGGER.info("AppRunner Ending");
	}
}