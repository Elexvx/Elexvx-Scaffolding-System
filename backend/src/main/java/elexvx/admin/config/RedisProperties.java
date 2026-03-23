package elexvx.admin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "elexvx.redis")
public class RedisProperties {
  private boolean enabled = false;
  private boolean clearOnStartup = false;
  private boolean allowClearOnStartupInProd = false;
  private String host = "localhost";
  private int port = 6379;
  private String password = "";
  private int timeout = 2000;
  private int database = 0;

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public boolean isClearOnStartup() {
    return clearOnStartup;
  }

  public void setClearOnStartup(boolean clearOnStartup) {
    this.clearOnStartup = clearOnStartup;
  }

  public boolean isAllowClearOnStartupInProd() {
    return allowClearOnStartupInProd;
  }

  public void setAllowClearOnStartupInProd(boolean allowClearOnStartupInProd) {
    this.allowClearOnStartupInProd = allowClearOnStartupInProd;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public int getTimeout() {
    return timeout;
  }

  public void setTimeout(int timeout) {
    this.timeout = timeout;
  }

  public int getDatabase() {
    return database;
  }

  public void setDatabase(int database) {
    this.database = database;
  }
}
