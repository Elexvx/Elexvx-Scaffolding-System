package top.elexvx.admin.entity;

public class UiFooterSetting {
  private Long id;
  private String footerCompany;
  private String footerIcp;
  private String copyrightStartYear;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getFooterCompany() { return footerCompany; }
  public void setFooterCompany(String footerCompany) { this.footerCompany = footerCompany; }
  public String getFooterIcp() { return footerIcp; }
  public void setFooterIcp(String footerIcp) { this.footerIcp = footerIcp; }
  public String getCopyrightStartYear() { return copyrightStartYear; }
  public void setCopyrightStartYear(String copyrightStartYear) { this.copyrightStartYear = copyrightStartYear; }
}
