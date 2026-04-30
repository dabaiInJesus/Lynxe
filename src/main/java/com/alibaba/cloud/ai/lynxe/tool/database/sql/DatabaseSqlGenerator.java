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

package com.alibaba.cloud.ai.lynxe.tool.database.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Database SQL generator, generates corresponding SQL statements based on different
 * database types
 */
public class DatabaseSqlGenerator {

	private static final Logger log = LoggerFactory.getLogger(DatabaseSqlGenerator.class);

	/**
	 * Generate SQL for getting table information
	 */
	public static String generateTableInfoSql(String databaseType, boolean fuzzy, String fuzzyText) {
		switch (normalizeDatabaseType(databaseType)) {
			case "mysql":
			case "mariadb":
				return generateMysqlTableInfoSql(fuzzy, fuzzyText);
			case "postgresql":
				return generatePostgresqlTableInfoSql(fuzzy, fuzzyText);
			case "oracle":
				return generateOracleTableInfoSql(fuzzy, fuzzyText);
			case "sqlserver":
				return generateSqlServerTableInfoSql(fuzzy, fuzzyText);
			case "h2":
				return generateH2TableInfoSql(fuzzy, fuzzyText);
			case "clickhouse":
				return generateClickHouseTableInfoSql(fuzzy, fuzzyText);
			case "doris":
			case "apache_doris":
				return generateDorisTableInfoSql(fuzzy, fuzzyText);
			case "hive":
			case "apache_hive":
				return generateHiveTableInfoSql(fuzzy, fuzzyText);
			case "oceanbase":
			case "ob":
				return generateOceanBaseTableInfoSql(fuzzy, fuzzyText);
			case "dameng":
			case "dm":
				return generateDamengTableInfoSql(fuzzy, fuzzyText);
			case "kingbase":
			case "kb":
				return generateKingbaseTableInfoSql(fuzzy, fuzzyText);
			case "gaussdb":
			case "gauss":
				return generateGaussdbTableInfoSql(fuzzy, fuzzyText);
			default:
				log.warn("Unknown database type: {}, using MySQL SQL as fallback", databaseType);
				return generateMysqlTableInfoSql(fuzzy, fuzzyText);
		}
	}

	/**
	 * Generate SQL for getting table field information
	 */
	public static String generateColumnInfoSql(String databaseType, String inClause) {
		switch (normalizeDatabaseType(databaseType)) {
			case "mysql":
			case "mariadb":
				return generateMysqlColumnInfoSql(inClause);
			case "postgresql":
				return generatePostgresqlColumnInfoSql(inClause);
			case "oracle":
				return generateOracleColumnInfoSql(inClause);
			case "sqlserver":
				return generateSqlServerColumnInfoSql(inClause);
			case "h2":
				return generateH2ColumnInfoSql(inClause);
			case "clickhouse":
				return generateClickHouseColumnInfoSql(inClause);
			case "doris":
			case "apache_doris":
				return generateDorisColumnInfoSql(inClause);
			case "hive":
			case "apache_hive":
				return generateHiveColumnInfoSql(inClause);
			case "oceanbase":
			case "ob":
				return generateOceanBaseColumnInfoSql(inClause);
			case "dameng":
			case "dm":
				return generateDamengColumnInfoSql(inClause);
			case "kingbase":
			case "kb":
				return generateKingbaseColumnInfoSql(inClause);
			case "gaussdb":
			case "gauss":
				return generateGaussdbColumnInfoSql(inClause);
			default:
				log.warn("Unknown database type: {}, using MySQL SQL as fallback", databaseType);
				return generateMysqlColumnInfoSql(inClause);
		}
	}

