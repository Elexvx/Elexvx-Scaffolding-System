package top.elexvx.admin.config;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
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
    running = true;

    if (!enabled) {
      log.info("数据库schema初始化已禁用 (elexvx.db.schema-init.enabled=false)");
      return;
    }

    try (Connection connection = dataSource.getConnection()) {
      DatabaseMetaData metaData = connection.getMetaData();
      log.info("数据库schema初始化已跳过，请使用 database/ 下的初始化脚本。数据库URL: {}", metaData.getURL());
    } catch (SQLException ex) {
      log.warn("数据库schema初始化跳过（数据库不可用或权限不足）: {}", ex.getMessage());
    }
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
}
