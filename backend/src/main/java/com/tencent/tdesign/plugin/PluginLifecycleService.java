package com.tencent.tdesign.plugin;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import org.flywaydb.core.Flyway;
import org.pf4j.DefaultPluginManager;
import org.pf4j.PluginWrapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "tdesign.plugins", name = "enabled", havingValue = "true")
public class PluginLifecycleService {
  private static final Pattern PLUGIN_ID_PATTERN = Pattern.compile("^[A-Za-z0-9._-]{1,64}$");
  private static final Pattern SCHEMA_NAME_PATTERN = Pattern.compile("^[a-z0-9_]{1,64}$");

  private final JdbcTemplate jdbcTemplate;
  private final Map<String, PluginLifecycleState> states = new ConcurrentHashMap<>();
  private final DefaultPluginManager pluginManager = new DefaultPluginManager();

  public PluginLifecycleService(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public PluginStatusResponse installAndEnable(PluginPackageService.PluginInstallArtifact artifact) {
    String id = requirePluginId(artifact.manifest().getId());
    states.put(id, PluginLifecycleState.VERIFIED);
    String schema = toSchemaName(id);
    jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS " + schema);
    Flyway flyway = Flyway.configure()
      .dataSource(jdbcTemplate.getDataSource())
      .schemas(schema)
      .table("flyway_plugin_history")
      .locations("filesystem:" + artifact.packageFile().getParent().resolve("database/migrations"))
      .load();
    flyway.migrate();
    states.put(id, PluginLifecycleState.INSTALLED);

    String loadedPluginId = pluginManager.loadPlugin(artifact.packageFile());
    PluginWrapper wrapper = pluginManager.getPlugin(loadedPluginId);
    pluginManager.startPlugin(loadedPluginId);
    states.put(id, PluginLifecycleState.ENABLED);
    return new PluginStatusResponse(id, artifact.manifest().getVersion(), states.get(id).name(), wrapper.getDescriptor().getPluginDescription());
  }

  public PluginStatusResponse disable(String pluginId) {
    String safePluginId = requirePluginId(pluginId);
    var plugin = pluginManager.getPlugin(safePluginId);
    if (plugin != null) {
      pluginManager.stopPlugin(safePluginId);
    }
    states.put(safePluginId, PluginLifecycleState.DISABLED);
    return new PluginStatusResponse(safePluginId, null, PluginLifecycleState.DISABLED.name(), "disabled");
  }

  private String requirePluginId(String rawId) {
    String id = rawId == null ? "" : rawId.trim();
    if (!PLUGIN_ID_PATTERN.matcher(id).matches()) {
      throw new IllegalArgumentException("插件 ID 非法");
    }
    return id;
  }

  private String toSchemaName(String pluginId) {
    String normalized = pluginId
      .trim()
      .toLowerCase(Locale.ROOT)
      .replace('-', '_')
      .replace('.', '_');
    String schema = "plug_" + normalized;
    if (!SCHEMA_NAME_PATTERN.matcher(schema).matches()) {
      throw new IllegalArgumentException("插件 schema 非法");
    }
    return schema;
  }

  public record PluginStatusResponse(String pluginId, String version, String state, String detail) {}
}