	/**
	 * Generate SQL for getting table index information
	 */
	public static String generateIndexInfoSql(String databaseType, String inClause) {
		switch (normalizeDatabaseType(databaseType)) {
			case "mysql":
			case "mariadb":
				return generateMysqlIndexInfoSql(inClause);
			case "postgresql":
				return generatePostgresqlIndexInfoSql(inClause);
			case "oracle":
				return generateOracleIndexInfoSql(inClause);
			case "sqlserver":
				return generateSqlServerIndexInfoSql(inClause);
			case "h2":
				return generateH2IndexInfoSql(inClause);
			case "clickhouse":
			case "doris":
			case "apache_doris":
			case "hive":
			case "apache_hive":
			case "oceanbase":
			case "ob":
			case "dameng":
			case "dm":
			case "kingbase":
			case "kb":
			case "gaussdb":
			case "gauss":
				// These databases don't have standard index metadata tables accessible via
				// simple SQL, return empty result
				return generateEmptyIndexInfoSql(inClause);
			default:
				log.warn("Unknown database type: {}, using MySQL SQL as fallback", databaseType);
				return generateMysqlIndexInfoSql(inClause);
		}
	}

	/**
	 * Normalize database type name
	 */
	private static String normalizeDatabaseType(String databaseType) {
		if (databaseType == null) {
			return "mysql"; // Default to MySQL
		}
		return databaseType.toLowerCase().trim();
	}

	// MySQL/MariaDB SQL generation methods
	private static String generateMysqlTableInfoSql(boolean fuzzy, String fuzzyText) {
		if (fuzzy) {
			return "SELECT TABLE_NAME, TABLE_COMMENT FROM information_schema.TABLES "
					+ "WHERE (TABLE_NAME LIKE ? OR TABLE_COMMENT LIKE ?) AND table_schema NOT IN ('sys','mysql','performance_schema','information_schema')";
		}
		else {
			return "SELECT TABLE_NAME, TABLE_COMMENT FROM information_schema.TABLES "
					+ "WHERE table_schema NOT IN ('sys','mysql','performance_schema','information_schema')";
		}
	}

	private static String generateMysqlColumnInfoSql(String inClause) {
		return "SELECT TABLE_NAME, COLUMN_NAME, COLUMN_TYPE, CHARACTER_MAXIMUM_LENGTH, COLUMN_COMMENT, "
				+ "COLUMN_DEFAULT, IS_NULLABLE " + "FROM information_schema.COLUMNS WHERE TABLE_NAME IN (" + inClause
				+ ") " + "ORDER BY TABLE_NAME, ORDINAL_POSITION";
	}

	private static String generateMysqlIndexInfoSql(String inClause) {
		return "SELECT TABLE_NAME, INDEX_NAME, COLUMN_NAME, INDEX_TYPE "
				+ "FROM information_schema.STATISTICS WHERE TABLE_NAME IN (" + inClause + ") "
				+ "ORDER BY TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX";
	}

	// PostgreSQL SQL generation methods
	private static String generatePostgresqlTableInfoSql(boolean fuzzy, String fuzzyText) {
		if (fuzzy) {
			return "SELECT tablename as TABLE_NAME, obj_description(c.oid) as TABLE_COMMENT "
					+ "FROM pg_tables t JOIN pg_class c ON t.tablename = c.relname "
					+ "WHERE obj_description(c.oid) LIKE ? AND schemaname NOT IN ('information_schema', 'pg_catalog')";
		}
		else {
			return "SELECT tablename as TABLE_NAME, obj_description(c.oid) as TABLE_COMMENT "
					+ "FROM pg_tables t JOIN pg_class c ON t.tablename = c.relname "
					+ "WHERE schemaname NOT IN ('information_schema', 'pg_catalog')";
		}
	}

	private static String generatePostgresqlColumnInfoSql(String inClause) {
		return "SELECT c.table_name as TABLE_NAME, c.column_name as COLUMN_NAME, c.data_type as COLUMN_TYPE, "
				+ "c.character_maximum_length as CHARACTER_MAXIMUM_LENGTH, col_description(c.table_name::regclass, c.ordinal_position) as COLUMN_COMMENT, "
				+ "c.column_default as COLUMN_DEFAULT, c.is_nullable as IS_NULLABLE "
				+ "FROM information_schema.columns c " + "WHERE c.table_name IN (" + inClause + ") "
				+ "ORDER BY c.table_name, c.ordinal_position";
	}

