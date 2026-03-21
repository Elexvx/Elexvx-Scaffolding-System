package elexvx.admin.model.req.setting;

public record VerificationProviderSettingRequest(
  Boolean smsEnabled,
  String smsProvider,
  Boolean smsAliyunEnabled,
  String smsAliyunAccessKeyId,
  String smsAliyunAccessKeySecret,
  String smsAliyunSignName,
  String smsAliyunTemplateCode,
  String smsAliyunRegionId,
  String smsAliyunEndpoint,
  Boolean smsTencentEnabled,
  String smsTencentSecretId,
  String smsTencentSecretKey,
  String smsTencentSignName,
  String smsTencentTemplateId,
  String smsTencentRegion,
  String smsTencentEndpoint,
  String smsSdkAppId,
  Boolean emailEnabled,
  String emailHost,
  Integer emailPort,
  String emailUsername,
  String emailPassword,
  String emailFrom,
  Boolean emailSsl
) {}
