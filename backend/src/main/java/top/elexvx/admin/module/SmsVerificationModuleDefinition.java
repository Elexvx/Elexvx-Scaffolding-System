package top.elexvx.admin.module;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SmsVerificationModuleDefinition implements ModuleDefinition {
  @Override
  public String getKey() {
    return "sms";
  }

  @Override
  public String getName() {
    return "短信验证";
  }

  @Override
  public String getVersion() {
    return "1.0.0";
  }

  @Override
  public List<String> getRequiredTables() {
    return List.of("verification_sms_settings");
  }

  @Override
  public void initialize(ModuleInstallationContext context) {
    if (context.tableExists("verification_sms_settings")) return;
    context.executeSqlResource(context.resolveModuleResource("sms", "install"));
  }

  @Override
  public void uninstall(ModuleInstallationContext context) {
    if (!context.tableExists("verification_sms_settings")) return;
    context.executeSqlResourceIfExists(context.resolveModuleResource("sms", "uninstall"));
  }
}