	private static String generatePostgresqlIndexInfoSql(String inClause) {
		return "SELECT t.relname as TABLE_NAME, i.relname as INDEX_NAME, "
				+ "a.attname as COLUMN_NAME, am.amname as INDEX_TYPE "
				+ "FROM pg_index ix JOIN pg_class t ON ix.indrelid = t.oid "
				+ "JOIN pg_class i ON ix.indexrelid = i.oid "
				+ "JOIN pg_attribute a ON a.attrelid = t.oid AND a.attnum = ANY(ix.indkey) "
				+ "JOIN pg_am am ON i.relam = am.oid " + "WHERE t.relname IN (" + inClause + ") "
				+ "ORDER BY t.relname, i.relname, a.attnum";
	}

	// Oracle SQL generation methods
	private static String generateOracleTableInfoSql(boolean fuzzy, String fuzzyText) {
		if (fuzzy) {
			return "SELECT table_name as TABLE_NAME, comments as TABLE_COMMENT " + "FROM user_tab_comments "
					+ "WHERE comments LIKE ?";
		}
		else {
			return "SELECT table_name as TABLE_NAME, comments as TABLE_COMMENT " + "FROM user_tab_comments";
		}
	}

	private static String generateOracleColumnInfoSql(String inClause) {
		return "SELECT c.table_name as TABLE_NAME, c.column_name as COLUMN_NAME, "
				+ "c.data_type as COLUMN_TYPE, c.data_length as CHARACTER_MAXIMUM_LENGTH, "
				+ "cc.comments as COLUMN_COMMENT, c.data_default as COLUMN_DEFAULT, "
				+ "CASE WHEN c.nullable = 'N' THEN 'NO' ELSE 'YES' END as IS_NULLABLE " + "FROM user_tab_columns c "
				+ "LEFT JOIN user_col_comments cc ON c.table_name = cc.table_name AND c.column_name = cc.column_name "
				+ "WHERE c.table_name IN (" + inClause + ") " + "ORDER BY c.table_name, c.column_id";
	}

	private static String generateOracleIndexInfoSql(String inClause) {
		return "SELECT t.table_name as TABLE_NAME, i.index_name as INDEX_NAME, "
				+ "c.column_name as COLUMN_NAME, i.index_type as INDEX_TYPE " + "FROM user_indexes i "
				+ "JOIN user_ind_columns c ON i.index_name = c.index_name "
				+ "JOIN user_tables t ON i.table_name = t.table_name " + "WHERE t.table_name IN (" + inClause + ") "
				+ "ORDER BY t.table_name, i.index_name, c.column_position";
	}

	// SQL Server SQL generation methods
	private static String generateSqlServerTableInfoSql(boolean fuzzy, String fuzzyText) {
		if (fuzzy) {
			return "SELECT t.name as TABLE_NAME, ep.value as TABLE_COMMENT " + "FROM sys.tables t "
					+ "LEFT JOIN sys.extended_properties ep ON ep.major_id = t.object_id "
					+ "AND ep.minor_id = 0 AND ep.name = 'MS_Description' " + "WHERE ep.value LIKE ?";
		}
		else {
			return "SELECT t.name as TABLE_NAME, ep.value as TABLE_COMMENT " + "FROM sys.tables t "
					+ "LEFT JOIN sys.extended_properties ep ON ep.major_id = t.object_id "
					+ "AND ep.minor_id = 0 AND ep.name = 'MS_Description'";
		}
	}

	private static String generateSqlServerColumnInfoSql(String inClause) {
		return "SELECT t.name as TABLE_NAME, c.name as COLUMN_NAME, "
				+ "ty.name as COLUMN_TYPE, c.max_length as CHARACTER_MAXIMUM_LENGTH, " + "ep.value as COLUMN_COMMENT, "
				+ "OBJECT_DEFINITION(dc.object_id) as COLUMN_DEFAULT, "
				+ "CASE WHEN c.is_nullable = 1 THEN 'YES' ELSE 'NO' END as IS_NULLABLE " + "FROM sys.columns c "
				+ "JOIN sys.tables t ON c.object_id = t.object_id "
				+ "JOIN sys.types ty ON c.user_type_id = ty.user_type_id "
				+ "LEFT JOIN sys.extended_properties ep ON ep.major_id = t.object_id "
				+ "AND ep.minor_id = c.column_id AND ep.name = 'MS_Description' "
				+ "LEFT JOIN sys.default_constraints dc ON dc.parent_object_id = t.object_id "
				+ "AND dc.parent_column_id = c.column_id " + "WHERE t.name IN (" + inClause + ") "
				+ "ORDER BY t.name, c.column_id";
	}

