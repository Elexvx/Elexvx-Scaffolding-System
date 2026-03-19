package elexvx.admin.entity;

public class UiLegalSetting {
  private Long id;
  private String userAgreement;
  private String privacyAgreement;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getUserAgreement() { return userAgreement; }
  public void setUserAgreement(String userAgreement) { this.userAgreement = userAgreement; }
  public String getPrivacyAgreement() { return privacyAgreement; }
  public void setPrivacyAgreement(String privacyAgreement) { this.privacyAgreement = privacyAgreement; }
}
