package elexvx.admin.model.req.setting;

public record UiLegalSettingRequest(
  String userAgreement,
  String privacyAgreement
) {}
