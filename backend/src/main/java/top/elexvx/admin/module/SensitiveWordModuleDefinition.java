package top.elexvx.admin.module;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SensitiveWordModuleDefinition implements ModuleDefinition {
  @Override
  public String getKey() {
    return "sensitive";
  }

  @Override
  public String getName() {
    return "敏感词拦截";
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
    return List.of("sensitive_words", "sensitive_page_settings", "sensitive_settings");
  }

  @Override
  public void initialize(ModuleInstallationContext context) {
    if (context.tableExists("sensitive_words")) return;
    context.executeSqlResource(context.resolveModuleResource(getKey(), "install"));
  }

  @Override
  public void uninstall(ModuleInstallationContext context) {
    if (!context.tableExists("sensitive_words") && !context.tableExists("sensitive_settings")) return;
    context.executeSqlResourceIfExists(context.resolveModuleResource(getKey(), "uninstall"));
  }
}
