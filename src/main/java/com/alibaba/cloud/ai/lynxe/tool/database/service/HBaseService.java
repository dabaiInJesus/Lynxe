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
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.ClusterStatus;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableInfo;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.FirstKeyOnlyFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.cloud.ai.lynxe.tool.database.model.vo.DatasourceConfigVO;

/**
 * Service for HBase operations via Thrift protocol
 * Handles table listing, scanning, and data retrieval
 */
@Service
public class HBaseService {

	private static final Logger log = LoggerFactory.getLogger(HBaseService.class);

	private final Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

	private final Map<String, String> zkQuorumMap = new ConcurrentHashMap<>();

	/**
	 * Add HBase cluster connection
	 */
	public void addHBase(String name, String zkQuorum, String zkPort, String zkNode) {
		try {
			Configuration config = HBaseConfiguration.create();
			config.set("hbase.zookeeper.quorum", zkQuorum);
			if (zkPort != null && !zkPort.isEmpty()) {
				config.set("hbase.zookeeper.property.clientPort", zkPort);
			}
			if (zkNode != null && !zkNode.isEmpty()) {
				config.set("zookeeper.znode.parent", zkNode);
			}

			Connection connection = ConnectionFactory.createConnection(config);
			connectionMap.put(name, connection);
			zkQuorumMap.put(name, zkQuorum);
			log.info("Added HBase connection '{}' with zookeeper: {}", name, zkQuorum);
		}
		catch (IOException e) {
			log.error("Failed to add HBase connection '{}'", name, e);
		}
	}

	/**
	 * Add HBase connection from datasource config
	 */
	public void addHBase(String name, DatasourceConfigVO config) {
		String url = config.getUrl();
		// HBase ZooKeeper URL format: zookeeper://zk1:2181,zk2:2181/hbase
		String zkQuorum;
		String zkPort = "2181";
		String zkNode = "/hbase";

		if (url != null && url.startsWith("zookeeper://")) {
			String rest = url.substring("zookeeper://".length());
			int slashIdx = rest.indexOf('/');
			String hosts;
			if (slashIdx > 0) {
				hosts = rest.substring(0, slashIdx);
				zkNode = rest.substring(slashIdx);
			}
			else {
				hosts = rest;
			}

			String[] hostParts = hosts.split(":");
			zkQuorum = hostParts[0];
			if (hostParts.length > 1) {
				zkPort = hostParts[1];
			}
		}
		else {
			// Default fallback
			zkQuorum = config.getUrl();
		}

		addHBase(name, zkQuorum, zkPort, zkNode);
	}

	/**
	 * List all table names
	 */
	public String listTables(String datasourceName) {
		Connection connection = connectionMap.get(datasourceName);
		if (connection == null) {
			return "Error: HBase connection '" + datasourceName + "' not found";
		}

		try (Admin admin = connection.getAdmin()) {
			TableName[] tableNames = admin.listTableNames();
			StringBuilder sb = new StringBuilder();
			sb.append("{\n");
			sb.append("  \"total\": ").append(tableNames.length).append(",\n");
			sb.append("  \"tables\": [\n");
			for (int i = 0; i < tableNames.length; i++) {
				TableName tn = tableNames[i];
				sb.append("    \"").append(tn.getNameAsString()).append("\"");
				if (i < tableNames.length - 1) {
					sb.append(",");
				}
				sb.append("\n");
			}
			sb.append("  ]\n");
			sb.append("}");
			return sb.toString();
		}
		catch (IOException e) {
			log.error("Failed to list tables from HBase '{}'", datasourceName, e);
			return "Error: " + e.getMessage();
		}
	}

	/**
	 * Get table description
	 */
	public String describeTable(String datasourceName, String tableName) {
		Connection connection = connectionMap.get(datasourceName);
		if (connection == null) {
			return "Error: HBase connection '" + datasourceName + "' not found";
		}

		try (Admin admin = connection.getAdmin()) {
			TableName tn = TableName.valueOf(tableName);
			if (!admin.tableExists(tn)) {
				return "Error: Table '" + tableName + "' does not exist";
			}

			org.apache.hadoop.hbase.TableDescriptor tableDesc = admin.getTableDescriptor(tn);
			StringBuilder sb = new StringBuilder();
			sb.append("{\n");
			sb.append("  \"tableName\": \"").append(tableName).append("\",\n");
			sb.append("  \"regionCount\": ").append(admin.getRegions(tn).size()).append(",\n");
			sb.append("  \"columnFamilies\": [\n");

			var columnFamilies = tableDesc.getColumnFamilies();
			for (int i = 0; i < columnFamilies.length; i++) {
				var cf = columnFamilies[i];
				sb.append("    {\n");
				sb.append("      \"name\": \"").append(Bytes.toString(cf.getName())).append("\",\n");
				sb.append("      \"bloomFilter\": \"").append(cf.getBloomFilterType()).append("\",\n");
				sb.append("      \"blockCacheEnabled\": ").append(cf.isBlockCacheEnabled()).append(",\n");
				sb.append("      \"blockSize\": ").append(cf.getBlocksize()).append(",\n");
				sb.append("      \"compression\": \"").append(cf.getCompressionType()).append("\",\n");
				sb.append("      \"timeToLive\": ").append(cf.getTimeToLive()).append("\n");
				sb.append("    }");
				if (i < columnFamilies.length - 1) {
					sb.append(",");
				}
				sb.append("\n");
			}

			sb.append("  ]\n");
			sb.append("}");
			return sb.toString();
		}
		catch (IOException e) {
			log.error("Failed to describe table '{}' from HBase '{}'", tableName, datasourceName, e);
			return "Error: " + e.getMessage();
		}
	}

