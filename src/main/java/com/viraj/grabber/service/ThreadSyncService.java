package com.viraj.grabber.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;

/**
 * This class is used just to signal shutdown to all threads.
 * Once the shutdown is detected using isShutdownDetected(), all threads are expected to finish current task & exit the threads.
 */

@Service
public class ThreadSyncService {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private boolean shutdownDetected = false;

	public ThreadSyncService() {
	}

	public boolean isShutdownDetected() {
		return shutdownDetected;
	}

	public void setShutdownDetected(boolean shutdownDetected) {
		this.shutdownDetected = shutdownDetected;
	}
}