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
package com.alibaba.cloud.ai.lynxe.tool.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.alibaba.cloud.ai.lynxe.tool.database.service.DatabaseDriverConstants;

/**
 * Unit tests for DatabaseDriverConstants
 */
public class DatabaseDriverConstantsTest {

	@Test
	void testGetDriverClass() {
		assertEquals("com.mysql.cj.jdbc.Driver", DatabaseDriverConstants.getDriverClass("mysql"));
		assertEquals("org.postgresql.Driver", DatabaseDriverConstants.getDriverClass("postgresql"));
		assertEquals("oracle.jdbc.OracleDriver", DatabaseDriverConstants.getDriverClass("oracle"));
		assertEquals("com.microsoft.sqlserver.jdbc.SQLServerDriver", DatabaseDriverConstants.getDriverClass("sqlserver"));
		assertEquals("org.h2.Driver", DatabaseDriverConstants.getDriverClass("h2"));
		assertEquals("com.clickhouse.jdbc.ClickHouseDriver", DatabaseDriverConstants.getDriverClass("clickhouse"));
		assertEquals("dm.jdbc.driver.DmDriver", DatabaseDriverConstants.getDriverClass("dm"));
		assertEquals("com.kingbase8.Driver", DatabaseDriverConstants.getDriverClass("kingbase"));
		assertEquals("com.huawei.gaussdb.jdbc.GaussDBDriver", DatabaseDriverConstants.getDriverClass("gaussdb"));
	}

	@Test
	void testGetDriverClassCaseInsensitive() {
		assertEquals("com.mysql.cj.jdbc.Driver", DatabaseDriverConstants.getDriverClass("MySQL"));
		assertEquals("com.mysql.cj.jdbc.Driver", DatabaseDriverConstants.getDriverClass("MYSQL"));
		assertEquals("org.postgresql.Driver", DatabaseDriverConstants.getDriverClass("PostgreSQL"));
	}

	@ParameterizedTest
	@NullAndEmptySource
	void testGetDriverClassWithNullOrEmpty(String databaseType) {
		assertNull(DatabaseDriverConstants.getDriverClass(databaseType));
	}

	@Test
	void testGetUrlPattern() {
		assertNotNull(DatabaseDriverConstants.getUrlPattern("mysql"));
		assertTrue(DatabaseDriverConstants.getUrlPattern("mysql").contains("jdbc:mysql://"));
		assertTrue(DatabaseDriverConstants.getUrlPattern("mysql").contains("{host}"));
		assertTrue(DatabaseDriverConstants.getUrlPattern("mysql").contains("{port}"));
		assertTrue(DatabaseDriverConstants.getUrlPattern("mysql").contains("{database}"));
	}

	@ParameterizedTest
	@ValueSource(strings = { "mysql", "postgresql", "oracle", "sqlserver", "h2", "clickhouse", "doris", "hive",
			"oceanbase", "dameng", "kingbase", "gaussdb" })
	void testAllMajorDatabasesHaveUrlPattern(String databaseType) {
		assertNotNull(DatabaseDriverConstants.getUrlPattern(databaseType),
				"URL pattern should exist for: " + databaseType);
	}

	@Test
	void testGetDefaultPort() {
		assertEquals(Integer.valueOf(3306), DatabaseDriverConstants.getDefaultPort("mysql"));
		assertEquals(Integer.valueOf(5432), DatabaseDriverConstants.getDefaultPort("postgresql"));
		assertEquals(Integer.valueOf(1521), DatabaseDriverConstants.getDefaultPort("oracle"));
		assertEquals(Integer.valueOf(1433), DatabaseDriverConstants.getDefaultPort("sqlserver"));
		assertEquals(Integer.valueOf(8123), DatabaseDriverConstants.getDefaultPort("clickhouse"));
		assertEquals(Integer.valueOf(5236), DatabaseDriverConstants.getDefaultPort("dameng"));
		assertEquals(Integer.valueOf(54321), DatabaseDriverConstants.getDefaultPort("kingbase"));
	}

	@ParameterizedTest
	@NullAndEmptySource
	void testGetDefaultPortWithNullOrEmpty(String databaseType) {
		assertNull(DatabaseDriverConstants.getDefaultPort(databaseType));
	}

	@Test
	void testBuildUrl() {
		String url = DatabaseDriverConstants.buildUrl("mysql", "localhost", 3306, "testdb");
		assertEquals("jdbc:mysql://localhost:3306/testdb?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true", url);
	}

	@Test
	void testBuildUrlWithNullHost() {
		String url = DatabaseDriverConstants.buildUrl("mysql", null, 3306, "testdb");
		assertTrue(url.contains("localhost"));
		assertTrue(url.contains("3306"));
		assertTrue(url.contains("testdb"));
	}

	@Test
	void testBuildUrlWithNullPort() {
		String url = DatabaseDriverConstants.buildUrl("mysql", "localhost", null, "testdb");
		assertTrue(url.contains("localhost"));
		assertTrue(url.contains("3306")); // Default MySQL port
		assertTrue(url.contains("testdb"));
	}