	private static String generateSqlServerIndexInfoSql(String inClause) {
		return "SELECT t.name as TABLE_NAME, i.name as INDEX_NAME, "
				+ "c.name as COLUMN_NAME, i.type_desc as INDEX_TYPE " + "FROM sys.indexes i "
				+ "JOIN sys.tables t ON i.object_id = t.object_id "
				+ "JOIN sys.index_columns ic ON i.object_id = ic.object_id AND i.index_id = ic.index_id "
				+ "JOIN sys.columns c ON ic.object_id = c.object_id AND ic.column_id = c.column_id "
				+ "WHERE t.name IN (" + inClause + ") " + "ORDER BY t.name, i.name, ic.key_ordinal";
	}

	// H2 SQL generation methods
	private static String generateH2TableInfoSql(boolean fuzzy, String fuzzyText) {
		if (fuzzy) {
			return "SELECT table_name as TABLE_NAME, remarks as TABLE_COMMENT " + "FROM information_schema.tables "
					+ "WHERE table_type = 'BASE TABLE' AND table_name LIKE ?";
		}
		else {
			return "SELECT table_name as TABLE_NAME, remarks as TABLE_COMMENT " + "FROM information_schema.tables "
					+ "WHERE table_type = 'BASE TABLE'";
		}
	}

	private static String generateH2ColumnInfoSql(String inClause) {
		return "SELECT table_name as TABLE_NAME, column_name as COLUMN_NAME, "
				+ "data_type as COLUMN_TYPE, character_maximum_length as CHARACTER_MAXIMUM_LENGTH, "
				+ "remarks as COLUMN_COMMENT, column_default as COLUMN_DEFAULT, " + "is_nullable as IS_NULLABLE "
				+ "FROM information_schema.columns " + "WHERE table_name IN (" + inClause + ") "
				+ "ORDER BY table_name, ordinal_position";
	}

	private static String generateH2IndexInfoSql(String inClause) {
		return "SELECT ic.table_name as TABLE_NAME, i.index_name as INDEX_NAME, " + "ic.column_name as COLUMN_NAME, "
				+ "CASE WHEN i.index_type_name = 'PRIMARY KEY' THEN 'PRIMARY' ELSE 'BTREE' END as INDEX_TYPE "
				+ "FROM information_schema.index_columns ic "
				+ "JOIN information_schema.indexes i ON ic.table_name = i.table_name AND ic.index_name = i.index_name "
				+ "WHERE ic.table_name IN (" + inClause + ") "
				+ "ORDER BY ic.table_name, i.index_name, ic.ordinal_position";
	}

	// ClickHouse SQL generation methods
	private static String generateClickHouseTableInfoSql(boolean fuzzy, String fuzzyText) {
		if (fuzzy) {
			return "SELECT name as TABLE_NAME, comment as TABLE_COMMENT FROM system.tables "
					+ "WHERE (name LIKE ? OR comment LIKE ?) AND database NOT IN ('system')";
		}
		else {
			return "SELECT name as TABLE_NAME, comment as TABLE_COMMENT FROM system.tables "
					+ "WHERE database NOT IN ('system')";
		}
	}

	private static String generateClickHouseColumnInfoSql(String inClause) {
		return "SELECT table as TABLE_NAME, name as COLUMN_NAME, type as COLUMN_TYPE, "
				+ "data_compressed_bytes as CHARACTER_MAXIMUM_LENGTH, comment as COLUMN_COMMENT, "
				+ "default_expression as COLUMN_DEFAULT, is_nullable as IS_NULLABLE "
				+ "FROM system.columns WHERE table IN (" + inClause + ") "
				+ "ORDER BY table, position";
	}

