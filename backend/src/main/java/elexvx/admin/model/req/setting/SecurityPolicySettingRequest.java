package elexvx.admin.model.req.setting;

public record SecurityPolicySettingRequest(
  Boolean captchaEnabled,
  String captchaType,
  Integer dragCaptchaWidth,
  Integer dragCaptchaHeight,
  Integer dragCaptchaThreshold,
  Integer imageCaptchaLength,
  Integer imageCaptchaNoiseLines,
  Integer passwordMinLength,
  Boolean passwordRequireUppercase,
  Boolean passwordRequireLowercase,
  Boolean passwordRequireSpecial,
  Boolean passwordAllowSequential
) {}
