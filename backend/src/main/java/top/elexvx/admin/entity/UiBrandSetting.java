package top.elexvx.admin.entity;

public class UiBrandSetting {
  private Long id;
  private String websiteName;
  private String appVersion;
  private String logoExpandedUrl;
  private String logoCollapsedUrl;
  private String faviconUrl;
  private String qrCodeUrl;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getWebsiteName() { return websiteName; }
  public void setWebsiteName(String websiteName) { this.websiteName = websiteName; }
  public String getAppVersion() { return appVersion; }
  public void setAppVersion(String appVersion) { this.appVersion = appVersion; }
  public String getLogoExpandedUrl() { return logoExpandedUrl; }
  public void setLogoExpandedUrl(String logoExpandedUrl) { this.logoExpandedUrl = logoExpandedUrl; }
  public String getLogoCollapsedUrl() { return logoCollapsedUrl; }
  public void setLogoCollapsedUrl(String logoCollapsedUrl) { this.logoCollapsedUrl = logoCollapsedUrl; }
  public String getFaviconUrl() { return faviconUrl; }
  public void setFaviconUrl(String faviconUrl) { this.faviconUrl = faviconUrl; }
  public String getQrCodeUrl() { return qrCodeUrl; }
  public void setQrCodeUrl(String qrCodeUrl) { this.qrCodeUrl = qrCodeUrl; }
}
