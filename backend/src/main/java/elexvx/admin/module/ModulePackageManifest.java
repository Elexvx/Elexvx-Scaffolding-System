package elexvx.admin.module;

import java.util.List;

public class ModulePackageManifest {
  private String key;
  private String name;
  private String version;
  private Boolean enabledByDefault;
  private List<String> requiredTables;
  private FrontendSpec frontend;
  private BackendSpec backend;

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public Boolean getEnabledByDefault() {
    return enabledByDefault;
  }

  public void setEnabledByDefault(Boolean enabledByDefault) {
    this.enabledByDefault = enabledByDefault;
  }

  public List<String> getRequiredTables() {
    return requiredTables;
  }

  public void setRequiredTables(List<String> requiredTables) {
    this.requiredTables = requiredTables;
  }

  public FrontendSpec getFrontend() {
    return frontend;
  }

  public void setFrontend(FrontendSpec frontend) {
    this.frontend = frontend;
  }

  public BackendSpec getBackend() {
    return backend;
  }

  public void setBackend(BackendSpec backend) {
    this.backend = backend;
  }

  public static class FrontendSpec {
    private String type;
    private String basePath;
    private String index;

    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }

    public String getBasePath() {
      return basePath;
    }

    public void setBasePath(String basePath) {
      this.basePath = basePath;
    }

    public String getIndex() {
      return index;
    }

    public void setIndex(String index) {
      this.index = index;
    }
  }

  public static class BackendSpec {
    private String type;
    private String startScript;
    private String basePath;
    private Boolean autoInstallDependencies;

    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }

    public String getStartScript() {
      return startScript;
    }

    public void setStartScript(String startScript) {
      this.startScript = startScript;
    }

    public String getBasePath() {
      return basePath;
    }

    public void setBasePath(String basePath) {
      this.basePath = basePath;
    }

    public Boolean getAutoInstallDependencies() {
      return autoInstallDependencies;
    }

    public void setAutoInstallDependencies(Boolean autoInstallDependencies) {
      this.autoInstallDependencies = autoInstallDependencies;
    }
  }
}
