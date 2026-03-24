package elexvx.admin.model.req.setting;

public record WatermarkSettingRequest(
  Boolean enabled,
  String type,
  String content,
  String imageUrl,
  Double opacity,
  Integer size,
  Integer gapX,
  Integer gapY,
  Integer rotate
) {}
