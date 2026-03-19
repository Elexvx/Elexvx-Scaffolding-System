package top.elexvx.admin.entity;


public class StorageSetting {
  public enum Provider {
    LOCAL,
    ALIYUN,
    TENCENT
  }

  private Long id;

  private String provider = Provider.LOCAL.name();

  private String bucket;

  private String region;

  private String endpoint;

  private String accessKey;

  private String secretKey;

  private String customDomain;

  private String pathPrefix;

  private Boolean publicRead = true;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getProvider() {
    return provider;
  }

  public void setProvider(String provider) {
    this.provider = provider;
  }

  public String getBucket() {
    return bucket;
  }

  public void setBucket(String bucket) {
    this.bucket = bucket;
  }

  public String getRegion() {
    return region;
  }

  public void setRegion(String region) {
    this.region = region;
  }

  public String getEndpoint() {
    return endpoint;
  }

  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }

  public String getAccessKey() {
    return accessKey;
  }

  public void setAccessKey(String accessKey) {
    this.accessKey = accessKey;
  }

  public String getSecretKey() {
    return secretKey;
  }

  public void setSecretKey(String secretKey) {
    this.secretKey = secretKey;
  }

  public String getCustomDomain() {
    return customDomain;
  }

  public void setCustomDomain(String customDomain) {
    this.customDomain = customDomain;
  }

  public String getPathPrefix() {
    return pathPrefix;
  }

  public void setPathPrefix(String pathPrefix) {
    this.pathPrefix = pathPrefix;
  }

  public Boolean getPublicRead() {
    return publicRead;
  }

  public void setPublicRead(Boolean publicRead) {
    this.publicRead = publicRead;
  }

  public Provider getProviderEnum() {
    try {
      return Provider.valueOf(String.valueOf(provider == null ? "" : provider).toUpperCase());
    } catch (Exception e) {
      return Provider.LOCAL;
    }
  }
}
