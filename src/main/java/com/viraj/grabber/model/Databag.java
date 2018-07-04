package com.viraj.grabber.model;

import com.viraj.grabber.client.response.UsageDataHttpResult;
import lombok.Builder;
import lombok.Data;
import org.joda.time.DateTime;

import java.net.URI;

@Data
@Builder
public class Databag {
	private final URI uri;
	private DateTime previousFetchTime;
	private UsageDataHttpResult previousResult;

	public void updatePreviousResult(final UsageDataHttpResult previousResult){
		this.previousResult = previousResult;
		this.previousFetchTime = DateTime.now();
	}
}
