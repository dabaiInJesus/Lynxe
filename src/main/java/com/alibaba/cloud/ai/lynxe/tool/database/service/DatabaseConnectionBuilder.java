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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.cloud.ai.lynxe.tool.database.model.vo.DatasourceConfigVO;

/**
 * Service for building database connection configurations
 * Automatically determines driver class and builds URL from components
 */
@Service
public class DatabaseConnectionBuilder {

	private static final Logger log = LoggerFactory.getLogger(DatabaseConnectionBuilder.class);

	private final DataSourceService dataSourceService;

	public DatabaseConnectionBuilder(@Autowired(required = false) DataSourceService dataSourceService) {
		this.dataSourceService = dataSourceService;
	}

	/**
	 * Add datasource with automatic driver class detection
	 * If driverClassName is not provided, it will be determined from database type
	 */
	public void addDataSourceAuto(String name, String url, String username, String password, String type) {
		String driverClassName = null;

		if (url != null && !url.isEmpty()) {
			// Try to detect driver from URL pattern
			driverClassName = detectDriverFromUrl(url);
		}

		// If still not found, use type to look up
		if (driverClassName == null && type != null) {
			driverClassName = DatabaseDriverConstants.getDriverClass(type);
		}

		if (dataSourceService != null) {
			dataSourceService.addDataSource(name, url, username, password, driverClassName, type);
			log.info("Added datasource '{}' with auto-detected driver: {}", name, driverClassName);
		}
	}

	/**
	 * Build and add datasource from config object
	 * Extracts host, port, database from URL or builds URL from components
	 */
	public void buildAndAddDataSource(DatasourceConfigVO config) {
		String name = config.getName();
		String type = config.getType();
		String url = config.getUrl();
		String username = config.getUsername();
		String password = config.getPassword();
		String driverClassName = config.getDriverClassName();

		// If URL is not provided, try to build from components
		if ((url == null || url.isEmpty()) && type != null) {
			url = buildUrlFromConfig(config);
		}

		// If driver class is not provided, auto-detect
		if ((driverClassName == null || driverClassName.isEmpty())) {
			if (url != null && !url.isEmpty()) {
				driverClassName = detectDriverFromUrl(url);
			}
			if (driverClassName == null) {
				driverClassName = DatabaseDriverConstants.getDriverClass(type);
			}
		}

		if (dataSourceService != null) {
			dataSourceService.addDataSource(name, url, username, password, driverClassName, type);
			log.info("Built and added datasource '{}' with type: {}, driver: {}", name, type, driverClassName);
		}
	}

	/**
	 * Build URL from config components (host, port, database)
	 */
	public String buildUrlFromConfig(DatasourceConfigVO config) {
		String type = config.getType();
		String host = extractHostFromUrl(config.getUrl());
		Integer port = extractPortFromUrl(config.getUrl());
		String database = extractDatabaseFromUrl(config.getUrl());

		if (host == null) {
			host = "localhost";
		}

		return DatabaseDriverConstants.buildUrl(type, host, port, database);
	}

	/**
	 * Extract host from URL
	 */
	private String extractHostFromUrl(String url) {
		if (url == null) {
			return null;
		}

		// Pattern: //host:port or :port
		int hostStart = url.indexOf("//");
		if (hostStart < 0) {
			hostStart = 0;
		}
		else {
			hostStart += 2;
		}

		int hostEnd = url.indexOf(":", hostStart);
		if (hostEnd < 0) {
			hostEnd = url.indexOf("/", hostStart);
		}
		if (hostEnd < 0) {
			hostEnd = url.indexOf("?", hostStart);
		}
		if (hostEnd < 0) {
			hostEnd = url.length();
		}

		return url.substring(hostStart, hostEnd);
	}

