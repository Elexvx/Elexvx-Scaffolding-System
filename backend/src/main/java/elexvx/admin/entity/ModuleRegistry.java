package elexvx.admin.entity;

import java.time.LocalDateTime;

public class ModuleRegistry {
  private String moduleKey;
  private String name;
  private String version;
  private Boolean enabled;
  private String installState;
  private LocalDateTime installedAt;

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
