package com.viraj.grabber.model;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import lombok.Builder;
import lombok.Singular;

import java.util.Map;

/**
 * Key value metric tags.
 */
@Builder(toBuilder = true)
public class Tags {
	@Singular
	private final Map<String, String> tags;

	private Tags(Map<String, String> tags) {
		this.tags = tags;
	}

	public String toMetricName(String metricName) {
		String tagStr = Joiner.on('.').withKeyValueSeparator(".").join(tags);
		if (!Strings.isNullOrEmpty(tagStr)) {
			return Joiner.on('.').join(tagStr, metricName);
		} else {
			return metricName;
		}
	}
}