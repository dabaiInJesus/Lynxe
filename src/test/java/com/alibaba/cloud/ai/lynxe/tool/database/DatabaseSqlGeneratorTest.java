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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.alibaba.cloud.ai.lynxe.tool.database.sql.DatabaseSqlGenerator;

/**
 * Unit tests for DatabaseSqlGenerator
 */
public class DatabaseSqlGeneratorTest {

	@ParameterizedTest
	@ValueSource(strings = { "mysql", "postgresql", "oracle", "sqlserver", "h2", "clickhouse", "doris", "hive",
			"oceanbase", "dameng", "kingbase", "gaussdb" })
	void testGenerateTableInfoSql(String databaseType) {
		String sql = DatabaseSqlGenerator.generateTableInfoSql(databaseType, false, null);
		assertNotNull(sql);
		assertTrue(sql.length() > 0);
		assertTrue(sql.toLowerCase().contains("select"));
		assertTrue(sql.toLowerCase().contains("table"));
	}

	@ParameterizedTest
	@ValueSource(strings = { "mysql", "postgresql", "oracle", "sqlserver", "h2", "clickhouse", "doris", "hive",
			"oceanbase", "dameng", "kingbase", "gaussdb" })
	void testGenerateTableInfoSqlWithFuzzy(String databaseType) {
		String sql = DatabaseSqlGenerator.generateTableInfoSql(databaseType, true, "test%");
		assertNotNull(sql);
		assertTrue(sql.length() > 0);
		assertTrue(sql.toLowerCase().contains("like"));
	}

	@Test
	void testGenerateTableInfoSqlWithNullType() {
		// Should fallback to MySQL
		String sql = DatabaseSqlGenerator.generateTableInfoSql(null, false, null);
		assertNotNull(sql);
		assertTrue(sql.contains("information_schema"));
	}

	@Test
	void testGenerateTableInfoSqlWithUnknownType() {
		// Should fallback to MySQL
		String sql = DatabaseSqlGenerator.generateTableInfoSql("unknown_db", false, null);
		assertNotNull(sql);
		assertTrue(sql.contains("information_schema"));
	}

	@ParameterizedTest
	@ValueSource(strings = { "mysql", "postgresql", "oracle", "sqlserver", "h2", "clickhouse", "doris", "hive",
			"oceanbase", "dameng", "kingbase", "gaussdb" })
	void testGenerateColumnInfoSql(String databaseType) {
		String inClause = "'table1', 'table2'";
		String sql = DatabaseSqlGenerator.generateColumnInfoSql(databaseType, inClause);
		assertNotNull(sql);
		assertTrue(sql.length() > 0);
		assertTrue(sql.toLowerCase().contains("select"));
		assertTrue(sql.toLowerCase().contains("column"));
		assertTrue(sql.contains(inClause));
	}

	@ParameterizedTest
	@ValueSource(strings = { "mysql", "postgresql", "oracle", "sqlserver", "h2" })
	void testGenerateIndexInfoSqlWithFullSupport(String databaseType) {
		String inClause = "'table1', 'table2'";
		String sql = DatabaseSqlGenerator.generateIndexInfoSql(databaseType, inClause);
		assertNotNull(sql);
		assertTrue(sql.length() > 0);
		assertTrue(sql.toLowerCase().contains("select"));
		assertTrue(sql.toLowerCase().contains("index"));
	}

	@ParameterizedTest
	@ValueSource(strings = { "clickhouse", "doris", "hive", "oceanbase", "dameng", "kingbase", "gaussdb" })
	void testGenerateIndexInfoSqlWithLimitedSupport(String databaseType) {
		String inClause = "'table1', 'table2'";
		String sql = DatabaseSqlGenerator.generateIndexInfoSql(databaseType, inClause);
		assertNotNull(sql);
		assertTrue(sql.contains("1=0")); // Returns empty result
	}

	@Test
	void testMySqlTableInfoSqlContainsCorrectSchema() {
		String sql = DatabaseSqlGenerator.generateTableInfoSql("mysql", false, null);
		assertTrue(sql.contains("information_schema.TABLES"));
		assertTrue(sql.contains("table_schema"));
		assertTrue(sql.contains("TABLE_NAME"));
		assertTrue(sql.contains("TABLE_COMMENT"));
	}

	@Test
	void testPostgresqlTableInfoSqlContainsPgTables() {
		String sql = DatabaseSqlGenerator.generateTableInfoSql("postgresql", false, null);
		assertTrue(sql.contains("pg_tables"));
		assertTrue(sql.contains("pg_class"));
		assertTrue(sql.contains("schemaname"));
	}

	@Test
	void testOracleTableInfoSqlContainsUserTabComments() {
		String sql = DatabaseSqlGenerator.generateTableInfoSql("oracle", false, null);
		assertTrue(sql.contains("user_tab_comments"));
		assertTrue(sql.contains("table_name"));
		assertTrue(sql.contains("comments"));
	}

	@Test
	void testSqlServerTableInfoSqlContainsSysTables() {
		String sql = DatabaseSqlGenerator.generateTableInfoSql("sqlserver", false, null);
		assertTrue(sql.contains("sys.tables"));
		assertTrue(sql.contains("extended_properties"));
	}

