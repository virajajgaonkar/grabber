package com.viraj.grabber.client.response;

import com.google.common.io.ByteStreams;
import com.viraj.grabber.client.exception.MalformedResponseException;
import lombok.Builder;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;

@Data
@Builder
public class UsageDataHttpResult {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final long nodeId;
	private final double timestamp;
	private final long kb;

	public static UsageDataHttpResult parseUsageData(long nodeId, HttpStatus responseStatus, InputStream stream) throws MalformedResponseException {
		String responseString = null;
		try {
			responseString = new String(ByteStreams.toByteArray(stream));
			LOGGER.debug("{}", responseString);
			String[] parts = responseString.split(",");
			if ((parts == null) || (parts.length < 2)) {
				throw MalformedResponseException.builder().nodeId(nodeId).responseStatus(responseStatus).responseString(responseString).build();
			}
			return UsageDataHttpResult.builder().nodeId(nodeId).timestamp(Double.parseDouble(parts[0])).kb(Long.parseLong(parts[1])).build();
		} catch (IOException | NumberFormatException e) {
			throw MalformedResponseException.builder().nodeId(nodeId).responseStatus(responseStatus).responseString(responseString).build();
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
				LOGGER.warn("IOException trying to close stream!!", e);
			}
		}
	}

}
