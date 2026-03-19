package elexvx.admin.module;

import java.util.List;

public class ExternalScriptModuleDefinition implements ModuleDefinition {
  private final ModulePackageManifest manifest;

  public ExternalScriptModuleDefinition(ModulePackageManifest manifest) {
    this.manifest = manifest;
  }

  @Override
  public String getKey() {
    return manifest == null ? null : manifest.getKey();
  }

  @Override
  public String getName() {
    return manifest == null ? null : manifest.getName();
  }

  @Override
  public String getVersion() {
    return manifest == null ? null : manifest.getVersion();
  }

  @Override
  public boolean isEnabledByDefault() {
    return manifest != null && Boolean.TRUE.equals(manifest.getEnabledByDefault());
  }

  @Override
  public List<String> getRequiredTables() {
    if (manifest == null || manifest.getRequiredTables() == null) return List.of();
    return manifest.getRequiredTables();
  }

  @Override
  public void initialize(ModuleInstallationContext context) {
    context.executeSqlResource(context.resolveModuleResource(getKey(), "install"));
  }

  @Override
  public void uninstall(ModuleInstallationContext context) {
    context.executeSqlResourceIfExists(context.resolveModuleResource(getKey(), "uninstall"));
  }
}