	@Test
	void testH2TableInfoSqlContainsInformationSchema() {
		String sql = DatabaseSqlGenerator.generateTableInfoSql("h2", false, null);
		assertTrue(sql.contains("information_schema.tables"));
		assertTrue(sql.contains("BASE TABLE"));
	}

	@Test
	void testClickHouseTableInfoSqlContainsSystemTables() {
		String sql = DatabaseSqlGenerator.generateTableInfoSql("clickhouse", false, null);
		assertTrue(sql.contains("system.tables"));
		assertTrue(sql.contains("database"));
	}

	@Test
	void testHiveTableInfoSqlContainsTbls() {
		String sql = DatabaseSqlGenerator.generateTableInfoSql("hive", false, null);
		assertTrue(sql.contains("TBLS"));
		assertTrue(sql.contains("TBL_NAME"));
		assertTrue(sql.contains("REMARKS"));
		assertTrue(sql.contains("VIEW")); // TBL_TYPE filter
	}

	@Test
	void testOceanBaseTableInfoSqlUsesMySqlCompatibility() {
		String sql = DatabaseSqlGenerator.generateTableInfoSql("oceanbase", false, null);
		assertTrue(sql.contains("information_schema.TABLES"));
		assertTrue(sql.contains("TABLE_NAME"));
		assertTrue(sql.contains("TABLE_COMMENT"));
	}

	@Test
	void testDamengTableInfoSqlContainsUserTabComments() {
		String sql = DatabaseSqlGenerator.generateTableInfoSql("dameng", false, null);
		assertTrue(sql.contains("USER_TAB_COMMENTS"));
	}

	@Test
	void testKingbaseTableInfoSqlUsesPostgresCompatibility() {
		String sql = DatabaseSqlGenerator.generateTableInfoSql("kingbase", false, null);
		assertTrue(sql.contains("pg_tables"));
		assertTrue(sql.contains("pg_class"));
	}

	@Test
	void testGaussdbTableInfoSqlUsesPostgresCompatibility() {
		String sql = DatabaseSqlGenerator.generateTableInfoSql("gaussdb", false, null);
		assertTrue(sql.contains("pg_tables"));
		assertTrue(sql.contains("pg_class"));
	}

	@Test
	void testDorisTableInfoSqlContainsInformationSchema() {
		String sql = DatabaseSqlGenerator.generateTableInfoSql("doris", false, null);
		assertTrue(sql.contains("information_schema.tables"));
		assertTrue(sql.contains("TABLE_NAME"));
		assertTrue(sql.contains("TABLE_COMMENT"));
	}

	@Test
	void testDatabaseAliasesProduceSameResults() {
		String dorisSql = DatabaseSqlGenerator.generateTableInfoSql("doris", false, null);
		String apacheDorisSql = DatabaseSqlGenerator.generateTableInfoSql("apache_doris", false, null);
		assertEquals(dorisSql, apacheDorisSql);

		String hiveSql = DatabaseSqlGenerator.generateTableInfoSql("hive", false, null);
		String apacheHiveSql = DatabaseSqlGenerator.generateTableInfoSql("apache_hive", false, null);
		assertEquals(hiveSql, apacheHiveSql);

		String obSql = DatabaseSqlGenerator.generateTableInfoSql("ob", false, null);
		String oceanbaseSql = DatabaseSqlGenerator.generateTableInfoSql("oceanbase", false, null);
		assertEquals(obSql, oceanbaseSql);

		String dmSql = DatabaseSqlGenerator.generateTableInfoSql("dm", false, null);
		String damengSql = DatabaseSqlGenerator.generateTableInfoSql("dameng", false, null);
		assertEquals(dmSql, damengSql);

		String kbSql = DatabaseSqlGenerator.generateTableInfoSql("kb", false, null);
		String kingbaseSql = DatabaseSqlGenerator.generateTableInfoSql("kingbase", false, null);
		assertEquals(kbSql, kingbaseSql);

		String gaussSql = DatabaseSqlGenerator.generateTableInfoSql("gauss", false, null);
		String gaussdbSql = DatabaseSqlGenerator.generateTableInfoSql("gaussdb", false, null);
		assertEquals(gaussSql, gaussdbSql);
	}

	@Test
	void testFuzzyQueryWithLike() {
		String mysqlSql = DatabaseSqlGenerator.generateTableInfoSql("mysql", true, "user%");
		assertTrue(mysqlSql.contains("LIKE"));
		assertTrue(mysqlSql.contains("?")); // Prepared statement parameter

		String pgSql = DatabaseSqlGenerator.generateTableInfoSql("postgresql", true, "user%");
		assertTrue(pgSql.contains("LIKE"));
	}

	@Test
	void testColumnInfoSqlContainsExpectedFields() {
		String sql = DatabaseSqlGenerator.generateColumnInfoSql("mysql", "'users', 'orders'");
		assertTrue(sql.contains("TABLE_NAME"));
		assertTrue(sql.contains("COLUMN_NAME"));
		assertTrue(sql.contains("COLUMN_TYPE"));
		assertTrue(sql.contains("CHARACTER_MAXIMUM_LENGTH"));
		assertTrue(sql.contains("COLUMN_COMMENT"));
		assertTrue(sql.contains("COLUMN_DEFAULT"));
		assertTrue(sql.contains("IS_NULLABLE"));
	}

	private void assertEquals(String sql1, String sql2) {
		org.junit.jupiter.api.Assertions.assertEquals(sql1, sql2);
	}

}
