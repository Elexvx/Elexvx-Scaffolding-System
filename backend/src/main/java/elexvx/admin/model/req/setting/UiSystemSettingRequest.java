package elexvx.admin.model.req.setting;

public record UiSystemSettingRequest(
  Integer logRetentionDays,
  Boolean maintenanceEnabled,
  String maintenanceMessage
) {}