	// Apache Doris SQL generation methods
	private static String generateDorisTableInfoSql(boolean fuzzy, String fuzzyText) {
		if (fuzzy) {
			return "SELECT TABLE_NAME, TABLE_COMMENT FROM information_schema.tables "
					+ "WHERE (TABLE_NAME LIKE ? OR TABLE_COMMENT LIKE ?) AND TABLE_SCHEMA NOT IN ('information_schema', 'mysql', 'performance_schema', 'sys')";
		}
		else {
			return "SELECT TABLE_NAME, TABLE_COMMENT FROM information_schema.tables "
					+ "WHERE TABLE_SCHEMA NOT IN ('information_schema', 'mysql', 'performance_schema', 'sys')";
		}
	}

	private static String generateDorisColumnInfoSql(String inClause) {
		return "SELECT TABLE_NAME, COLUMN_NAME, COLUMN_TYPE, CHARACTER_MAXIMUM_LENGTH, COLUMN_COMMENT, "
				+ "COLUMN_DEFAULT, IS_NULLABLE FROM information_schema.columns WHERE TABLE_NAME IN (" + inClause
				+ ") ORDER BY TABLE_NAME, ORDINAL_POSITION";
	}

	// Apache Hive SQL generation methods
	private static String generateHiveTableInfoSql(boolean fuzzy, String fuzzyText) {
		if (fuzzy) {
			return "SELECT TBL_NAME as TABLE_NAME, REMARKS as TABLE_COMMENT FROM TBLS "
					+ "WHERE (TBL_NAME LIKE ? OR REMARKS LIKE ?) AND TBL_TYPE NOT LIKE '%VIEW%'";
		}
		else {
			return "SELECT TBL_NAME as TABLE_NAME, REMARKS as TABLE_COMMENT FROM TBLS "
					+ "WHERE TBL_TYPE NOT LIKE '%VIEW%'";
		}
	}

	private static String generateHiveColumnInfoSql(String inClause) {
		return "SELECT t.TBL_NAME as TABLE_NAME, c.COLUMN_NAME as COLUMN_NAME, c.TYPE_NAME as COLUMN_TYPE, "
				+ "c.`COMMENT` as COLUMN_COMMENT FROM COLUMNS_V2 c "
				+ "JOIN TBLS t ON c.CD_ID = t.DEFAULT_COL_NAME_LEN " + "WHERE t.TBL_NAME IN (" + inClause
				+ ") ORDER BY t.TBL_NAME, c.INTEGER_IDX";
	}

	// OceanBase SQL generation methods (compatible with MySQL mode)
	private static String generateOceanBaseTableInfoSql(boolean fuzzy, String fuzzyText) {
		if (fuzzy) {
			return "SELECT TABLE_NAME, TABLE_COMMENT FROM information_schema.TABLES "
					+ "WHERE (TABLE_NAME LIKE ? OR TABLE_COMMENT LIKE ?) AND table_schema NOT IN ('sys','mysql','performance_schema','information_schema')";
		}
		else {
			return "SELECT TABLE_NAME, TABLE_COMMENT FROM information_schema.TABLES "
					+ "WHERE table_schema NOT IN ('sys','mysql','performance_schema','information_schema')";
		}
	}

	private static String generateOceanBaseColumnInfoSql(String inClause) {
		return "SELECT TABLE_NAME, COLUMN_NAME, COLUMN_TYPE, CHARACTER_MAXIMUM_LENGTH, COLUMN_COMMENT, "
				+ "COLUMN_DEFAULT, IS_NULLABLE FROM information_schema.COLUMNS WHERE TABLE_NAME IN (" + inClause
				+ ") ORDER BY TABLE_NAME, ORDINAL_POSITION";
	}

	// 达梦 (DM Database) SQL generation methods
	private static String generateDamengTableInfoSql(boolean fuzzy, String fuzzyText) {
		if (fuzzy) {
			return "SELECT TABLE_NAME, COMMENTS as TABLE_COMMENT FROM USER_TAB_COMMENTS "
					+ "WHERE COMMENTS LIKE ?";
		}
		else {
			return "SELECT TABLE_NAME, COMMENTS as TABLE_COMMENT FROM USER_TAB_COMMENTS";
		}
	}

