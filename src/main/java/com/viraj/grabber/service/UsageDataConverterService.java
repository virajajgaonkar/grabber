package com.viraj.grabber.service;

import com.google.common.base.Preconditions;
import com.viraj.grabber.client.response.UsageDataHttpResult;
import com.viraj.grabber.model.UsageData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.Optional;

@Service
public class UsageDataConverterService {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public Optional<UsageData> convert(final UsageDataHttpResult prevResult, final UsageDataHttpResult currentResult) {
		Preconditions.checkNotNull(prevResult, "Input prevResult cannot be null!!");
		Preconditions.checkNotNull(currentResult, "Input currentResult cannot be null!!");
		if (currentResult.getTimestamp() <= prevResult.getTimestamp()) {
			LOGGER.warn("Received an older or duplicate data!");
			//Might want to add more stats
			return Optional.empty();
		}
		UsageData usageData = UsageData.builder()
				.nodeId(currentResult.getNodeId())
				.timestamp(currentResult.getTimestamp())
				.kb((int) (currentResult.getKb() - prevResult.getKb()))
				.build();
		return Optional.ofNullable(usageData);
	}
}
