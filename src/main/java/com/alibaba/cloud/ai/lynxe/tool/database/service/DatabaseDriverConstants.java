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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Database driver and URL configuration constants
 * Maps database types to their JDBC driver class names and URL patterns
 */
public class DatabaseDriverConstants {

	private DatabaseDriverConstants() {
		// Utility class
	}

	// Database type to JDBC driver class name mapping
	public static final Map<String, String> DRIVER_CLASS_MAP = new HashMap<>();

	static {
		// MySQL
		DRIVER_CLASS_MAP.put("mysql", "com.mysql.cj.jdbc.Driver");
		DRIVER_CLASS_MAP.put("mariadb", "org.mariadb.jdbc.Driver");

		// PostgreSQL
		DRIVER_CLASS_MAP.put("postgresql", "org.postgresql.Driver");

		// Oracle
		DRIVER_CLASS_MAP.put("oracle", "oracle.jdbc.OracleDriver");

		// SQL Server
		DRIVER_CLASS_MAP.put("sqlserver", "com.microsoft.sqlserver.jdbc.SQLServerDriver");

		// H2
		DRIVER_CLASS_MAP.put("h2", "org.h2.Driver");

		// ClickHouse
		DRIVER_CLASS_MAP.put("clickhouse", "com.clickhouse.jdbc.ClickHouseDriver");

		// Apache Doris (uses MySQL protocol)
		DRIVER_CLASS_MAP.put("doris", "com.mysql.cj.jdbc.Driver");
		DRIVER_CLASS_MAP.put("apache_doris", "com.mysql.cj.jdbc.Driver");

		// Apache Hive
		DRIVER_CLASS_MAP.put("hive", "org.apache.hive.jdbc.HiveDriver");
		DRIVER_CLASS_MAP.put("apache_hive", "org.apache.hive.jdbc.HiveDriver");

		// OceanBase (MySQL compatible)
		DRIVER_CLASS_MAP.put("oceanbase", "com.mysql.cj.jdbc.Driver");
		DRIVER_CLASS_MAP.put("ob", "com.mysql.cj.jdbc.Driver");

		// 达梦 (DM Database)
		DRIVER_CLASS_MAP.put("dameng", "dm.jdbc.driver.DmDriver");
		DRIVER_CLASS_MAP.put("dm", "dm.jdbc.driver.DmDriver");

		// 人大金仓 (KingBase)
		DRIVER_CLASS_MAP.put("kingbase", "com.kingbase8.Driver");
		DRIVER_CLASS_MAP.put("kb", "com.kingbase8.Driver");

		// GaussDB
		DRIVER_CLASS_MAP.put("gaussdb", "com.huawei.gaussdb.jdbc.GaussDBDriver");
		DRIVER_CLASS_MAP.put("gauss", "com.huawei.gaussdb.jdbc.GaussDBDriver");
	}

	// URL pattern placeholders for each database type
	// {host}, {port}, {database} are placeholders that will be replaced
	public static final Map<String, String> URL_PATTERN_MAP = new HashMap<>();

	static {
		// MySQL
		URL_PATTERN_MAP.put("mysql", "jdbc:mysql://{host}:{port}/{database}?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");
		URL_PATTERN_MAP.put("mariadb", "jdbc:mariadb://{host}:{port}/{database}");

		// PostgreSQL
		URL_PATTERN_MAP.put("postgresql", "jdbc:postgresql://{host}:{port}/{database}");

		// Oracle (thin mode)
		URL_PATTERN_MAP.put("oracle", "jdbc:oracle:thin:@{host}:{port}:{database}");

		// SQL Server
		URL_PATTERN_MAP.put("sqlserver", "jdbc:sqlserver://{host}:{port};databaseName={database}");

		// H2 (embedded/file mode)
		URL_PATTERN_MAP.put("h2", "jdbc:h2:mem:{database};DB_CLOSE_DELAY=-1;MODE=MySQL");

		// ClickHouse
		URL_PATTERN_MAP.put("clickhouse", "jdbc:clickhouse://{host}:{port}/{database}");

		// Apache Doris
		URL_PATTERN_MAP.put("doris", "jdbc:mysql://{host}:{port}/{database}?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");
		URL_PATTERN_MAP.put("apache_doris", "jdbc:mysql://{host}:{port}/{database}?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");

		// Apache Hive
		URL_PATTERN_MAP.put("hive", "jdbc:hive2://{host}:{port}/{database}");
		URL_PATTERN_MAP.put("apache_hive", "jdbc:hive2://{host}:{port}/{database}");

		// OceanBase
		URL_PATTERN_MAP.put("oceanbase", "jdbc:mysql://{host}:{port}/{database}?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");
		URL_PATTERN_MAP.put("ob", "jdbc:mysql://{host}:{port}/{database}?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");

		// 达梦 (DM Database)
		URL_PATTERN_MAP.put("dameng", "jdbc:dm://{host}:{port}/{database}");
		URL_PATTERN_MAP.put("dm", "jdbc:dm://{host}:{port}/{database}");

		// 人大金仓 (KingBase)
		URL_PATTERN_MAP.put("kingbase", "jdbc:kingbase8://{host}:{port}/{database}");
		URL_PATTERN_MAP.put("kb", "jdbc:kingbase8://{host}:{port}/{database}");

		// GaussDB
		URL_PATTERN_MAP.put("gaussdb", "jdbc:gaussdb://{host}:{port}/{database}");
		URL_PATTERN_MAP.put("gauss", "jdbc:gaussdb://{host}:{port}/{database}");
	}

