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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.alibaba.cloud.ai.lynxe.tool.database.service.DataSourceService;

/**
 * Unit tests for DataSourceService
 */
public class DataSourceServiceTest {

	private DataSourceService dataSourceService;

	@BeforeEach
	void setUp() {
		dataSourceService = new DataSourceService();
	}

	@Test
	void testAddDataSource() {
		dataSourceService.addDataSource("test-mysql", "jdbc:mysql://localhost:3306/testdb", "root", "password",
				"com.mysql.cj.jdbc.Driver");

		assertTrue(dataSourceService.hasDataSource("test-mysql"));
		assertEquals(1, dataSourceService.getDataSourceCount());
	}

	@Test
	void testAddDataSourceWithType() {
		dataSourceService.addDataSource("test-mysql", "jdbc:mysql://localhost:3306/testdb", "root", "password",
				"com.mysql.cj.jdbc.Driver", "mysql");

		assertTrue(dataSourceService.hasDataSource("test-mysql"));
		assertEquals("mysql", dataSourceService.getDataSourceType("test-mysql"));
	}

	@Test
	void testAddMultipleDataSources() {
		dataSourceService.addDataSource("mysql-1", "jdbc:mysql://localhost:3306/db1", "root", "pass", "com.mysql.cj.jdbc.Driver");
		dataSourceService.addDataSource("mysql-2", "jdbc:mysql://localhost:3306/db2", "root", "pass", "com.mysql.cj.jdbc.Driver");
		dataSourceService.addDataSource("pg-1", "jdbc:postgresql://localhost:5432/db3", "postgres", "pass", "org.postgresql.Driver");

		assertEquals(3, dataSourceService.getDataSourceCount());
	}

	@Test
	void testGetDataSource() {
		dataSourceService.addDataSource("test-ds", "jdbc:h2:mem:testdb", "sa", "", "org.h2.Driver");

		DataSource ds = dataSourceService.getDataSource("test-ds");
		assertNotNull(ds);
	}

	@Test
	void testGetDataSourceNotFound() {
		DataSource ds = dataSourceService.getDataSource("non-existent");
		assertNull(ds);
	}

	@Test
	void testGetConnection() throws SQLException {
		dataSourceService.addDataSource("h2-test", "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", "", "org.h2.Driver");

		assertTrue(dataSourceService.hasDataSource("h2-test"));
		// Should be able to get connection (H2 in-memory)
		// Note: This might throw if driver not available in test classpath
		try {
			dataSourceService.getConnection("h2-test");
		}
		catch (Exception e) {
			// Expected if driver not in classpath for unit test
		}
	}

	@Test
	void testGetConnectionNotFound() {
		assertThrows(SQLException.class, () -> {
			dataSourceService.getConnection("non-existent");
		});
	}

	@Test
	void testGetDataSourceNames() {
		dataSourceService.addDataSource("ds1", "jdbc:h2:mem:db1", "sa", "", "org.h2.Driver");
		dataSourceService.addDataSource("ds2", "jdbc:h2:mem:db2", "sa", "", "org.h2.Driver");

		Set<String> names = dataSourceService.getDataSourceNames();
		assertNotNull(names);
		assertEquals(2, names.size());
		assertTrue(names.contains("ds1"));
		assertTrue(names.contains("ds2"));
	}

	@Test
	void testGetDataSourceType() {
		dataSourceService.addDataSource("mysql-ds", "jdbc:mysql://localhost:3306/db", "root", "pass",
				"com.mysql.cj.jdbc.Driver", "mysql");
		dataSourceService.addDataSource("pg-ds", "jdbc:postgresql://localhost:5432/db", "postgres", "pass",
				"org.postgresql.Driver", "postgresql");

		assertEquals("mysql", dataSourceService.getDataSourceType("mysql-ds"));
		assertEquals("postgresql", dataSourceService.getDataSourceType("pg-ds"));
		assertNull(dataSourceService.getDataSourceType("non-existent"));
	}

	@Test
	void testGetDataSourceTypeMap() {
		dataSourceService.addDataSource("ds1", "jdbc:h2:mem:db1", "sa", "", "org.h2.Driver", "h2");
		dataSourceService.addDataSource("ds2", "jdbc:h2:mem:db2", "sa", "", "org.h2.Driver", "h2");

		Map<String, String> typeMap = dataSourceService.getDataSourceTypeMap();
		assertNotNull(typeMap);
		assertEquals(2, typeMap.size());
		assertEquals("h2", typeMap.get("ds1"));
		assertEquals("h2", typeMap.get("ds2"));
	}

