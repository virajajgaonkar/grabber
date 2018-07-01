package com.viraj.grabber.model;

import lombok.Builder;
import lombok.Data;

/**
 * Object to encapsulate DB Server Settings.
 */
@Data
@Builder(toBuilder = true)
public class DBSettings {
	private final String hostName;
	private final String userName;
	private final String password;
	private final String databaseName;
	private final String schemaName;
	private final int maxPoolSize;
	private final int minIdle;
	private final int idleTimeoutInMins;
}