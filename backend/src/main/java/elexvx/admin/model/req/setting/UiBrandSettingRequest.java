package elexvx.admin.model.req.setting;

public record UiBrandSettingRequest(
  String websiteName,
  String appVersion,
  String logoExpandedUrl,
  String logoCollapsedUrl,
  String faviconUrl,
  String qrCodeUrl
) {}