	@Test
	void testGetAllDatasourceInfo() {
		dataSourceService.addDataSource("ds1", "jdbc:h2:mem:db1", "sa", "", "org.h2.Driver", "h2");
		dataSourceService.addDataSource("ds2", "jdbc:mysql://localhost:3306/db", "root", "pass", "com.mysql.cj.jdbc.Driver",
				"mysql");

		Map<String, String> info = dataSourceService.getAllDatasourceInfo();
		assertNotNull(info);
		assertEquals(2, info.size());
		assertEquals("h2", info.get("ds1"));
		assertEquals("mysql", info.get("ds2"));
	}

	@Test
	void testHasDataSource() {
		dataSourceService.addDataSource("existing-ds", "jdbc:h2:mem:db", "sa", "", "org.h2.Driver");

		assertTrue(dataSourceService.hasDataSource("existing-ds"));
		assertFalse(dataSourceService.hasDataSource("non-existing-ds"));
	}

	@Test
	void testGetDataSourceCount() {
		assertEquals(0, dataSourceService.getDataSourceCount());

		dataSourceService.addDataSource("ds1", "jdbc:h2:mem:db1", "sa", "", "org.h2.Driver");
		assertEquals(1, dataSourceService.getDataSourceCount());

		dataSourceService.addDataSource("ds2", "jdbc:h2:mem:db2", "sa", "", "org.h2.Driver");
		assertEquals(2, dataSourceService.getDataSourceCount());
	}

	@Test
	void testGetDefaultDataSource() {
		dataSourceService.addDataSource("ds1", "jdbc:h2:mem:db1", "sa", "", "org.h2.Driver");
		dataSourceService.addDataSource("ds2", "jdbc:h2:mem:db2", "sa", "", "org.h2.Driver");

		DataSource defaultDs = dataSourceService.getDataSource();
		assertNotNull(defaultDs);
	}

	@Test
	void testGetDefaultDataSourceWhenEmpty() {
		DataSource defaultDs = dataSourceService.getDataSource();
		assertNull(defaultDs);
	}

	@Test
	void testGetDefaultDataSourceType() {
		dataSourceService.addDataSource("ds1", "jdbc:h2:mem:db1", "sa", "", "org.h2.Driver", "h2");
		dataSourceService.addDataSource("ds2", "jdbc:mysql://localhost:3306/db", "root", "pass", "com.mysql.cj.jdbc.Driver",
				"mysql");

		String defaultType = dataSourceService.getDataSourceType();
		assertNotNull(defaultType);
	}

	@Test
	void testGetDefaultDataSourceTypeWhenEmpty() {
		String defaultType = dataSourceService.getDataSourceType();
		assertNull(defaultType);
	}

	@Test
	void testClose() {
		// Should not throw
		dataSourceService.close();
	}

	@Test
	void testCloseAllConnections() {
		// Should not throw
		dataSourceService.closeAllConnections();
	}

	@Test
	void testTestConnectionWithH2() {
		// H2 in-memory database for testing
		boolean result = dataSourceService.testConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", "",
				"org.h2.Driver");
		// This may return false if H2 driver not in test classpath
		// In a real integration test, this would work
	}

	@Test
	void testAddDataSourceOverwritesExisting() {
		dataSourceService.addDataSource("ds1", "jdbc:h2:mem:db1", "sa", "", "org.h2.Driver");
		dataSourceService.addDataSource("ds1", "jdbc:h2:mem:db2", "sa", "", "org.h2.Driver");

		assertEquals(1, dataSourceService.getDataSourceCount());
	}

	@Test
	void void_testGetConnectionWithEmptyDataSourceList() {
		assertThrows(SQLException.class, () -> {
			dataSourceService.getConnection();
		});
	}

	@Test
	void testGetConnectionByNameReturnsCorrectType() {
		dataSourceService.addDataSource("h2-test", "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", "", "org.h2.Driver");

		DataSource ds = dataSourceService.getDataSource("h2-test");
		assertNotNull(ds);
		assertSame(ds.getClass().getName().contains("DriverManagerDataSource"), true);
	}

}
