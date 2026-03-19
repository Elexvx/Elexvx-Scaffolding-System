package elexvx.admin.config;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSchemaInitializer implements SmartLifecycle {
  private static final Logger log = LoggerFactory.getLogger(DatabaseSchemaInitializer.class);
  private static final List<String> REQUIRED_TABLES = List.of(
    "ai_provider_settings",
    "announcements",
    "areas",
    "file_resources",
    "messages",
    "module_registry",
    "notifications",
    "operation_logs",
    "org_unit_leaders",
    "org_units",
    "role_menus",
    "role_permissions",
    "roles",
    "security_captcha_settings",
    "security_password_policy",
    "security_token_settings",
    "sensitive_page_settings",
    "sensitive_settings",
    "sensitive_words",
    "storage_settings",
    "sys_dict",
    "sys_menu_items",
    "ui_brand_settings",
    "ui_footer_settings",
    "ui_layout_settings",
    "ui_legal_settings",
    "ui_login_settings",
    "ui_system_settings",
    "ui_theme_settings",
    "user_departments",
    "user_org_units",
    "user_parameters",
    "user_roles",
    "users",
    "verification_email_settings",
    "verification_sms_settings",
    "watermark_settings"
  );

  private final DataSource dataSource;
  private volatile boolean running = false;

  @Value("${elexvx.db.schema-init.enabled:true}")
  private boolean enabled;

  public DatabaseSchemaInitializer(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public void start() {
    if (running) {
      return;
    }

    if (!enabled) {
      log.info("数据库schema初始化已禁用 (elexvx.db.schema-init.enabled=false)");
      running = true;
      return;
    }

    try (Connection connection = dataSource.getConnection()) {
      DatabaseMetaData metaData = connection.getMetaData();
      List<String> missingTables = findMissingTables(connection, REQUIRED_TABLES);
      if (!missingTables.isEmpty()) {
        String message = "数据库 schema 不完整，缺少表: " + String.join(", ", missingTables)
          + ". 请先导入 database/tdesign_init.sql 或对应数据库的初始化脚本。数据库URL: " + metaData.getURL();
        log.error(message);
        throw new IllegalStateException(message);
      }
      log.info("数据库 schema 校验通过。数据库URL: {}", metaData.getURL());
    } catch (SQLException ex) {
      String message = "数据库 schema 校验失败: " + ex.getMessage();
      log.error(message, ex);
      throw new IllegalStateException(message, ex);
    }

    running = true;
  }

  @Override
  public void stop() {
    running = false;
  }

  @Override
  public void stop(@NonNull Runnable callback) {
    stop();
    callback.run();
  }

  @Override
  public boolean isRunning() {
    return running;
  }

  @Override
  public boolean isAutoStartup() {
    return true;
  }

  @Override
  public int getPhase() {
    return Integer.MIN_VALUE;
  }

  private List<String> findMissingTables(Connection connection, List<String> requiredTables) throws SQLException {
    List<String> missing = new ArrayList<>();
    DatabaseMetaData metaData = connection.getMetaData();
    for (String table : requiredTables) {
      if (!tableExists(metaData, connection.getCatalog(), table)) {
        missing.add(table);
      }
    }
    return missing;
  }

  private boolean tableExists(DatabaseMetaData metaData, String catalog, String tableName) throws SQLException {
    try (var tables = metaData.getTables(catalog, null, "%", new String[] {"TABLE"})) {
      while (tables.next()) {
        String name = tables.getString("TABLE_NAME");
        if (name != null && name.equalsIgnoreCase(tableName)) {
          return true;
        }
      }
    }
    return false;
  }
}
