package com.viraj.grabber.config;

import com.viraj.grabber.model.DBSettings;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.TimeUnit;

/**
 * Manages all configuration related to the database.
 */
@Configuration
public class DBConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	public static final String JDBC_URL_FMT = "jdbc:sqlite:%s";

	@Bean
	public DBSettings dbSettings(@Value("${db.host:usage_data.sqlite3}") String dbHost,
								 @Value("${db.name:usage_data}") String dbName,
								 @Value("${db.username:}") String dbUsername,
								 @Value("${db.password:}") String dbPassword,
								 @Value("${db.maxpoolsize:10}") int dbMaxPoolSize,
								 @Value("${db.minimumidle:1}") int dbMinimumIdle,
								 @Value("${db.idletimeoutinmins:5}") int dbIdleTimeoutInMins

	) {
		return DBSettings.builder()
				.hostName(System.getProperty("user.home") + "/" + dbHost)
				.databaseName(dbName)
				.userName(dbUsername)
				.password(dbPassword)
				.maxPoolSize(dbMaxPoolSize)
				.minIdle(dbMinimumIdle)
				.idleTimeoutInMins(dbIdleTimeoutInMins)
				.build();
	}

	@Bean
	public DSLContext dslContext(DBSettings dbSettings) {
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl(String.format(JDBC_URL_FMT, dbSettings.getHostName()));
		config.setUsername(dbSettings.getUserName());
		config.setPassword(dbSettings.getPassword());
		config.setAutoCommit(true);
		config.setMaximumPoolSize(dbSettings.getMaxPoolSize());
		config.setMinimumIdle(dbSettings.getMinIdle());
		config.setIdleTimeout(TimeUnit.MINUTES.toMillis(dbSettings.getIdleTimeoutInMins()));
		config.setMaxLifetime(1800000);

		DataSource dataSource = new HikariDataSource(config);
		DSLContext dslContext = DSL.using(dataSource, SQLDialect.SQLITE);
		return dslContext;

	}
}