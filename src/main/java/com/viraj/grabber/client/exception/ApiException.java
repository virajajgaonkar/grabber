package com.viraj.grabber.client.exception;

import com.google.common.io.ByteStreams;
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
public class ApiException extends Exception {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final long serialVersionUID = 3034460295874688949L;

	private final HttpStatus responseStatus;
	private final String errorMessage;

	public static ApiException getApiErrorMessage(HttpStatus responseStatus, InputStream stream) {
		try {
			String errorString = new String(ByteStreams.toByteArray(stream));
			return ApiException.builder().responseStatus(responseStatus).errorMessage(errorString).build();
		} catch (IOException e) {
			return ApiException.builder().responseStatus(responseStatus).errorMessage("IOException").build();
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
				LOGGER.warn("IOException trying to close stream!!", e);
			}
		}
	}
}