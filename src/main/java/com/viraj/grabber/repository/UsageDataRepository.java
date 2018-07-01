package com.viraj.grabber.repository;

import com.viraj.grabber.model.UsageData;

import java.util.List;
import java.util.Optional;

public interface UsageDataRepository {
	void saveUsageData(final UsageData usageData);

	void bulkSaveUsageData(final List<UsageData> usageDataList);

	Optional<UsageData> getLastUsageData(final int nodeId);
}