	// Default ports for each database type
	public static final Map<String, Integer> DEFAULT_PORT_MAP = new HashMap<>();

	static {
		DEFAULT_PORT_MAP.put("mysql", 3306);
		DEFAULT_PORT_MAP.put("mariadb", 3306);
		DEFAULT_PORT_MAP.put("postgresql", 5432);
		DEFAULT_PORT_MAP.put("oracle", 1521);
		DEFAULT_PORT_MAP.put("sqlserver", 1433);
		DEFAULT_PORT_MAP.put("h2", 9092);
		DEFAULT_PORT_MAP.put("clickhouse", 8123);
		DEFAULT_PORT_MAP.put("doris", 9030);
		DEFAULT_PORT_MAP.put("apache_doris", 9030);
		DEFAULT_PORT_MAP.put("hive", 10000);
		DEFAULT_PORT_MAP.put("apache_hive", 10000);
		DEFAULT_PORT_MAP.put("oceanbase", 2883);
		DEFAULT_PORT_MAP.put("ob", 2883);
		DEFAULT_PORT_MAP.put("dameng", 5236);
		DEFAULT_PORT_MAP.put("dm", 5236);
		DEFAULT_PORT_MAP.put("kingbase", 54321);
		DEFAULT_PORT_MAP.put("kb", 54321);
		DEFAULT_PORT_MAP.put("gaussdb", 5432);
		DEFAULT_PORT_MAP.put("gauss", 5432);
	}

	// Regex patterns for validating connection URLs
	public static final Map<String, Pattern> URL_REGEX_MAP = new HashMap<>();

	static {
		// MySQL pattern: jdbc:mysql://host:port/database
		URL_REGEX_MAP.put("mysql",
				Pattern.compile("jdbc:mysql://.+:\\d+/.*"));
		URL_REGEX_MAP.put("mariadb",
				Pattern.compile("jdbc:mariadb://.+:\\d+/.*"));
		URL_REGEX_MAP.put("postgresql",
				Pattern.compile("jdbc:postgresql://.+:\\d+/.*"));
		URL_REGEX_MAP.put("oracle",
				Pattern.compile("jdbc:oracle:thin:@.+:\\d+:.*"));
		URL_REGEX_MAP.put("sqlserver",
				Pattern.compile("jdbc:sqlserver://.+:\\d+.*"));
		URL_REGEX_MAP.put("h2",
				Pattern.compile("jdbc:h2:.*"));
		URL_REGEX_MAP.put("clickhouse",
				Pattern.compile("jdbc:clickhouse://.+:\\d+.*"));
		URL_REGEX_MAP.put("doris",
				Pattern.compile("jdbc:mysql://.+:\\d+/.*"));
		URL_REGEX_MAP.put("hive",
				Pattern.compile("jdbc:hive2://.+:\\d+.*"));
		URL_REGEX_MAP.put("oceanbase",
				Pattern.compile("jdbc:mysql://.+:\\d+/.*"));
		URL_REGEX_MAP.put("dameng",
				Pattern.compile("jdbc:dm://.+:\\d+/.*"));
		URL_REGEX_MAP.put("kingbase",
				Pattern.compile("jdbc:kingbase8://.+:\\d+.*"));
		URL_REGEX_MAP.put("gaussdb",
				Pattern.compile("jdbc:gaussdb://.+:\\d+.*"));
	}

	/**
	 * Get driver class name for database type
	 */
	public static String getDriverClass(String databaseType) {
		if (databaseType == null) {
			return null;
		}
		return DRIVER_CLASS_MAP.get(databaseType.toLowerCase().trim());
	}

	/**
	 * Get URL pattern for database type
	 */
	public static String getUrlPattern(String databaseType) {
		if (databaseType == null) {
			return null;
		}
		return URL_PATTERN_MAP.get(databaseType.toLowerCase().trim());
	}

	/**
	 * Get default port for database type
	 */
	public static Integer getDefaultPort(String databaseType) {
		if (databaseType == null) {
			return null;
		}
		return DEFAULT_PORT_MAP.get(databaseType.toLowerCase().trim());
	}

	/**
	 * Build connection URL from components
	 */
	public static String buildUrl(String databaseType, String host, Integer port, String database) {
		String pattern = getUrlPattern(databaseType);
		if (pattern == null) {
			return null;
		}

		int actualPort = port != null ? port : (getDefaultPort(databaseType) != null ? getDefaultPort(databaseType) : 0);

		return pattern
				.replace("{host}", host != null ? host : "localhost")
				.replace("{port}", String.valueOf(actualPort))
				.replace("{database}", database != null ? database : "");
	}

	/**
	 * Validate URL matches expected pattern for database type
	 */
	public static boolean validateUrl(String databaseType, String url) {
		if (databaseType == null || url == null) {
			return false;
		}

		Pattern pattern = URL_REGEX_MAP.get(databaseType.toLowerCase().trim());
		if (pattern == null) {
			// If no pattern defined, allow any URL
			return true;
		}
		return pattern.matcher(url).matches();
	}

	/**
	 * Check if database type is supported
	 */
	public static boolean isSupported(String databaseType) {
		if (databaseType == null) {
			return false;
		}
		return DRIVER_CLASS_MAP.containsKey(databaseType.toLowerCase().trim());
	}

	/**
	 * Get all supported database types
	 */
	public static java.util.Set<String> getSupportedTypes() {
		return DRIVER_CLASS_MAP.keySet();
	}

}
