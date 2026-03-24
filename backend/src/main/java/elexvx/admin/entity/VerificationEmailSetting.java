package elexvx.admin.entity;

public class VerificationEmailSetting {
  private Long id;
  private Boolean emailEnabled;
  private String emailHost;
  private Integer emailPort;
  private String emailUsername;
  private String emailPassword;
  private String emailFrom;
  private Boolean emailSsl;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Boolean getEmailEnabled() { return emailEnabled; }
  public void setEmailEnabled(Boolean emailEnabled) { this.emailEnabled = emailEnabled; }
  public String getEmailHost() { return emailHost; }
  public void setEmailHost(String emailHost) { this.emailHost = emailHost; }
  public Integer getEmailPort() { return emailPort; }
  public void setEmailPort(Integer emailPort) { this.emailPort = emailPort; }
  public String getEmailUsername() { return emailUsername; }
  public void setEmailUsername(String emailUsername) { this.emailUsername = emailUsername; }
  public String getEmailPassword() { return emailPassword; }
  public void setEmailPassword(String emailPassword) { this.emailPassword = emailPassword; }
  public String getEmailFrom() { return emailFrom; }
  public void setEmailFrom(String emailFrom) { this.emailFrom = emailFrom; }
  public Boolean getEmailSsl() { return emailSsl; }
  public void setEmailSsl(Boolean emailSsl) { this.emailSsl = emailSsl; }
}
