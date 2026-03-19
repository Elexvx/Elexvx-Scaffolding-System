package top.elexvx.admin.entity;

public class UiSystemSetting {
  private Long id;
  private Integer logRetentionDays;
  private Boolean aiAssistantEnabled;
  private Boolean maintenanceEnabled;
  private String maintenanceMessage;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Integer getLogRetentionDays() { return logRetentionDays; }
  public void setLogRetentionDays(Integer logRetentionDays) { this.logRetentionDays = logRetentionDays; }
  public Boolean getAiAssistantEnabled() { return aiAssistantEnabled; }
  public void setAiAssistantEnabled(Boolean aiAssistantEnabled) { this.aiAssistantEnabled = aiAssistantEnabled; }
  public Boolean getMaintenanceEnabled() { return maintenanceEnabled; }
  public void setMaintenanceEnabled(Boolean maintenanceEnabled) { this.maintenanceEnabled = maintenanceEnabled; }
  public String getMaintenanceMessage() { return maintenanceMessage; }
  public void setMaintenanceMessage(String maintenanceMessage) { this.maintenanceMessage = maintenanceMessage; }
}
