package com.viraj.grabber.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.util.sqlite.SQLiteDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

@Getter
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@Builder
public class UsageData {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public final static String TABLE_NAME = "usage_data";
	public final static Table<Record> TABLE = DSL.table(DSL.name(TABLE_NAME));

	public final static Field<Long> NODE_ID = DSL.field(DSL.name(TABLE_NAME, "node_id"), SQLiteDataType.BIGINT);
	private final long nodeId;

	public final static Field<Double> TIMESTAMP = DSL.field(DSL.name(TABLE_NAME, "timestamp"), SQLiteDataType.DOUBLE);
	private final double timestamp;

	public final static Field<Long> KB = DSL.field(DSL.name(TABLE_NAME, "kb"), SQLiteDataType.BIGINT);
	private final long kb;
}
