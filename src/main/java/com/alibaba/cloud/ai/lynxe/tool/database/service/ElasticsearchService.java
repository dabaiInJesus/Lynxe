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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.indices.GetIndexResponse;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import co.elastic.clients.util.ObjectBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

/**
 * Service for Elasticsearch operations
 * Handles index listing, mapping retrieval, and query execution
 */
@Service
public class ElasticsearchService {

	private static final Logger log = LoggerFactory.getLogger(ElasticsearchService.class);

	private final Map<String, ElasticsearchClientWrapper> clientMap = new ConcurrentHashMap<>();

	/**
	 * Add Elasticsearch cluster connection
	 */
	public void addElasticsearch(String name, String hosts, String username, String password) {
		try {
			String[] hostArray = hosts.split(",");
			HttpHost[] httpHosts = new HttpHost[hostArray.length];
			for (int i = 0; i < hostArray.length; i++) {
				String host = hostArray[i].trim();
				if (!host.startsWith("http://") && !host.startsWith("https://")) {
					host = "http://" + host;
				}
				java.net.URI uri = new java.net.URI(host);
				httpHosts[i] = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
			}

			RestClient.Builder restBuilder = RestClient.builder(httpHosts);
			if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
				final String finalUsername = username;
				final String finalPassword = password;
				restBuilder.setBasicAuth(finalUsername, finalPassword);
			}

			RestClient restClient = restBuilder.build();
			ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
			ElasticsearchClient client = new ElasticsearchClient(transport);

			clientMap.put(name, new ElasticsearchClientWrapper(client, restClient));
			log.info("Added Elasticsearch connection '{}' with hosts: {}", name, hosts);
		}
		catch (Exception e) {
			log.error("Failed to add Elasticsearch connection '{}'", name, e);
		}
	}

	/**
	 * Get all index names from Elasticsearch
	 */
	public String getIndexes(String datasourceName) {
		ElasticsearchClientWrapper wrapper = clientMap.get(datasourceName);
		if (wrapper == null) {
			return "Error: Elasticsearch connection '" + datasourceName + "' not found";
		}

		try {
			GetIndexResponse response = wrapper.client.indices().get(builder -> builder.index("*"));
			Map<String, co.elastic.clients.elasticsearch.indices.get_index.IndexState> indexes = response.result();

			ObjectMapper mapper = new ObjectMapper();
			ObjectNode result = mapper.createObjectNode();
			ArrayNode indicesArray = mapper.createArrayNode();

			for (String indexName : indexes.keySet()) {
				indicesArray.add(indexName);
			}

			result.put("total", indicesArray.size());
			result.set("indexes", indicesArray);

			return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
		}
		catch (IOException e) {
			log.error("Failed to get indexes from Elasticsearch '{}'", datasourceName, e);
			return "Error: " + e.getMessage();
		}
	}

	/**
	 * Get index mapping (field definitions)
	 */
	public String getIndexMapping(String datasourceName, String indexName) {
		ElasticsearchClientWrapper wrapper = clientMap.get(datasourceName);
		if (wrapper == null) {
			return "Error: Elasticsearch connection '" + datasourceName + "' not found";
		}

		try {
			var response = wrapper.client.indices().getMapping(builder -> builder.index(indexName));
			ObjectMapper mapper = new ObjectMapper();
			ObjectNode result = mapper.createObjectNode();

			response.result().forEach((name, mapping) -> {
				ObjectNode indexNode = mapper.createObjectNode();
				indexNode.put("name", name);

				if (mapping.mappings() != null && mapping.mappings().properties() != null) {
					ObjectNode propsNode = mapper.createObjectNode();
					mapping.mappings().properties().forEach((fieldName, fieldMapping) -> {
						ObjectNode fieldNode = mapper.createObjectNode();
						fieldNode.put("type", fieldMapping.type() != null ? fieldMapping.type().jsonValue() : "unknown");
						if (fieldMapping.keyword() != null) {
							fieldNode.put("keyword", true);
						}
						if (fieldMapping.text() != null) {
							fieldNode.put("text", true);
						}
						if (fieldMapping.long_() != null) {
							fieldNode.put("long", true);
						}
						if (fieldMapping.integer() != null) {
							fieldNode.put("integer", true);
						}
						if (fieldMapping.double_() != null) {
							fieldNode.put("double", true);
						}
						if (fieldMapping.boolean_() != null) {
							fieldNode.put("boolean", true);
						}
						if (fieldMapping.date() != null) {
							fieldNode.put("date", true);
						}
						if (fieldMapping geoPoint != null) {
							fieldNode.put("geo_point", true);
						}
						propsNode.set(fieldName, fieldNode);
					});
					indexNode.set("properties", propsNode);
				}
				result.set(name, indexNode);
			});

			return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
		}
		catch (IOException e) {
			log.error("Failed to get index mapping from Elasticsearch '{}', index '{}'", datasourceName, indexName, e);
			return "Error: " + e.getMessage();
		}
	}

	/**
	 * Execute search query
	 */
	public String search(String datasourceName, String indexName, String query) {
		ElasticsearchClientWrapper wrapper = clientMap.get(datasourceName);
		if (wrapper == null) {
			return "Error: Elasticsearch connection '" + datasourceName + "' not found";
		}

		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode queryNode = null;
			if (query != null && !query.trim().isEmpty()) {
				queryNode = mapper.readTree(query);
			}

			SearchResponse<JsonNode> response;
			if (queryNode != null) {
				response = wrapper.client.search(builder -> {
					builder.index(indexName).query(q -> q.withJson(new java.io.StringReader(query)));
					return builder;
				}, JsonNode.class);
			}
			else {
				response = wrapper.client.search(builder -> builder.index(indexName).query(q -> q.matchAll(m -> m)), JsonNode.class);
			}

			ObjectNode result = mapper.createObjectNode();
			result.put("total", response.hits().total() != null ? response.hits().total().value() : response.hits().hits().size());
			result.put("took", response.took());

			ArrayNode hitsArray = mapper.createArrayNode();
			for (var hit : response.hits().hits()) {
				ObjectNode hitNode = mapper.createObjectNode();
				hitNode.put("_index", hit.index());
				hitNode.put("_id", hit.id());
				if (hit.score() != null) {
					hitNode.put("_score", hit.score());
				}
				if (hit.source() != null) {
					hitNode.set("_source", hit.source());
				}
				hitsArray.add(hitNode);
			}
			result.set("hits", hitsArray);

			return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
		}
		catch (IOException e) {
			log.error("Failed to execute search on Elasticsearch '{}', index '{}'", datasourceName, indexName, e);
			return "Error: " + e.getMessage();
		}
	}

	/**
	 * Test connection
	 */
	public boolean testConnection(String hosts, String username, String password) {
		try {
			String[] hostArray = hosts.split(",");
			HttpHost[] httpHosts = new HttpHost[hostArray.length];
			for (int i = 0; i < hostArray.length; i++) {
				String host = hostArray[i].trim();
				if (!host.startsWith("http://") && !host.startsWith("https://")) {
					host = "http://" + host;
				}
				java.net.URI uri = new java.net.URI(host);
				httpHosts[i] = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
			}

			RestClient.Builder restBuilder = RestClient.builder(httpHosts);
			if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
				restBuilder.setBasicAuth(username, password);
			}

			RestClient restClient = restBuilder.build();
			ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
			ElasticsearchClient client = new ElasticsearchClient(transport);

			return client.ping().value();
		}
		catch (Exception e) {
			log.error("Failed to test Elasticsearch connection", e);
			return false;
		}
	}

	/**
	 * Get all datasource info for display
	 */
	public Map<String, String> getAllDatasourceInfo() {
		Map<String, String> info = new HashMap<>();
		clientMap.forEach((name, wrapper) -> {
			info.put(name, "elasticsearch");
		});
		return info;
	}

	/**
	 * Get available datasource names
	 */
	public java.util.Set<String> getDataSourceNames() {
		return clientMap.keySet();
	}

	/**
	 * Wrapper to hold client and rest client for proper cleanup
	 */
	private static class ElasticsearchClientWrapper {

		final ElasticsearchClient client;

		final RestClient restClient;

		ElasticsearchClientWrapper(ElasticsearchClient client, RestClient restClient) {
			this.client = client;
			this.restClient = restClient;
		}

	}

}
