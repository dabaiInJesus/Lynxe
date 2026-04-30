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
package com.alibaba.cloud.ai.lynxe.tool.database.databaseOperators;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.cloud.ai.lynxe.tool.AbstractBaseTool;
import com.alibaba.cloud.ai.lynxe.tool.ToolStateInfo;
import com.alibaba.cloud.ai.lynxe.tool.code.ToolExecuteResult;
import com.alibaba.cloud.ai.lynxe.tool.database.service.HBaseService;
import com.alibaba.cloud.ai.lynxe.tool.i18n.ToolI18nService;

/**
 * HBase query tool for listing tables, scanning data, and retrieving rows
 */
public class HBaseQueryTool extends AbstractBaseTool<HBaseQueryTool.HBaseQueryInput> {

	private static final Logger log = LoggerFactory.getLogger(HBaseQueryTool.class);

	private static final String TOOL_NAME = "hbase-query";

	/**
	 * Input class for HBase query operations
	 */
	public static class HBaseQueryInput {

		private String tableName;

		private String rowkey;

		private String datasourceName;

		private String operation;

		private Integer limit;

		// Getters and setters
		public String getTableName() {
			return tableName;
		}

		public void setTableName(String tableName) {
			this.tableName = tableName;
		}

		public String getRowkey() {
			return rowkey;
		}

		public void setRowkey(String rowkey) {
			this.rowkey = rowkey;
		}

		public String getDatasourceName() {
			return datasourceName;
		}

		public void setDatasourceName(String datasourceName) {
			this.datasourceName = datasourceName;
		}

		public String getOperation() {
			return operation;
		}

		public void setOperation(String operation) {
			this.operation = operation;
		}

		public Integer getLimit() {
			return limit;
		}

		public void setLimit(Integer limit) {
			this.limit = limit;
		}

	}

	private final HBaseService hbaseService;

	private final ToolI18nService toolI18nService;

	public HBaseQueryTool(HBaseService hbaseService, ToolI18nService toolI18nService) {
		this.hbaseService = hbaseService;
		this.toolI18nService = toolI18nService;
	}

	@Override
	public ToolExecuteResult run(HBaseQueryInput input) {
		log.info("HBaseQueryTool request: table={}, operation={}", input.getTableName(), input.getOperation());
		try {
			String operation = input.getOperation();
			String datasourceName = input.getDatasourceName();

			if (operation == null) {
				operation = "list";
			}

			switch (operation.toLowerCase()) {
				case "list":
				case "list_tables":
					return new ToolExecuteResult(hbaseService.listTables(datasourceName));
				case "describe":
				case "desc":
				case "schema":
					if (input.getTableName() == null || input.getTableName().trim().isEmpty()) {
						return new ToolExecuteResult("Error: tableName is required for describe operation");
					}
					return new ToolExecuteResult(hbaseService.describeTable(datasourceName, input.getTableName()));
				case "scan":
				case "search":
					if (input.getTableName() == null || input.getTableName().trim().isEmpty()) {
						return new ToolExecuteResult("Error: tableName is required for scan operation");
					}
					int limit = input.getLimit() != null ? input.getLimit() : 100;
					return new ToolExecuteResult(hbaseService.scanTable(datasourceName, input.getTableName(), limit));
				case "get":
				case "get_row":
					if (input.getTableName() == null || input.getTableName().trim().isEmpty()) {
						return new ToolExecuteResult("Error: tableName is required for get operation");
					}
					if (input.getRowkey() == null || input.getRowkey().trim().isEmpty()) {
						return new ToolExecuteResult("Error: rowkey is required for get operation");
					}
					return new ToolExecuteResult(hbaseService.getRow(datasourceName, input.getTableName(), input.getRowkey()));
				default:
					return new ToolExecuteResult(
							"Unknown operation: " + operation + ". Supported: list, describe, scan, get");
			}
		}
		catch (Exception e) {
			log.error("HBaseQueryTool execution failed", e);
			return new ToolExecuteResult("Tool execution failed: " + e.getMessage());
		}
	}

	@Override
	public ToolStateInfo getCurrentToolStateString() {
		String stateString;
		try {
			Map<String, String> datasourceInfo = hbaseService.getAllDatasourceInfo();
			StringBuilder stateBuilder = new StringBuilder();
			stateBuilder.append("\n=== HBase Query Tool Current State ===\n");

			if (datasourceInfo.isEmpty()) {
				stateBuilder.append("No HBase connections configured.\n");
			}
			else {
				stateBuilder.append("Available HBase connections:\n");
				for (Map.Entry<String, String> entry : datasourceInfo.entrySet()) {
					stateBuilder.append(String.format("  - %s (%s)\n", entry.getKey(), entry.getValue()));
				}
			}

			stateBuilder.append("\nSupported operations:\n");
			stateBuilder.append("  - list: List all table names\n");
			stateBuilder.append("  - describe: Get table schema (requires tableName)\n");
			stateBuilder.append("  - scan: Scan table rows (requires tableName, optional limit)\n");
			stateBuilder.append("  - get: Get a single row by rowkey (requires tableName and rowkey)\n");

			stateBuilder.append("\n=== End HBase Query Tool State ===\n");
			stateString = stateBuilder.toString();
		}
		catch (Exception e) {
			log.error("Failed to get hbase query tool state", e);
			stateString = String.format("HBase query tool state error: %s", e.getMessage());
		}
		return new ToolStateInfo(null, stateString);
	}

	@Override
	public String getName() {
		return TOOL_NAME;
	}

	@Override
	public String getDescription() {
		return toolI18nService.getDescription("hbase-query");
	}

	@Override
	public String getParameters() {
		return toolI18nService.getParameters("hbase-query");
	}

	@Override
	public Class<HBaseQueryInput> getInputType() {
		return HBaseQueryInput.class;
	}

	@Override
	public void cleanup(String planId) {
		if (planId != null) {
			log.info("Cleaning up hbase query resources for plan: {}", planId);
		}
	}

	@Override
	public String getServiceGroup() {
		return "db-service";
	}

	@Override
	public boolean isSelectable() {
		return true;
	}

}
