package elexvx.admin.vo;

import elexvx.admin.entity.ModuleRegistry;
import java.time.LocalDateTime;

public class ModuleRegistryResponse {
  private String moduleKey;
  private String name;
  private String version;
  private Boolean enabled;
  private String installState;
  private LocalDateTime installedAt;

  public static ModuleRegistryResponse from(ModuleRegistry registry) {
    ModuleRegistryResponse response = new ModuleRegistryResponse();
    if (registry == null) return response;
    response.setModuleKey(registry.getModuleKey());
    response.setName(registry.getName());
    response.setVersion(registry.getVersion());
    response.setInstallState(registry.getInstallState());
    response.setEnabled(Boolean.TRUE.equals(registry.getEnabled()));
    response.setInstalledAt(registry.getInstalledAt());
    return response;
  }

  public String getModuleKey() { return moduleKey; }
  public void setModuleKey(String moduleKey) { this.moduleKey = moduleKey; }
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  public String getVersion() { return version; }
  public void setVersion(String version) { this.version = version; }
  public Boolean getEnabled() { return enabled; }
  public void setEnabled(Boolean enabled) { this.enabled = enabled; }
  public String getInstallState() { return installState; }
  public void setInstallState(String installState) { this.installState = installState; }
  public LocalDateTime getInstalledAt() { return installedAt; }
  public void setInstalledAt(LocalDateTime installedAt) { this.installedAt = installedAt; }
}
