package top.elexvx.admin.entity;

public class VerificationSmsSetting {
  private Long id;
  private Boolean smsEnabled;
  private String smsProvider;
  private Boolean smsAliyunEnabled;
  private String smsAliyunAccessKeyId;
  private String smsAliyunAccessKeySecret;
  private String smsAliyunSignName;
  private String smsAliyunTemplateCode;
  private String smsAliyunRegionId;
  private String smsAliyunEndpoint;
  private Boolean smsTencentEnabled;
  private String smsTencentSecretId;
  private String smsTencentSecretKey;
  private String smsTencentSignName;
  private String smsTencentTemplateId;
  private String smsTencentRegion;
  private String smsTencentEndpoint;
  private String smsSdkAppId;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Boolean getSmsEnabled() { return smsEnabled; }
  public void setSmsEnabled(Boolean smsEnabled) { this.smsEnabled = smsEnabled; }
  public String getSmsProvider() { return smsProvider; }
  public void setSmsProvider(String smsProvider) { this.smsProvider = smsProvider; }
  public Boolean getSmsAliyunEnabled() { return smsAliyunEnabled; }
  public void setSmsAliyunEnabled(Boolean smsAliyunEnabled) { this.smsAliyunEnabled = smsAliyunEnabled; }
  public String getSmsAliyunAccessKeyId() { return smsAliyunAccessKeyId; }
  public void setSmsAliyunAccessKeyId(String smsAliyunAccessKeyId) { this.smsAliyunAccessKeyId = smsAliyunAccessKeyId; }
  public String getSmsAliyunAccessKeySecret() { return smsAliyunAccessKeySecret; }
  public void setSmsAliyunAccessKeySecret(String smsAliyunAccessKeySecret) { this.smsAliyunAccessKeySecret = smsAliyunAccessKeySecret; }
  public String getSmsAliyunSignName() { return smsAliyunSignName; }
  public void setSmsAliyunSignName(String smsAliyunSignName) { this.smsAliyunSignName = smsAliyunSignName; }
  public String getSmsAliyunTemplateCode() { return smsAliyunTemplateCode; }
  public void setSmsAliyunTemplateCode(String smsAliyunTemplateCode) { this.smsAliyunTemplateCode = smsAliyunTemplateCode; }
  public String getSmsAliyunRegionId() { return smsAliyunRegionId; }
  public void setSmsAliyunRegionId(String smsAliyunRegionId) { this.smsAliyunRegionId = smsAliyunRegionId; }
  public String getSmsAliyunEndpoint() { return smsAliyunEndpoint; }
  public void setSmsAliyunEndpoint(String smsAliyunEndpoint) { this.smsAliyunEndpoint = smsAliyunEndpoint; }
  public Boolean getSmsTencentEnabled() { return smsTencentEnabled; }
  public void setSmsTencentEnabled(Boolean smsTencentEnabled) { this.smsTencentEnabled = smsTencentEnabled; }
  public String getSmsTencentSecretId() { return smsTencentSecretId; }
  public void setSmsTencentSecretId(String smsTencentSecretId) { this.smsTencentSecretId = smsTencentSecretId; }
  public String getSmsTencentSecretKey() { return smsTencentSecretKey; }
  public void setSmsTencentSecretKey(String smsTencentSecretKey) { this.smsTencentSecretKey = smsTencentSecretKey; }
  public String getSmsTencentSignName() { return smsTencentSignName; }
  public void setSmsTencentSignName(String smsTencentSignName) { this.smsTencentSignName = smsTencentSignName; }
  public String getSmsTencentTemplateId() { return smsTencentTemplateId; }
  public void setSmsTencentTemplateId(String smsTencentTemplateId) { this.smsTencentTemplateId = smsTencentTemplateId; }
  public String getSmsTencentRegion() { return smsTencentRegion; }
  public void setSmsTencentRegion(String smsTencentRegion) { this.smsTencentRegion = smsTencentRegion; }
  public String getSmsTencentEndpoint() { return smsTencentEndpoint; }
  public void setSmsTencentEndpoint(String smsTencentEndpoint) { this.smsTencentEndpoint = smsTencentEndpoint; }
  public String getSmsSdkAppId() { return smsSdkAppId; }
  public void setSmsSdkAppId(String smsSdkAppId) { this.smsSdkAppId = smsSdkAppId; }
}
