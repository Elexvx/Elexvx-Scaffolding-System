package elexvx.admin.model.req.setting;

public record UiLoginSettingRequest(
  String loginBgUrl,
  Boolean allowMultiDeviceLogin
) {}
