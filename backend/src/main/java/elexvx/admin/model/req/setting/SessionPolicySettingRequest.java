package elexvx.admin.model.req.setting;

public record SessionPolicySettingRequest(
  Integer sessionTimeoutMinutes,
  Integer tokenTimeoutMinutes,
  Integer tokenRefreshGraceMinutes,
  Boolean allowUrlTokenParam
) {}