	private static String generateDamengColumnInfoSql(String inClause) {
		return "SELECT c.TABLE_NAME as TABLE_NAME, c.COLUMN_NAME as COLUMN_NAME, c.DATA_TYPE as COLUMN_TYPE, "
				+ "c.DATA_LENGTH as CHARACTER_MAXIMUM_LENGTH, cc.COMMENTS as COLUMN_COMMENT, "
				+ "c.DATA_DEFAULT as COLUMN_DEFAULT, CASE WHEN c.NULLABLE = 'N' THEN 'NO' ELSE 'YES' END as IS_NULLABLE "
				+ "FROM USER_TAB_COLUMNS c LEFT JOIN USER_COL_COMMENTS cc "
				+ "ON c.TABLE_NAME = cc.TABLE_NAME AND c.COLUMN_NAME = cc.COLUMN_NAME "
				+ "WHERE c.TABLE_NAME IN (" + inClause + ") ORDER BY c.TABLE_NAME, c.COLUMN_ID";
	}

	// 人大金仓 (KingBase) SQL generation methods (compatible with PostgreSQL mode)
	private static String generateKingbaseTableInfoSql(boolean fuzzy, String fuzzyText) {
		if (fuzzy) {
			return "SELECT tablename as TABLE_NAME, obj_description(c.oid) as TABLE_COMMENT "
					+ "FROM pg_tables t JOIN pg_class c ON t.tablename = c.relname "
					+ "WHERE obj_description(c.oid) LIKE ? AND schemaname NOT IN ('information_schema', 'pg_catalog')";
		}
		else {
			return "SELECT tablename as TABLE_NAME, obj_description(c.oid) as TABLE_COMMENT "
					+ "FROM pg_tables t JOIN pg_class c ON t.tablename = c.relname "
					+ "WHERE schemaname NOT IN ('information_schema', 'pg_catalog')";
		}
	}

	private static String generateKingbaseColumnInfoSql(String inClause) {
		return "SELECT c.table_name as TABLE_NAME, c.column_name as COLUMN_NAME, c.data_type as COLUMN_TYPE, "
				+ "c.character_maximum_length as CHARACTER_MAXIMUM_LENGTH, col_description(c.table_name::regclass, c.ordinal_position) as COLUMN_COMMENT, "
				+ "c.column_default as COLUMN_DEFAULT, c.is_nullable as IS_NULLABLE "
				+ "FROM information_schema.columns c WHERE c.table_name IN (" + inClause + ") "
				+ "ORDER BY c.table_name, c.ordinal_position";
	}

	// GaussDB SQL generation methods
	private static String generateGaussdbTableInfoSql(boolean fuzzy, String fuzzyText) {
		if (fuzzy) {
			return "SELECT tablename as TABLE_NAME, obj_description(c.oid) as TABLE_COMMENT "
					+ "FROM pg_tables t JOIN pg_class c ON t.tablename = c.relname "
					+ "WHERE obj_description(c.oid) LIKE ? AND schemaname NOT IN ('information_schema', 'pg_catalog')";
		}
		else {
			return "SELECT tablename as TABLE_NAME, obj_description(c.oid) as TABLE_COMMENT "
					+ "FROM pg_tables t JOIN pg_class c ON t.tablename = c.relname "
					+ "WHERE schemaname NOT IN ('information_schema', 'pg_catalog')";
		}
	}

	private static String generateGaussdbColumnInfoSql(String inClause) {
		return "SELECT c.table_name as TABLE_NAME, c.column_name as COLUMN_NAME, c.data_type as COLUMN_TYPE, "
				+ "c.character_maximum_length as CHARACTER_MAXIMUM_LENGTH, col_description(c.table_name::regclass, c.ordinal_position) as COLUMN_COMMENT, "
				+ "c.column_default as COLUMN_DEFAULT, c.is_nullable as IS_NULLABLE "
				+ "FROM information_schema.columns c WHERE c.table_name IN (" + inClause + ") "
				+ "ORDER BY c.table_name, c.ordinal_position";
	}

	// Empty index SQL for databases without standard index metadata
	private static String generateEmptyIndexInfoSql(String inClause) {
		return "SELECT '' as TABLE_NAME, '' as INDEX_NAME, '' as COLUMN_NAME, '' as INDEX_TYPE "
				+ "WHERE 1=0 -- Not supported for this database type";
	}

}
