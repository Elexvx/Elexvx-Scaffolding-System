package top.elexvx.admin.vo;

import top.elexvx.admin.entity.StorageSetting;

public class StorageSettingResponse {
  private String provider;
  private String bucket;
  private String region;
  private String endpoint;
  private String accessKey;
  private String customDomain;
  private String pathPrefix;
  private Boolean publicRead;
  private boolean secretConfigured;

  public static StorageSettingResponse from(StorageSetting setting) {
    StorageSettingResponse r = new StorageSettingResponse();
    if (setting == null) return r;
    r.setProvider(setting.getProvider());
    r.setBucket(setting.getBucket());
    r.setRegion(setting.getRegion());
    r.setEndpoint(setting.getEndpoint());
    r.setAccessKey(setting.getAccessKey());
    r.setCustomDomain(setting.getCustomDomain());
    r.setPathPrefix(setting.getPathPrefix());
    r.setPublicRead(setting.getPublicRead());
    r.setSecretConfigured(setting.getSecretKey() != null && !setting.getSecretKey().isBlank());
    return r;
  }

  public String getProvider() { return provider; }
  public void setProvider(String provider) { this.provider = provider; }
  public String getBucket() { return bucket; }
  public void setBucket(String bucket) { this.bucket = bucket; }
  public String getRegion() { return region; }
  public void setRegion(String region) { this.region = region; }
  public String getEndpoint() { return endpoint; }
  public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
  public String getAccessKey() { return accessKey; }
  public void setAccessKey(String accessKey) { this.accessKey = accessKey; }
  public String getCustomDomain() { return customDomain; }
  public void setCustomDomain(String customDomain) { this.customDomain = customDomain; }
  public String getPathPrefix() { return pathPrefix; }
  public void setPathPrefix(String pathPrefix) { this.pathPrefix = pathPrefix; }
  public Boolean getPublicRead() { return publicRead; }
  public void setPublicRead(Boolean publicRead) { this.publicRead = publicRead; }
  public boolean isSecretConfigured() { return secretConfigured; }
  public void setSecretConfigured(boolean secretConfigured) { this.secretConfigured = secretConfigured; }
}
