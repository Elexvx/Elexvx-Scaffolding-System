package top.elexvx.admin.module;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AiSettingModuleDefinition implements ModuleDefinition {
  @Override
  public String getKey() {
    return "ai";
  }

  @Override
  public String getName() {
    return "AI 设置";
  }

  @Override
  public String getVersion() {
    return "1.0.0";
  }

  @Override
  public boolean isEnabledByDefault() {
    return false;
  }

  @Override
  public List<String> getRequiredTables() {
    return List.of("ai_provider_settings");
  }

  @Override
  public void initialize(ModuleInstallationContext context) {
    if (context.tableExists("ai_provider_settings")) return;
    context.executeSqlResource(context.resolveModuleResource(getKey(), "install"));
  }

  @Override
  public void uninstall(ModuleInstallationContext context) {
    if (!context.tableExists("ai_provider_settings")) return;
    context.executeSqlResourceIfExists(context.resolveModuleResource(getKey(), "uninstall"));
  }
}
