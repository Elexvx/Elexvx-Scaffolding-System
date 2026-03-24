package elexvx.admin.module;

import java.util.List;

public interface ModuleDefinition {
  String getKey();
  String getName();
  String getVersion();

  default boolean isEnabledByDefault() {
    return true;
  }

  default List<String> getRequiredTables() {
    return List.of();
  }

  default List<String> getMigrationResources() {
    return List.of();
  }

  default void initialize(ModuleInstallationContext context) {}

  default void uninstall(ModuleInstallationContext context) {}
}
