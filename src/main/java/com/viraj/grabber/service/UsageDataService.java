package com.viraj.grabber.service;

import com.viraj.grabber.model.UsageData;
import com.viraj.grabber.repository.UsageDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Optional;

@Service
public class UsageDataService {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private final UsageDataRepository usageDataRepository;

	@Autowired
	public UsageDataService(UsageDataRepository usageDataRepository) {
		this.usageDataRepository = usageDataRepository;
	}

	public void saveUsageData(final UsageData usageData) {
		usageDataRepository.saveUsageData(usageData);
	}

	public void bulkSaveUsageData(final List<UsageData> usageDataList) {
		usageDataRepository.bulkSaveUsageData(usageDataList);
	}

	public Optional<UsageData> getLastUsageData(final int nodeId) {
		return usageDataRepository.getLastUsageData(nodeId);
	}
}