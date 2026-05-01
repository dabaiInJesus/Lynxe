# Multi-Database Support

Lynxe supports multiple databases through a unified abstraction layer.

## Supported Databases

| Database | Driver | Default Port | Type |
|----------|--------|--------------|------|
| MySQL | com.mysql.cj.jdbc.Driver | 3306 | JDBC |
| MariaDB | org.mariadb.jdbc.Driver | 3306 | JDBC |
| PostgreSQL | org.postgresql.Driver | 5432 | JDBC |
| Oracle | oracle.jdbc.OracleDriver | 1521 | JDBC |
| SQL Server | com.microsoft.sqlserver.jdbc.SQLServerDriver | 1433 | JDBC |
| H2 | org.h2.Driver | 9092 | JDBC |
| ClickHouse | com.clickhouse.jdbc.ClickHouseDriver | 8123 | JDBC |
| Apache Doris | com.mysql.cj.jdbc.Driver | 9030 | JDBC |
| Apache Hive | org.apache.hive.jdbc.HiveDriver | 10000 | JDBC |
| OceanBase | com.mysql.cj.jdbc.Driver | 2883 | JDBC |
| 达梦 (DM) | dm.jdbc.driver.DmDriver | 5236 | JDBC |
| 人大金仓 (KingBase) | com.kingbase8.Driver | 54321 | JDBC |
| GaussDB | com.huawei.gaussdb.jdbc.GaussDBDriver | 5432 | JDBC |
| HBase | org.apache.hbase:hbase-client | 2181 | Thrift |
| Elasticsearch | co.elastic.clients:elasticsearch-java | 9200 | REST |

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    REST API Layer                           │
│  DatasourceConfigController                                │
│  - GET /api/datasource-configs                              │
│  - GET /api/datasource-configs/supported-types              │
│  - POST /api/datasource-configs/test-connection             │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│                    Service Layer                            │
│  ┌─────────────────┐  ┌─────────────────┐                  │
│  │ DataSourceService│  │DatasourceConfig │                  │
│  │                 │  │    Service      │                  │
│  └─────────────────┘  └─────────────────┘                  │
│  ┌─────────────────┐  ┌─────────────────┐                  │
│  │ElasticsearchSvc │  │   HBaseService  │                  │
│  └─────────────────┘  └─────────────────┘                  │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│                  Database Driver Layer                      │
│  DatabaseDriverConstants                                    │
│  - DRIVER_CLASS_MAP                                         │
│  - URL_PATTERN_MAP                                          │
│  - DEFAULT_PORT_MAP                                         │
│  - URL_REGEX_MAP                                            │
│                                                              │
│  DatabaseSqlGenerator                                       │
│  - generateTableInfoSql()                                   │
│  - generateColumnInfoSql()                                  │
│  - generateIndexInfoSql()                                   │
└─────────────────────────────────────────────────────────────┘
```

## Key Components

### DatabaseDriverConstants

Central configuration class mapping database types to:
- JDBC driver class names
- Connection URL patterns
- Default ports
- URL validation patterns

```java
// Get driver class
String driver = DatabaseDriverConstants.getDriverClass("mysql");

// Build connection URL
String url = DatabaseDriverConstants.buildUrl("mysql", "localhost", 3306, "mydb");

// Validate URL
boolean valid = DatabaseDriverConstants.validateUrl("mysql", "jdbc:mysql://localhost:3306/mydb");
```

### DatabaseSqlGenerator

Generates database-specific SQL for metadata queries:

```java
// Table info SQL
String tableSql = DatabaseSqlGenerator.generateTableInfoSql("mysql", false, null);

// Column info SQL
String columnSql = DatabaseSqlGenerator.generateColumnInfoSql("postgresql", "'users', 'orders'");

// Index info SQL
String indexSql = DatabaseSqlGenerator.generateIndexInfoSql("oracle", "'users'");
```

### DataSourceService

Manages dynamic JDBC DataSource instances:

```java
// Add data source
dataSourceService.addDataSource("myds", url, username, password, driverClass, type);

// Get connection
Connection conn = dataSourceService.getConnection("myds");

// Get data source
DataSource ds = dataSourceService.getDataSource("myds");

// Get all names
Set<String> names = dataSourceService.getDataSourceNames();
```

### ElasticsearchService

Handles Elasticsearch operations:

```java
// Add ES connection
elasticsearchService.addElasticsearch("es-1", "localhost:9200", username, password);

// List indexes
String indexes = elasticsearchService.getIndexes("es-1");

// Get mapping
String mapping = elasticsearchService.getIndexMapping("es-1", "my-index");

// Search
String results = elasticsearchService.search("es-1", "my-index", "{\"query\":{\"match_all\":{}}}");
```

### HBaseService

Manages HBase connections via Thrift:

```java
// Add HBase connection via ZooKeeper
hbaseService.addHBase("hbase-1", "zk1:2181,zk2:2181", "2181", "/hbase");

// List tables
String tables = hbaseService.listTables("hbase-1");

// Describe table
String desc = hbaseService.describeTable("hbase-1", "my-table");

// Scan table (limited rows)
String scan = hbaseService.scanTable("hbase-1", "my-table", 100);

// Get row by key
String row = hbaseService.getRow("hbase-1", "my-table", "row-key-123");
```

## REST API

### Get Supported Database Types

```
GET /api/datasource-configs/supported-types
```

Response:
```json
{
  "types": ["mysql", "postgresql", "oracle", ...],
  "defaultPorts": {
    "mysql": 3306,
    "postgresql": 5432,
    ...
  }
}
```

### Get URL Pattern for Type

```
GET /api/datasource-configs/url-pattern/{type}
```

Response:
```json
{
  "type": "mysql",
  "urlPattern": "jdbc:mysql://{host}:{port}/{database}?...",
  "defaultPort": 3306,
  "driverClass": "com.mysql.cj.jdbc.Driver"
}
```

### Test Connection

```
POST /api/datasource-configs/test-connection
Content-Type: application/json

{
  "name": "test-mysql",
  "type": "mysql",
  "url": "jdbc:mysql://localhost:3306/testdb",
  "driverClassName": "com.mysql.cj.jdbc.Driver",
  "username": "root",
  "password": "password"
}
```

Response:
```json
{
  "success": true,
  "message": "Connection test successful"
}
```

## Adding New Database Support

1. Add driver dependency in `pom.xml`
2. Add entries to `DatabaseDriverConstants`:
   - `DRIVER_CLASS_MAP`
   - `URL_PATTERN_MAP`
   - `DEFAULT_PORT_MAP`
   - `URL_REGEX_MAP`
3. Add SQL generation methods to `DatabaseSqlGenerator`:
   - `generateXxxTableInfoSql()`
   - `generateXxxColumnInfoSql()`
   - `generateXxxIndexInfoSql()`
4. Add unit tests

## Configuration

Database drivers are included as runtime dependencies. Ensure the `pom.xml` includes the necessary JDBC driver:

```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```
