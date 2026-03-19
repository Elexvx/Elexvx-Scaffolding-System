package top.elexvx.admin.module;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class EmailVerificationModuleDefinition implements ModuleDefinition {
  @Override
  public String getKey() {
    return "email";
  }

  @Override
  public String getName() {
    return "邮箱验证";
  }

  @Override
  public String getVersion() {
    return "1.0.0";
  }

  @Override
  public List<String> getRequiredTables() {
    return List.of("verification_email_settings");
  }

  @Override
  public void initialize(ModuleInstallationContext context) {
    if (context.tableExists("verification_email_settings")) return;
    context.executeSqlResource(context.resolveModuleResource("email", "install"));
  }

  @Override
  public void uninstall(ModuleInstallationContext context) {
    if (!context.tableExists("verification_email_settings")) return;
    context.executeSqlResourceIfExists(context.resolveModuleResource("email", "uninstall"));
  }
}
