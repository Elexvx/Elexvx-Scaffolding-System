package top.elexvx.admin.service;

import top.elexvx.admin.entity.SysDict;
import top.elexvx.admin.entity.SysDictItem;
import top.elexvx.admin.exception.BusinessException;
import top.elexvx.admin.exception.ErrorCodes;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class DictionaryItemTableService {
  private static final String TABLE_PREFIX = "sys_di_";
  private static final String CODE_PATTERN = "[^a-z0-9_]";

  private static BusinessException badRequest(String message) {
    return new BusinessException(ErrorCodes.BAD_REQUEST, message);
  }

  private final DataSource dataSource;
  private final JdbcTemplate jdbcTemplate;
  private final Set<String> initializedTables = ConcurrentHashMap.newKeySet();
  private final AtomicLong idSequence = new AtomicLong(System.currentTimeMillis() * 1000L);

  private volatile String databaseKey;

  public DictionaryItemTableService(DataSource dataSource) {
    this.dataSource = dataSource;
    this.jdbcTemplate = new JdbcTemplate(dataSource);
  }

  public void ensureDictTable(SysDict dict) {
    Objects.requireNonNull(dict, "dict");
    if (!StringUtils.hasText(dict.getCode())) {
      throw badRequest("Dictionary code is required.");
    }

    String tableName = resolveTableName(dict.getCode());
    if (initializedTables.contains(tableName)) {
      return;
    }

    if (!tableExists(tableName)) {
      jdbcTemplate.execute(buildCreateTableSql(tableName));
    }
    ensureExtendedColumns(tableName);
    initializedTables.add(tableName);
  }

  public void renameDictTable(SysDict dict, String newCode) {
    Objects.requireNonNull(dict, "dict");
    if (!StringUtils.hasText(dict.getCode()) || !StringUtils.hasText(newCode)) {
      throw badRequest("Dictionary code is required.");
    }

    String oldTableName = resolveTableName(dict.getCode());
    String newTableName = resolveTableName(newCode);
    if (oldTableName.equals(newTableName)) {
      return;
    }

    ensureDictTable(dict);
    if (tableExists(newTableName)) {
      throw new IllegalStateException("Dictionary table already exists: " + newTableName);
    }

    jdbcTemplate.execute(buildCreateTableSql(newTableName));
    List<SysDictItem> items = queryAllItems(oldTableName, dict.getId());
    for (SysDictItem item : items) {
      insertIntoTable(newTableName, item);
    }
    jdbcTemplate.execute("DROP TABLE " + oldTableName);
    initializedTables.remove(oldTableName);
    initializedTables.add(newTableName);
  }

  public void dropDictTable(String dictCode) {
    if (!StringUtils.hasText(dictCode)) {
      return;
    }
    String tableName = resolveTableName(dictCode);
    if (!tableExists(tableName)) {
      initializedTables.remove(tableName);
      return;
    }
    jdbcTemplate.execute("DROP TABLE " + tableName);
    initializedTables.remove(tableName);
  }

  public boolean dictTableExists(String dictCode) {
    if (!StringUtils.hasText(dictCode)) {
      return false;
    }
    return tableExists(resolveTableName(dictCode));
  }

  public String resolvePhysicalTableName(String dictCode) {
    return resolveTableName(dictCode);
  }

  public List<SysDictItem> selectPage(
    SysDict dict,
    String keyword,
    Integer status,
    int offset,
    int limit
  ) {
    String tableName = ensureAndResolve(dict);
    StringBuilder sql = new StringBuilder()
      .append("SELECT id, label, value, value_type, status, sort, tag_color, province, city, district, created_at, updated_at ")
      .append("FROM ")
      .append(tableName)
      .append(" WHERE 1=1");
    List<Object> args = new ArrayList<>();

    if (StringUtils.hasText(keyword)) {
      String like = "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
      sql.append(" AND (LOWER(label) LIKE ? OR LOWER(value) LIKE ? OR LOWER(province) LIKE ? OR LOWER(city) LIKE ? OR LOWER(district) LIKE ?)");
      args.add(like);
      args.add(like);
      args.add(like);
      args.add(like);
      args.add(like);
    }
    if (status != null) {
      sql.append(" AND status = ?");
      args.add(status);
    }
    sql.append(" ORDER BY sort ASC, id ASC");

    String db = getDatabaseKey();
    if ("oracle".equals(db) || "sqlserver".equals(db)) {
      sql.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
      args.add(offset);
      args.add(limit);
    } else {
      sql.append(" LIMIT ? OFFSET ?");
      args.add(limit);
      args.add(offset);
    }

    return jdbcTemplate.query(
      sql.toString(),
      (rs, rowNum) -> mapItem(rs, dict.getId()),
      args.toArray()
    );
  }

  public long countByDict(SysDict dict, String keyword, Integer status) {
    String tableName = ensureAndResolve(dict);
    StringBuilder sql = new StringBuilder().append("SELECT COUNT(1) FROM ").append(tableName).append(" WHERE 1=1");
    List<Object> args = new ArrayList<>();
    if (StringUtils.hasText(keyword)) {
      String like = "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
      sql.append(" AND (LOWER(label) LIKE ? OR LOWER(value) LIKE ? OR LOWER(province) LIKE ? OR LOWER(city) LIKE ? OR LOWER(district) LIKE ?)");
      args.add(like);
      args.add(like);
      args.add(like);
      args.add(like);
      args.add(like);
    }
    if (status != null) {
      sql.append(" AND status = ?");
      args.add(status);
    }
    Long count = jdbcTemplate.queryForObject(sql.toString(), Long.class, args.toArray());
    return count == null ? 0L : count;
  }

  public List<SysDictItem> selectByDict(SysDict dict) {
    String tableName = ensureAndResolve(dict);
    String sql =
      "SELECT id, label, value, value_type, status, sort, tag_color, province, city, district, created_at, updated_at FROM " +
      tableName +
      " ORDER BY sort ASC, id ASC";
    return jdbcTemplate.query(sql, (rs, rowNum) -> mapItem(rs, dict.getId()));
  }

  public List<SysDictItem> selectEnabledByDict(SysDict dict) {
    String tableName = ensureAndResolve(dict);
    String sql =
      "SELECT id, label, value, value_type, status, sort, tag_color, province, city, district, created_at, updated_at FROM " +
      tableName +
      " WHERE status = 1 ORDER BY sort ASC, id ASC";
    return jdbcTemplate.query(sql, (rs, rowNum) -> mapItem(rs, dict.getId()));
  }

  public SysDictItem selectByDictValue(SysDict dict, String value) {
    String tableName = ensureAndResolve(dict);
    String sql =
      "SELECT id, label, value, value_type, status, sort, tag_color, province, city, district, created_at, updated_at FROM " +
      tableName +
      " WHERE value = ? " +
      buildSingleRowSuffix();
    List<SysDictItem> rows = jdbcTemplate.query(sql, (rs, rowNum) -> mapItem(rs, dict.getId()), value);
    return rows.isEmpty() ? null : rows.get(0);
  }

  public SysDictItem selectById(SysDict dict, long id) {
    String tableName = ensureAndResolve(dict);
    String sql =
      "SELECT id, label, value, value_type, status, sort, tag_color, province, city, district, created_at, updated_at FROM " +
      tableName +
      " WHERE id = ? " +
      buildSingleRowSuffix();
    List<SysDictItem> rows = jdbcTemplate.query(sql, (rs, rowNum) -> mapItem(rs, dict.getId()), id);
    return rows.isEmpty() ? null : rows.get(0);
  }

  public void insert(SysDict dict, SysDictItem item) {
    String tableName = ensureAndResolve(dict);
    if (item.getId() == null) {
      item.setId(nextId());
    }
    if (item.getCreatedAt() == null) {
      item.setCreatedAt(LocalDateTime.now());
    }
    if (item.getUpdatedAt() == null) {
      item.setUpdatedAt(item.getCreatedAt());
    }
    insertIntoTable(tableName, item);
    item.setDictId(dict.getId());
  }

  public int update(SysDict dict, SysDictItem item) {
    String tableName = ensureAndResolve(dict);
    String sql =
      "UPDATE " +
      tableName +
      " SET label = ?, value = ?, value_type = ?, status = ?, sort = ?, tag_color = ?, province = ?, city = ?, district = ?, updated_at = ? WHERE id = ?";
    return jdbcTemplate.update(
      sql,
      item.getLabel(),
      item.getValue(),
      item.getValueType(),
      item.getStatus(),
      item.getSort(),
      item.getTagColor(),
      item.getProvince(),
      item.getCity(),
      item.getDistrict(),
      toTimestamp(item.getUpdatedAt()),
      item.getId()
    );
  }

  public int deleteById(SysDict dict, long id) {
    String tableName = ensureAndResolve(dict);
    return jdbcTemplate.update("DELETE FROM " + tableName + " WHERE id = ?", id);
  }

  public int deleteAll(SysDict dict) {
    String tableName = ensureAndResolve(dict);
    return jdbcTemplate.update("DELETE FROM " + tableName);
  }

  private String ensureAndResolve(SysDict dict) {
    ensureDictTable(dict);
    return resolveTableName(dict.getCode());
  }

  private List<SysDictItem> queryAllItems(String tableName, Long dictId) {
    String sql =
      "SELECT id, label, value, value_type, status, sort, tag_color, province, city, district, created_at, updated_at FROM " +
      tableName +
      " ORDER BY sort ASC, id ASC";
    return jdbcTemplate.query(sql, (rs, rowNum) -> mapItem(rs, dictId));
  }

  private void insertIntoTable(String tableName, SysDictItem item) {
    String sql =
      "INSERT INTO " +
      tableName +
      " (id, label, value, value_type, status, sort, tag_color, province, city, district, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    jdbcTemplate.update(
      sql,
      item.getId(),
      item.getLabel(),
      item.getValue(),
      item.getValueType(),
      item.getStatus(),
      item.getSort(),
      item.getTagColor(),
      item.getProvince(),
      item.getCity(),
      item.getDistrict(),
      toTimestamp(item.getCreatedAt()),
      toTimestamp(item.getUpdatedAt())
    );
  }

  private SysDictItem mapItem(ResultSet rs, Long dictId) throws SQLException {
    SysDictItem item = new SysDictItem();
    item.setId(rs.getLong("id"));
    item.setDictId(dictId);
    item.setLabel(rs.getString("label"));
    item.setValue(rs.getString("value"));
    item.setValueType(rs.getString("value_type"));
    item.setStatus(rs.getInt("status"));
    item.setSort(rs.getInt("sort"));
    item.setTagColor(rs.getString("tag_color"));
    item.setProvince(rs.getString("province"));
    item.setCity(rs.getString("city"));
    item.setDistrict(rs.getString("district"));
    Timestamp createdAt = rs.getTimestamp("created_at");
    Timestamp updatedAt = rs.getTimestamp("updated_at");
    item.setCreatedAt(createdAt == null ? null : createdAt.toLocalDateTime());
    item.setUpdatedAt(updatedAt == null ? null : updatedAt.toLocalDateTime());
    return item;
  }

  private Timestamp toTimestamp(LocalDateTime dateTime) {
    return dateTime == null ? null : Timestamp.valueOf(dateTime);
  }

  private long nextId() {
    return idSequence.incrementAndGet();
  }

  private String buildSingleRowSuffix() {
    return switch (getDatabaseKey()) {
      case "sqlserver" -> "ORDER BY id ASC OFFSET 0 ROWS FETCH NEXT 1 ROWS ONLY";
      case "oracle" -> "ORDER BY id ASC FETCH FIRST 1 ROWS ONLY";
      default -> "ORDER BY id ASC LIMIT 1";
    };
  }

  private String buildCreateTableSql(String tableName) {
    String db = getDatabaseKey();
    return switch (db) {
      case "oracle" ->
        "CREATE TABLE " +
        tableName +
        " (" +
        "id NUMBER(19) PRIMARY KEY, " +
        "sort NUMBER(10) DEFAULT 0 NOT NULL, " +
        "label VARCHAR2(64) NOT NULL, " +
        "value VARCHAR2(128) NOT NULL UNIQUE, " +
        "value_type VARCHAR2(16) DEFAULT 'string' NOT NULL, " +
        "status NUMBER(3) DEFAULT 1 NOT NULL, " +
        "tag_color VARCHAR2(32), " +
        "province VARCHAR2(128), " +
        "city VARCHAR2(128), " +
        "district VARCHAR2(128), " +
        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, " +
        "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL" +
        ")";
      case "sqlserver" ->
        "CREATE TABLE " +
        tableName +
        " (" +
        "id BIGINT NOT NULL PRIMARY KEY, " +
        "sort INT NOT NULL DEFAULT 0, " +
        "label NVARCHAR(64) NOT NULL, " +
        "value NVARCHAR(128) NOT NULL UNIQUE, " +
        "value_type NVARCHAR(16) NOT NULL DEFAULT 'string', " +
        "status SMALLINT NOT NULL DEFAULT 1, " +
        "tag_color NVARCHAR(32) NULL, " +
        "province NVARCHAR(128) NULL, " +
        "city NVARCHAR(128) NULL, " +
        "district NVARCHAR(128) NULL, " +
        "created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(), " +
        "updated_at DATETIME2 NOT NULL DEFAULT SYSDATETIME()" +
        ")";
      case "postgresql" ->
        "CREATE TABLE IF NOT EXISTS " +
        tableName +
        " (" +
        "id BIGINT PRIMARY KEY, " +
        "sort INTEGER NOT NULL DEFAULT 0, " +
        "label VARCHAR(64) NOT NULL, " +
        "value VARCHAR(128) NOT NULL UNIQUE, " +
        "value_type VARCHAR(16) NOT NULL DEFAULT 'string', " +
        "status SMALLINT NOT NULL DEFAULT 1, " +
        "tag_color VARCHAR(32), " +
        "province VARCHAR(128), " +
        "city VARCHAR(128), " +
        "district VARCHAR(128), " +
        "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
        "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP" +
        ")";
      default ->
        "CREATE TABLE IF NOT EXISTS " +
        tableName +
        " (" +
        "id BIGINT NOT NULL PRIMARY KEY, " +
        "sort INT NOT NULL DEFAULT 0, " +
        "label VARCHAR(64) NOT NULL, " +
        "value VARCHAR(128) NOT NULL UNIQUE, " +
        "value_type VARCHAR(16) NOT NULL DEFAULT 'string', " +
        "status TINYINT NOT NULL DEFAULT 1, " +
        "tag_color VARCHAR(32), " +
        "province VARCHAR(128), " +
        "city VARCHAR(128), " +
        "district VARCHAR(128), " +
        "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
        "updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP" +
        ")";
    };
  }

  private void ensureExtendedColumns(String tableName) {
    addColumnIfMissing(tableName, "province");
    addColumnIfMissing(tableName, "city");
    addColumnIfMissing(tableName, "district");
  }

  private void addColumnIfMissing(String tableName, String columnName) {
    if (columnExists(tableName, columnName)) {
      return;
    }
    jdbcTemplate.execute(buildAddColumnSql(tableName, columnName));
  }

  private boolean columnExists(String tableName, String columnName) {
    if (!StringUtils.hasText(tableName) || !StringUtils.hasText(columnName)) {
      return false;
    }
    String targetTable = tableName.trim().toLowerCase(Locale.ROOT);
    String targetColumn = columnName.trim().toLowerCase(Locale.ROOT);
    try (Connection connection = dataSource.getConnection()) {
      DatabaseMetaData metaData = connection.getMetaData();
      try (ResultSet columns = metaData.getColumns(connection.getCatalog(), null, "%", "%")) {
        while (columns.next()) {
          String foundTable = columns.getString("TABLE_NAME");
          String foundColumn = columns.getString("COLUMN_NAME");
          if (foundTable == null || foundColumn == null) {
            continue;
          }
          if (
            targetTable.equals(foundTable.trim().toLowerCase(Locale.ROOT)) &&
            targetColumn.equals(foundColumn.trim().toLowerCase(Locale.ROOT))
          ) {
            return true;
          }
        }
      }
    } catch (SQLException ignored) {
      return false;
    }
    return false;
  }

  private String buildAddColumnSql(String tableName, String columnName) {
    String db = getDatabaseKey();
    return switch (db) {
      case "oracle" -> "ALTER TABLE " + tableName + " ADD (" + columnName + " VARCHAR2(128))";
      case "sqlserver" -> "ALTER TABLE " + tableName + " ADD " + columnName + " NVARCHAR(128) NULL";
      case "postgresql" -> "ALTER TABLE " + tableName + " ADD COLUMN IF NOT EXISTS " + columnName + " VARCHAR(128)";
      default -> "ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " VARCHAR(128) NULL";
    };
  }

  private boolean tableExists(String tableName) {
    if (!StringUtils.hasText(tableName)) {
      return false;
    }
    String target = tableName.trim().toLowerCase(Locale.ROOT);
    try (Connection connection = dataSource.getConnection()) {
      DatabaseMetaData metaData = connection.getMetaData();
      try (ResultSet tables = metaData.getTables(connection.getCatalog(), null, "%", new String[] {"TABLE"})) {
        while (tables.next()) {
          String name = tables.getString("TABLE_NAME");
          if (name != null && target.equals(name.trim().toLowerCase(Locale.ROOT))) {
            return true;
          }
        }
      }
    } catch (SQLException ignored) {
      return false;
    }
    return false;
  }

  private String getDatabaseKey() {
    if (databaseKey != null) {
      return databaseKey;
    }
    synchronized (this) {
      if (databaseKey != null) {
        return databaseKey;
      }
      databaseKey = detectDatabaseKey();
      return databaseKey;
    }
  }

  private String detectDatabaseKey() {
    try (Connection connection = dataSource.getConnection()) {
      String productName = connection.getMetaData().getDatabaseProductName();
      String normalized = productName == null ? "" : productName.toLowerCase(Locale.ROOT);
      if (normalized.contains("postgres")) {
        return "postgresql";
      }
      if (normalized.contains("oracle")) {
        return "oracle";
      }
      if (normalized.contains("sql server") || normalized.contains("microsoft")) {
        return "sqlserver";
      }
      return "mysql";
    } catch (SQLException ignored) {
      return "mysql";
    }
  }

  private String resolveTableName(String dictCode) {
    String normalized = dictCode == null ? "" : dictCode.trim().toLowerCase(Locale.ROOT);
    String safe = normalized.replaceAll(CODE_PATTERN, "_");
    if (safe.isBlank()) {
      safe = "dict";
    }
    String compact = safe.replaceAll("_+", "_");
    String prefix = compact.length() > 14 ? compact.substring(0, 14) : compact;
    return TABLE_PREFIX + prefix + "_" + shortHash(normalized);
  }

  private String shortHash(String source) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] bytes = digest.digest(source.getBytes(StandardCharsets.UTF_8));
      StringBuilder hex = new StringBuilder();
      for (int i = 0; i < 4; i++) {
        hex.append(String.format("%02x", bytes[i]));
      }
      return hex.toString();
    } catch (Exception ex) {
      throw new IllegalStateException("Failed to generate table hash.", ex);
    }
  }
}
