package top.elexvx.admin.config.properties;

import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 主配置读取 elexvx.*，旧 tdesign.* 通过环境后处理器兼容，后续版本再移除旧键。
 */
@ConfigurationProperties(prefix = "elexvx")
public class ElexvxCoreProperties {
  private final Db db = new Db();
  private final System system = new System();
  private final File file = new File();
  private final Security security = new Security();
  private final Plugins plugins = new Plugins();
  private final Modules modules = new Modules();
  private final Redis redis = new Redis();
  private final Netty netty = new Netty();
  private final Web web = new Web();
  private final Menu menu = new Menu();
  private final User user = new User();

  public Db getDb() { return db; }
  public System getSystem() { return system; }
  public File getFile() { return file; }
  public Security getSecurity() { return security; }
  public Plugins getPlugins() { return plugins; }
  public Modules getModules() { return modules; }
  public Redis getRedis() { return redis; }
  public Netty getNetty() { return netty; }
  public Web getWeb() { return web; }
  public Menu getMenu() { return menu; }
  public User getUser() { return user; }

  public static class Db {
    private String type = "mysql";
    private String dialect = "mysql";
    private Map<String, DbTemplate> templates = new HashMap<>();
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getDialect() { return dialect; }
    public void setDialect(String dialect) { this.dialect = dialect; }
    public Map<String, DbTemplate> getTemplates() { return templates; }
    public void setTemplates(Map<String, DbTemplate> templates) { this.templates = templates; }
  }
  public static class DbTemplate {
    private String url; private String driverClassName; private String dialect;
    public String getUrl() { return url; } public void setUrl(String url) { this.url = url; }
    public String getDriverClassName() { return driverClassName; } public void setDriverClassName(String driverClassName) { this.driverClassName = driverClassName; }
    public String getDialect() { return dialect; } public void setDialect(String dialect) { this.dialect = dialect; }
  }
  public static class System {
    private String version = "1.0.0"; private String updateCheckUrl = "";
    public String getVersion() { return version; } public void setVersion(String version) { this.version = version; }
    public String getUpdateCheckUrl() { return updateCheckUrl; } public void setUpdateCheckUrl(String updateCheckUrl) { this.updateCheckUrl = updateCheckUrl; }
  }
  public static class File {
    private String tokenSecret = ""; private long tokenTtlSeconds = 600; private final Upload upload = new Upload();
    public String getTokenSecret() { return tokenSecret; } public void setTokenSecret(String tokenSecret) { this.tokenSecret = tokenSecret; }
    public long getTokenTtlSeconds() { return tokenTtlSeconds; } public void setTokenTtlSeconds(long tokenTtlSeconds) { this.tokenTtlSeconds = tokenTtlSeconds; }
    public Upload getUpload() { return upload; }
    public static class Upload { private long maxFileSizeMb = 100; public long getMaxFileSizeMb() { return maxFileSizeMb; } public void setMaxFileSizeMb(long maxFileSizeMb) { this.maxFileSizeMb = maxFileSizeMb; } }
  }
  public static class Security {
    private boolean allowPasswordInQuery = false; private String fieldSecret = ""; private final RateLimit rateLimit = new RateLimit();
    public boolean isAllowPasswordInQuery() { return allowPasswordInQuery; } public void setAllowPasswordInQuery(boolean allowPasswordInQuery) { this.allowPasswordInQuery = allowPasswordInQuery; }
    public String getFieldSecret() { return fieldSecret; } public void setFieldSecret(String fieldSecret) { this.fieldSecret = fieldSecret; }
    public RateLimit getRateLimit() { return rateLimit; }
    public static class RateLimit {
      private int loginPerMinute = 10; private int loginFailThreshold = 5; private int smsEmailPerMinute = 3; private int smsEmailPerDay = 20; private int uploadRequestsPerMinute = 240; private int uploadFilesPerDay = 200; private long uploadBytesPerDayMb = 2048;
      public int getLoginPerMinute() { return loginPerMinute; } public void setLoginPerMinute(int loginPerMinute) { this.loginPerMinute = loginPerMinute; }
      public int getLoginFailThreshold() { return loginFailThreshold; } public void setLoginFailThreshold(int loginFailThreshold) { this.loginFailThreshold = loginFailThreshold; }
      public int getSmsEmailPerMinute() { return smsEmailPerMinute; } public void setSmsEmailPerMinute(int smsEmailPerMinute) { this.smsEmailPerMinute = smsEmailPerMinute; }
      public int getSmsEmailPerDay() { return smsEmailPerDay; } public void setSmsEmailPerDay(int smsEmailPerDay) { this.smsEmailPerDay = smsEmailPerDay; }
      public int getUploadRequestsPerMinute() { return uploadRequestsPerMinute; } public void setUploadRequestsPerMinute(int uploadRequestsPerMinute) { this.uploadRequestsPerMinute = uploadRequestsPerMinute; }
      public int getUploadFilesPerDay() { return uploadFilesPerDay; } public void setUploadFilesPerDay(int uploadFilesPerDay) { this.uploadFilesPerDay = uploadFilesPerDay; }
      public long getUploadBytesPerDayMb() { return uploadBytesPerDayMb; } public void setUploadBytesPerDayMb(long uploadBytesPerDayMb) { this.uploadBytesPerDayMb = uploadBytesPerDayMb; }
    }
  }
  public static class Plugins { private boolean enabled = false; private String packageDir = "data/plugins"; public boolean isEnabled() { return enabled; } public void setEnabled(boolean enabled) { this.enabled = enabled; } public String getPackageDir() { return packageDir; } public void setPackageDir(String packageDir) { this.packageDir = packageDir; } }
  public static class Modules {
    private final Toggle sms = new Toggle(); private final Toggle email = new Toggle(); private final Toggle captcha = new Toggle(); private final Backend backend = new Backend();
    public Toggle getSms() { return sms; } public Toggle getEmail() { return email; } public Toggle getCaptcha() { return captcha; } public Backend getBackend() { return backend; }
    public static class Toggle { private boolean enabled = true; public boolean isEnabled() { return enabled; } public void setEnabled(boolean enabled) { this.enabled = enabled; } }
    public static class Backend { private boolean forwardAuthToken = false; private boolean requireModuleQueryPermission = true; private boolean exposeDbCredentials = false; private boolean npmAllowScripts = false;
      public boolean isForwardAuthToken() { return forwardAuthToken; } public void setForwardAuthToken(boolean forwardAuthToken) { this.forwardAuthToken = forwardAuthToken; }
      public boolean isRequireModuleQueryPermission() { return requireModuleQueryPermission; } public void setRequireModuleQueryPermission(boolean requireModuleQueryPermission) { this.requireModuleQueryPermission = requireModuleQueryPermission; }
      public boolean isExposeDbCredentials() { return exposeDbCredentials; } public void setExposeDbCredentials(boolean exposeDbCredentials) { this.exposeDbCredentials = exposeDbCredentials; }
      public boolean isNpmAllowScripts() { return npmAllowScripts; } public void setNpmAllowScripts(boolean npmAllowScripts) { this.npmAllowScripts = npmAllowScripts; }
    }
  }
  public static class Redis { private boolean enabled = true; private String host = "127.0.0.1"; private int port = 6379; private String password = ""; private int timeout = 2000; private int database = 0;
    public boolean isEnabled() { return enabled; } public void setEnabled(boolean enabled) { this.enabled = enabled; } public String getHost() { return host; } public void setHost(String host) { this.host = host; } public int getPort() { return port; } public void setPort(int port) { this.port = port; } public String getPassword() { return password; } public void setPassword(String password) { this.password = password; } public int getTimeout() { return timeout; } public void setTimeout(int timeout) { this.timeout = timeout; } public int getDatabase() { return database; } public void setDatabase(int database) { this.database = database; } }
  public static class Netty { private boolean enabled = false; private int port = 9092; public boolean isEnabled() { return enabled; } public void setEnabled(boolean enabled) { this.enabled = enabled; } public int getPort() { return port; } public void setPort(int port) { this.port = port; } }
  public static class Web { private boolean exposeUploads = false; private final Cors cors = new Cors(); public boolean isExposeUploads() { return exposeUploads; } public void setExposeUploads(boolean exposeUploads) { this.exposeUploads = exposeUploads; } public Cors getCors() { return cors; } public static class Cors { private String allowedOriginPatterns = ""; public String getAllowedOriginPatterns() { return allowedOriginPatterns; } public void setAllowedOriginPatterns(String allowedOriginPatterns) { this.allowedOriginPatterns = allowedOriginPatterns; } } }
  public static class Menu { private final Maintenance maintenance = new Maintenance(); public Maintenance getMaintenance() { return maintenance; } public static class Maintenance { private boolean enabled = true; public boolean isEnabled() { return enabled; } public void setEnabled(boolean enabled) { this.enabled = enabled; } } }
  public static class User { private final Guid guid = new Guid(); public Guid getGuid() { return guid; } public static class Guid { private boolean backfillOnStartup = false; public boolean isBackfillOnStartup() { return backfillOnStartup; } public void setBackfillOnStartup(boolean backfillOnStartup) { this.backfillOnStartup = backfillOnStartup; } } }
}