	@Test
	void testBuildUrlForAllMajorDatabases() {
		assertNotNull(DatabaseDriverConstants.buildUrl("mysql", "localhost", 3306, "db"));
		assertNotNull(DatabaseDriverConstants.buildUrl("postgresql", "localhost", 5432, "db"));
		assertNotNull(DatabaseDriverConstants.buildUrl("oracle", "localhost", 1521, "db"));
		assertNotNull(DatabaseDriverConstants.buildUrl("sqlserver", "localhost", 1433, "db"));
		assertNotNull(DatabaseDriverConstants.buildUrl("clickhouse", "localhost", 8123, "db"));
		assertNotNull(DatabaseDriverConstants.buildUrl("hive", "localhost", 10000, "db"));
		assertNotNull(DatabaseDriverConstants.buildUrl("dameng", "localhost", 5236, "db"));
		assertNotNull(DatabaseDriverConstants.buildUrl("kingbase", "localhost", 54321, "db"));
		assertNotNull(DatabaseDriverConstants.buildUrl("gaussdb", "localhost", 5432, "db"));
	}

	@Test
	void testValidateUrl() {
		assertTrue(DatabaseDriverConstants.validateUrl("mysql", "jdbc:mysql://localhost:3306/testdb"));
		assertTrue(DatabaseDriverConstants.validateUrl("postgresql", "jdbc:postgresql://localhost:5432/testdb"));
		assertTrue(DatabaseDriverConstants.validateUrl("oracle", "jdbc:oracle:thin:@localhost:1521:orcl"));
		assertTrue(DatabaseDriverConstants.validateUrl("sqlserver", "jdbc:sqlserver://localhost:1433;databaseName=testdb"));
		assertTrue(DatabaseDriverConstants.validateUrl("h2", "jdbc:h2:mem:testdb"));
		assertTrue(DatabaseDriverConstants.validateUrl("clickhouse", "jdbc:clickhouse://localhost:8123/testdb"));
		assertTrue(DatabaseDriverConstants.validateUrl("hive", "jdbc:hive2://localhost:10000/testdb"));
	}

	@Test
	void testValidateUrlInvalid() {
		assertFalse(DatabaseDriverConstants.validateUrl("mysql", "invalid-url"));
		assertFalse(DatabaseDriverConstants.validateUrl(null, "jdbc:mysql://localhost:3306/testdb"));
		assertFalse(DatabaseDriverConstants.validateUrl("mysql", null));
	}

	@Test
	void testIsSupported() {
		assertTrue(DatabaseDriverConstants.isSupported("mysql"));
		assertTrue(DatabaseDriverConstants.isSupported("postgresql"));
		assertTrue(DatabaseDriverConstants.isSupported("oracle"));
		assertTrue(DatabaseDriverConstants.isSupported("sqlserver"));
		assertTrue(DatabaseDriverConstants.isSupported("h2"));
		assertTrue(DatabaseDriverConstants.isSupported("clickhouse"));
		assertTrue(DatabaseDriverConstants.isSupported("doris"));
		assertTrue(DatabaseDriverConstants.isSupported("hive"));
		assertTrue(DatabaseDriverConstants.isSupported("oceanbase"));
		assertTrue(DatabaseDriverConstants.isSupported("dameng"));
		assertTrue(DatabaseDriverConstants.isSupported("kingbase"));
		assertTrue(DatabaseDriverConstants.isSupported("gaussdb"));
	}

	@ParameterizedTest
	@ValueSource(strings = { "unknown", "mongodb", "redis", "elasticsearch" })
	void testIsNotSupported(String databaseType) {
		assertFalse(DatabaseDriverConstants.isSupported(databaseType));
	}

	@Test
	void testGetSupportedTypes() {
		Set<String> types = DatabaseDriverConstants.getSupportedTypes();
		assertNotNull(types);
		assertFalse(types.isEmpty());
		assertTrue(types.contains("mysql"));
		assertTrue(types.contains("postgresql"));
		assertTrue(types.contains("oracle"));
		assertTrue(types.contains("sqlserver"));
		assertTrue(types.contains("h2"));
		assertTrue(types.contains("clickhouse"));
		assertTrue(types.contains("doris"));
		assertTrue(types.contains("hive"));
		assertTrue(types.contains("oceanbase"));
		assertTrue(types.contains("dameng"));
		assertTrue(types.contains("kingbase"));
		assertTrue(types.contains("gaussdb"));
	}

	@Test
	void testAliases() {
		assertEquals(DatabaseDriverConstants.getDriverClass("doris"),
				DatabaseDriverConstants.getDriverClass("apache_doris"));
		assertEquals(DatabaseDriverConstants.getDriverClass("hive"),
				DatabaseDriverConstants.getDriverClass("apache_hive"));
		assertEquals(DatabaseDriverConstants.getDriverClass("oceanbase"), DatabaseDriverConstants.getDriverClass("ob"));
		assertEquals(DatabaseDriverConstants.getDriverClass("dameng"), DatabaseDriverConstants.getDriverClass("dm"));
		assertEquals(DatabaseDriverConstants.getDriverClass("kingbase"), DatabaseDriverConstants.getDriverClass("kb"));
		assertEquals(DatabaseDriverConstants.getDriverClass("gaussdb"), DatabaseDriverConstants.getDriverClass("gauss"));
	}

	@Test
	void testDriverClassMapIsComplete() {
		// Verify that all entries in DRIVER_CLASS_MAP have corresponding URL patterns
		Set<String> driverTypes = DatabaseDriverConstants.getSupportedTypes();
		for (String type : driverTypes) {
			assertNotNull(DatabaseDriverConstants.getUrlPattern(type),
					"URL pattern should exist for driver type: " + type);
			assertNotNull(DatabaseDriverConstants.getDefaultPort(type),
					"Default port should exist for driver type: " + type);
		}
	}

}
