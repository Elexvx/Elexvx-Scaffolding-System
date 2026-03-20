export interface SmsVerificationForm {
  smsEnabled: boolean;
  smsProvider: string;
  smsAliyunEnabled: boolean;
  smsAliyunAccessKeyId: string;
  smsAliyunAccessKeySecret: string;
  smsAliyunSignName: string;
  smsAliyunTemplateCode: string;
  smsAliyunRegionId: string;
  smsAliyunEndpoint: string;
  smsTencentEnabled: boolean;
  smsTencentSecretId: string;
  smsTencentSecretKey: string;
  smsTencentSignName: string;
  smsTencentTemplateId: string;
  smsTencentRegion: string;
  smsTencentEndpoint: string;
  smsSdkAppId: string;
}

export interface EmailVerificationForm {
  emailEnabled: boolean;
  emailHost: string;
  emailPort: number;
  emailUsername: string;
  emailPassword: string;
  emailFrom: string;
  emailSsl: boolean;
  emailTemplate: string;
}
