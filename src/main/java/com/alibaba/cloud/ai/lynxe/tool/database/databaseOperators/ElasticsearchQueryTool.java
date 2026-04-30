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
import com.alibaba.cloud.ai.lynxe.tool.database.service.ElasticsearchService;
import com.alibaba.cloud.ai.lynxe.tool.i18n.ToolI18nService;

/**
 * Elasticsearch query tool for executing search queries on ES indices
 */
public class ElasticsearchQueryTool extends AbstractBaseTool<ElasticsearchQueryTool.EsQueryInput> {

	private static final Logger log = LoggerFactory.getLogger(ElasticsearchQueryTool.class);

	private static final String TOOL_NAME = "elasticsearch-query";

	/**
	 * Input class for Elasticsearch query operations
	 */
	public static class EsQueryInput {

		private String indexName;

		private String query;

		private String datasourceName;

		private String operation;

		// Getters and setters
		public String getIndexName() {
			return indexName;
		}

		public void setIndexName(String indexName) {
			this.indexName = indexName;
		}

		public String getQuery() {
			return query;
		}

		public void setQuery(String query) {
			this.query = query;
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

	}

	private final ElasticsearchService elasticsearchService;

	private final ToolI18nService toolI18nService;

	public ElasticsearchQueryTool(ElasticsearchService elasticsearchService, ToolI18nService toolI18nService) {
		this.elasticsearchService = elasticsearchService;
		this.toolI18nService = toolI18nService;
	}

	@Override
	public ToolExecuteResult run(EsQueryInput input) {
		log.info("ElasticsearchQueryTool request: index={}, operation={}", input.getIndexName(), input.getOperation());
		try {
			String operation = input.getOperation();
			String datasourceName = input.getDatasourceName();

			if (operation == null) {
				operation = "search";
			}

			switch (operation.toLowerCase()) {
				case "list_indexes":
				case "list":
					return new ToolExecuteResult(elasticsearchService.getIndexes(datasourceName));
				case "mapping":
				case "describe":
					if (input.getIndexName() == null || input.getIndexName().trim().isEmpty()) {
						return new ToolExecuteResult("Error: indexName is required for mapping operation");
					}
					return new ToolExecuteResult(elasticsearchService.getIndexMapping(datasourceName, input.getIndexName()));
				case "search":
				case "query":
				default:
					if (input.getIndexName() == null || input.getIndexName().trim().isEmpty()) {
						return new ToolExecuteResult("Error: indexName is required for search operation");
					}
					return new ToolExecuteResult(elasticsearchService.search(datasourceName, input.getIndexName(), input.getQuery()));
			}
		}
		catch (Exception e) {
			log.error("ElasticsearchQueryTool execution failed", e);
			return new ToolExecuteResult("Tool execution failed: " + e.getMessage());
		}
	}

	@Override
	public ToolStateInfo getCurrentToolStateString() {
		String stateString;
		try {
			Map<String, String> datasourceInfo = elasticsearchService.getAllDatasourceInfo();
			StringBuilder stateBuilder = new StringBuilder();
			stateBuilder.append("\n=== Elasticsearch Query Tool Current State ===\n");

			if (datasourceInfo.isEmpty()) {
				stateBuilder.append("No Elasticsearch connections configured.\n");
			}
			else {
				stateBuilder.append("Available Elasticsearch connections:\n");
				for (Map.Entry<String, String> entry : datasourceInfo.entrySet()) {
					stateBuilder.append(String.format("  - %s (%s)\n", entry.getKey(), entry.getValue()));
				}
			}

			stateBuilder.append("\nSupported operations:\n");
			stateBuilder.append("  - list_indexes: List all index names\n");
			stateBuilder.append("  - mapping: Get index field mappings (requires indexName)\n");
			stateBuilder.append("  - search: Execute search query (requires indexName, optional query)\n");

			stateBuilder.append("\n=== End Elasticsearch Query Tool State ===\n");
			stateString = stateBuilder.toString();
		}
		catch (Exception e) {
			log.error("Failed to get elasticsearch query tool state", e);
			stateString = String.format("Elasticsearch query tool state error: %s", e.getMessage());
		}
		return new ToolStateInfo(null, stateString);
	}

	@Override
	public String getName() {
		return TOOL_NAME;
	}

	@Override
	public String getDescription() {
		return toolI18nService.getDescription("elasticsearch-query");
	}

	@Override
	public String getParameters() {
		return toolI18nService.getParameters("elasticsearch-query");
	}

	@Override
	public Class<EsQueryInput> getInputType() {
		return EsQueryInput.class;
	}

	@Override
	public void cleanup(String planId) {
		if (planId != null) {
			log.info("Cleaning up elasticsearch query resources for plan: {}", planId);
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
