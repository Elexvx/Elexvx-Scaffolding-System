package elexvx.admin.model.req.setting;

public record UiThemeSettingRequest(
  Boolean autoTheme,
  String lightStartTime,
  String darkStartTime,
  String mode,
  String brandTheme
) {}
