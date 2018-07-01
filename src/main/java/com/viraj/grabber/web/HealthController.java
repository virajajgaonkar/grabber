package com.viraj.grabber.web;

import com.viraj.grabber.service.HealthService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.lang.invoke.MethodHandles;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
public class HealthController {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private final HealthService healthService;

	@Autowired
	public HealthController(HealthService healthService) {
		this.healthService = healthService;
	}

	@ApiOperation(value = "Health Check", response = HealthService.Summary.class)
	@ApiResponses(
			value = {
					@ApiResponse(code = 200, message = "Health Check Successful!!")
			}
	)
	@RequestMapping(path = "/health", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<HealthService.Summary> checkHealth() {
		HealthService.Summary summary = healthService.getSummary();

		return new ResponseEntity<HealthService.Summary>(summary, HttpStatus.OK);
	}
}