	/**
	 * Scan table data (limited)
	 */
	public String scanTable(String datasourceName, String tableName, int limit) {
		Connection connection = connectionMap.get(datasourceName);
		if (connection == null) {
			return "Error: HBase connection '" + datasourceName + "' not found";
		}

		if (limit <= 0 || limit > 1000) {
			limit = 100;
		}

		try (Table table = connection.getTable(TableName.valueOf(tableName))) {
			Scan scan = new Scan();
			scan.setFilter(new FirstKeyOnlyFilter());
			scan.setLimit(limit);

			var scanner = table.getScanner(scan);
			var results = scanner.next(limit);
			scanner.close();

			StringBuilder sb = new StringBuilder();
			sb.append("{\n");
			sb.append("  \"tableName\": \"").append(tableName).append("\",\n");
			sb.append("  \"rowCount\": ").append(results.size()).append(",\n");
			sb.append("  \"rows\": [\n");

			for (int i = 0; i < results.size(); i++) {
				Result result = results.get(i);
				sb.append("    {\n");
				sb.append("      \"rowkey\": \"").append(Bytes.toString(result.getRow())).append("\",\n");

				NavigableMap<byte[], byte[]> familyMap = result.getNoVersionMap();
				sb.append("      \"columns\": {");
				boolean first = true;
				for (Map.Entry<byte[], byte[]> familyEntry : familyMap.entrySet()) {
					String family = Bytes.toString(familyEntry.getKey());
					Map<byte[], byte[]> columns = familyEntry.getValue();
					for (Map.Entry<byte[], byte[]> colEntry : columns.entrySet()) {
						if (!first) {
							sb.append(",");
						}
						String colName = Bytes.toString(colEntry.getKey());
						String colValue = Bytes.toString(colEntry.getValue());
						sb.append("\"").append(family).append(":").append(colName).append("\": \"")
								.append(colValue.replace("\"", "\\\"")).append("\"");
						first = false;
					}
				}
				sb.append("}\n");
				sb.append("    }");
				if (i < results.size() - 1) {
					sb.append(",");
				}
				sb.append("\n");
			}

			sb.append("  ]\n");
			sb.append("}");
			return sb.toString();
		}
		catch (IOException e) {
			log.error("Failed to scan table '{}' from HBase '{}'", tableName, datasourceName, e);
			return "Error: " + e.getMessage();
		}
	}

	/**
	 * Get a single row by rowkey
	 */
	public String getRow(String datasourceName, String tableName, String rowkey) {
		Connection connection = connectionMap.get(datasourceName);
		if (connection == null) {
			return "Error: HBase connection '" + datasourceName + "' not found";
		}

		try (Table table = connection.getTable(TableName.valueOf(tableName))) {
			Get get = new Get(Bytes.toBytes(rowkey));
			Result result = table.get(get);

			if (result.isEmpty()) {
				return "{\"rowkey\": \"" + rowkey + "\", \"found\": false}";
			}

			StringBuilder sb = new StringBuilder();
			sb.append("{\n");
			sb.append("  \"rowkey\": \"").append(rowkey).append("\",\n");
			sb.append("  \"found\": true,\n");
			sb.append("  \"columns\": {");

			NavigableMap<byte[], byte[]> familyMap = result.getNoVersionMap();
			boolean first = true;
			for (Map.Entry<byte[], byte[]> familyEntry : familyMap.entrySet()) {
				String family = Bytes.toString(familyEntry.getKey());
				Map<byte[], byte[]> columns = familyEntry.getValue();
				for (Map.Entry<byte[], byte[]> colEntry : columns.entrySet()) {
					if (!first) {
						sb.append(",");
					}
					String colName = Bytes.toString(colEntry.getKey());
					String colValue = Bytes.toString(colEntry.getValue());
					sb.append("\"").append(family).append(":").append(colName).append("\": \"")
							.append(colValue.replace("\"", "\\\"")).append("\"");
					first = false;
				}
			}

			sb.append("}\n");
			sb.append("}");
			return sb.toString();
		}
		catch (IOException e) {
			log.error("Failed to get row '{}' from HBase '{}', table '{}'", rowkey, datasourceName, tableName, e);
			return "Error: " + e.getMessage();
		}
	}

	/**
	 * Test connection
	 */
	public boolean testConnection(String zkQuorum, String zkPort, String zkNode) {
		try {
			Configuration config = HBaseConfiguration.create();
			config.set("hbase.zookeeper.quorum", zkQuorum);
			if (zkPort != null && !zkPort.isEmpty()) {
				config.set("hbase.zookeeper.property.clientPort", zkPort);
			}
			if (zkNode != null && !zkNode.isEmpty()) {
				config.set("zookeeper.znode.parent", zkNode);
			}

			try (Connection connection = ConnectionFactory.createConnection(config);
					Admin admin = connection.getAdmin()) {
				ClusterStatus status = admin.getClusterStatus();
				return status.getServers().size() > 0;
			}
		}
		catch (Exception e) {
			log.error("Failed to test HBase connection", e);
			return false;
		}
	}

	/**
	 * Get all datasource info
	 */
	public Map<String, String> getAllDatasourceInfo() {
		Map<String, String> info = new HashMap<>();
		zkQuorumMap.forEach((name, zkQuorum) -> info.put(name, "hbase:" + zkQuorum));
		return info;
	}

	/**
	 * Get available datasource names
	 */
	public java.util.Set<String> getDataSourceNames() {
		return connectionMap.keySet();
	}

}
