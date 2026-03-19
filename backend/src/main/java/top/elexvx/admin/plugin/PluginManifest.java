package top.elexvx.admin.plugin;

import java.util.ArrayList;
import java.util.List;

public class PluginManifest {
  private String id;
  private String version;
  private String vendor;
  private String backendEntry;
  private String frontendEntry;
  private List<String> permissions = new ArrayList<>();

  public String getId() { return id; }
  public void setId(String id) { this.id = id; }
  public String getVersion() { return version; }
  public void setVersion(String version) { this.version = version; }
  public String getVendor() { return vendor; }
  public void setVendor(String vendor) { this.vendor = vendor; }
  public String getBackendEntry() { return backendEntry; }
  public void setBackendEntry(String backendEntry) { this.backendEntry = backendEntry; }
  public String getFrontendEntry() { return frontendEntry; }
  public void setFrontendEntry(String frontendEntry) { this.frontendEntry = frontendEntry; }
  public List<String> getPermissions() { return permissions; }
  public void setPermissions(List<String> permissions) { this.permissions = permissions; }
}