	/**
	 * Extract port from URL
	 */
	private Integer extractPortFromUrl(String url) {
		if (url == null) {
			return null;
		}

		int portStart = url.indexOf(":", url.indexOf("//") + 2);
		if (portStart < 0) {
			return null;
		}
		portStart++;

		int portEnd = url.indexOf("/", portStart);
		if (portEnd < 0) {
			portEnd = url.indexOf("?", portStart);
		}
		if (portEnd < 0) {
			portEnd = url.length();
		}

		try {
			return Integer.parseInt(url.substring(portStart, portEnd));
		}
		catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * Extract database name from URL
	 */
	private String extractDatabaseFromUrl(String url) {
		if (url == null) {
			return null;
		}

		// Find database name after host:port
		int dbStart = url.indexOf("/", url.indexOf("//") + 2);
		if (dbStart < 0) {
			return null;
		}
		dbStart++;

		int dbEnd = url.indexOf("?", dbStart);
		if (dbEnd < 0) {
			dbEnd = url.indexOf("#", dbStart);
		}
		if (dbEnd < 0) {
			dbEnd = url.length();
		}

		String db = url.substring(dbStart, dbEnd);
		// URL decode if needed
		try {
			db = java.net.URLDecoder.decode(db, "UTF-8");
		}
		catch (Exception e) {
			// Ignore
		}
		return db;
	}

	/**
	 * Detect driver class from JDBC URL pattern
	 */
	private String detectDriverFromUrl(String url) {
		if (url == null || url.isEmpty()) {
			return null;
		}

		if (url.startsWith("jdbc:mysql://") || url.contains("mysql")) {
			return "com.mysql.cj.jdbc.Driver";
		}
		if (url.startsWith("jdbc:mariadb://")) {
			return "org.mariadb.jdbc.Driver";
		}
		if (url.startsWith("jdbc:postgresql://")) {
			return "org.postgresql.Driver";
		}
		if (url.startsWith("jdbc:oracle:thin:@")) {
			return "oracle.jdbc.OracleDriver";
		}
		if (url.startsWith("jdbc:sqlserver://")) {
			return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
		}
		if (url.startsWith("jdbc:h2:")) {
			return "org.h2.Driver";
		}
		if (url.startsWith("jdbc:clickhouse://")) {
			return "com.clickhouse.jdbc.ClickHouseDriver";
		}
		if (url.startsWith("jdbc:hive2://")) {
			return "org.apache.hive.jdbc.HiveDriver";
		}
		if (url.startsWith("jdbc:dm://")) {
			return "dm.jdbc.driver.DmDriver";
		}
		if (url.startsWith("jdbc:kingbase")) {
			return "com.kingbase8.Driver";
		}
		if (url.startsWith("jdbc:gaussdb://")) {
			return "com.huawei.gaussdb.jdbc.GaussDBDriver";
		}

		return null;
	}

	/**
	 * Validate connection config before adding
	 */
	public ValidationResult validateConfig(DatasourceConfigVO config) {
		ValidationResult result = new ValidationResult();

		String type = config.getType();
		String url = config.getUrl();
		String driverClassName = config.getDriverClassName();

		// Check type
		if (type != null && !DatabaseDriverConstants.isSupported(type)) {
			result.addError("Unsupported database type: " + type + ". Supported types: " + DatabaseDriverConstants.getSupportedTypes());
		}

		// Check URL format if provided
		if (url != null && !url.isEmpty() && type != null) {
			if (!DatabaseDriverConstants.validateUrl(type, url)) {
				result.addError("URL format doesn't match expected pattern for " + type + ": " + url);
			}
		}

		// Check driver class
		if (driverClassName != null && !driverClassName.isEmpty()) {
			if (!isValidDriverClass(driverClassName)) {
				result.addWarning("Unusual driver class name: " + driverClassName);
			}
		}

		return result;
	}

	/**
	 * Check if driver class name looks valid
	 */
	private boolean isValidDriverClass(String driverClassName) {
		return driverClassName != null && driverClassName.startsWith("com.")
				|| driverClassName.startsWith("org.") || driverClassName.startsWith("java.");
	}

	/**
	 * Validation result holder
	 */
	public static class ValidationResult {

		private final java.util.ArrayList<String> errors = new java.util.ArrayList<>();

		private final java.util.ArrayList<String> warnings = new java.util.ArrayList<>();

		public void addError(String error) {
			errors.add(error);
		}

		public void addWarning(String warning) {
			warnings.add(warning);
		}

		public boolean isValid() {
			return errors.isEmpty();
		}

		public java.util.List<String> getErrors() {
			return errors;
		}

		public java.util.List<String> getWarnings() {
			return warnings;
		}

		public String getMessage() {
			StringBuilder sb = new StringBuilder();
			if (!errors.isEmpty()) {
				sb.append("Errors:\n");
				for (String e : errors) {
					sb.append("  - ").append(e).append("\n");
				}
			}
			if (!warnings.isEmpty()) {
				sb.append("Warnings:\n");
				for (String w : warnings) {
					sb.append("  - ").append(w).append("\n");
				}
			}
			return sb.toString();
		}

	}

}
