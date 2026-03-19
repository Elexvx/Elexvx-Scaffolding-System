package top.elexvx.admin.entity;

public class UiThemeSetting {
  private Long id;
  private Boolean autoTheme;
  private String lightStartTime;
  private String darkStartTime;
  private String mode;
  private String brandTheme;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Boolean getAutoTheme() { return autoTheme; }
  public void setAutoTheme(Boolean autoTheme) { this.autoTheme = autoTheme; }
  public String getLightStartTime() { return lightStartTime; }
  public void setLightStartTime(String lightStartTime) { this.lightStartTime = lightStartTime; }
  public String getDarkStartTime() { return darkStartTime; }
  public void setDarkStartTime(String darkStartTime) { this.darkStartTime = darkStartTime; }
  public String getMode() { return mode; }
  public void setMode(String mode) { this.mode = mode; }
  public String getBrandTheme() { return brandTheme; }
  public void setBrandTheme(String brandTheme) { this.brandTheme = brandTheme; }
}
