/*
 * Copyright 2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.ai.lynxe.tool.database.service;

/**
 * Database configuration constants
 */
public class DatabaseConfigConstants {

	// Configuration prefix
	public static final String CONFIG_PREFIX = "database_use.datasource.";

	// Configuration property names
	public static final String PROP_TYPE = "type";

	public static final String PROP_ENABLE = "enable";

	public static final String PROP_URL = "url";

	public static final String PROP_DRIVER_CLASS_NAME = "driver-class-name";

	public static final String PROP_USERNAME = "username";

	public static final String PROP_PASSWORD = "password";

	// Configuration values
	public static final String ENABLE_TRUE = "true";

	public static final String ENABLE_FALSE = "false";

	// Supported database types
	public static final String DB_TYPE_MYSQL = "mysql";

	public static final String DB_TYPE_POSTGRESQL = "postgresql";

	public static final String DB_TYPE_ORACLE = "oracle";

	public static final String DB_TYPE_SQLSERVER = "sqlserver";

	public static final String DB_TYPE_H2 = "h2";

	public static final String DB_TYPE_CLICKHOUSE = "clickhouse";

	public static final String DB_TYPE_DORIS = "doris";

	public static final String DB_TYPE_HIVE = "hive";

	public static final String DB_TYPE_HBASE = "hbase";

	public static final String DB_TYPE_ELASTICSEARCH = "elasticsearch";

	public static final String DB_TYPE_OCEANBASE = "oceanbase";

	public static final String DB_TYPE_DAMENG = "dameng";

	public static final String DB_TYPE_KINGBASE = "kingbase";

	public static final String DB_TYPE_GAUSSDB = "gaussdb";

	public static final String DB_TYPE_MARIADB = "mariadb";

	private DatabaseConfigConstants() {
		// Utility class, instantiation prohibited
	}

}
