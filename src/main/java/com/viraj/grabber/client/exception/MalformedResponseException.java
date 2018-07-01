package com.viraj.grabber.client.exception;

import lombok.Builder;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.lang.invoke.MethodHandles;

@Data
@Builder
public class MalformedResponseException extends Exception {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final long serialVersionUID = -6273191715572029523L;

	private final long nodeId;
	private final HttpStatus responseStatus;
	private final String responseString;
}
