package elexvx.admin.entity;

public class SecurityPasswordPolicy {
  private Long id;
  private Integer passwordMinLength;
  private Boolean passwordRequireUppercase;
  private Boolean passwordRequireLowercase;
  private Boolean passwordRequireSpecial;
  private Boolean passwordAllowSequential;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Integer getPasswordMinLength() { return passwordMinLength; }
  public void setPasswordMinLength(Integer passwordMinLength) { this.passwordMinLength = passwordMinLength; }
  public Boolean getPasswordRequireUppercase() { return passwordRequireUppercase; }
  public void setPasswordRequireUppercase(Boolean passwordRequireUppercase) { this.passwordRequireUppercase = passwordRequireUppercase; }
  public Boolean getPasswordRequireLowercase() { return passwordRequireLowercase; }
  public void setPasswordRequireLowercase(Boolean passwordRequireLowercase) { this.passwordRequireLowercase = passwordRequireLowercase; }
  public Boolean getPasswordRequireSpecial() { return passwordRequireSpecial; }
  public void setPasswordRequireSpecial(Boolean passwordRequireSpecial) { this.passwordRequireSpecial = passwordRequireSpecial; }
  public Boolean getPasswordAllowSequential() { return passwordAllowSequential; }
  public void setPasswordAllowSequential(Boolean passwordAllowSequential) { this.passwordAllowSequential = passwordAllowSequential; }
}
