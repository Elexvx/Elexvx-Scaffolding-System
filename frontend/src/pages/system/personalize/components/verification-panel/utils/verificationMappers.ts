import type { EmailVerificationForm, SmsVerificationForm } from '../types';

export const resolveVerificationTab = (value: unknown) => {
  const tab = typeof value === 'string' ? value : '';
  return tab === 'email' ? 'email' : 'sms';
};

export const assignVerificationSettings = (smsForm: SmsVerificationForm, emailForm: EmailVerificationForm, s: any) => {
  smsForm.smsEnabled = !!s.smsEnabled;
  smsForm.smsProvider = s.smsProvider || 'aliyun';
  smsForm.smsAliyunEnabled = s.smsAliyunEnabled ?? false;
  smsForm.smsAliyunAccessKeyId = s.smsAliyunAccessKeyId || s.smsAccessKeyId || '';
  smsForm.smsAliyunAccessKeySecret = s.smsAliyunAccessKeySecret || s.smsAccessKeySecret || '';
  smsForm.smsAliyunSignName = s.smsAliyunSignName || s.smsSignName || '';
  smsForm.smsAliyunTemplateCode = s.smsAliyunTemplateCode || s.smsTemplateCode || '';
  smsForm.smsAliyunRegionId = s.smsAliyunRegionId || s.smsRegionId || 'cn-hangzhou';
  smsForm.smsAliyunEndpoint = s.smsAliyunEndpoint || s.smsEndpoint || '';
  smsForm.smsTencentEnabled = s.smsTencentEnabled ?? false;
  const tencentFallback = s.smsProvider === 'tencent';
  smsForm.smsTencentSecretId = s.smsTencentSecretId || (tencentFallback ? s.smsAccessKeyId : '') || '';
  smsForm.smsTencentSecretKey = s.smsTencentSecretKey || (tencentFallback ? s.smsAccessKeySecret : '') || '';
  smsForm.smsTencentSignName = s.smsTencentSignName || (tencentFallback ? s.smsSignName : '') || '';
  smsForm.smsTencentTemplateId = s.smsTencentTemplateId || (tencentFallback ? s.smsTemplateCode : '') || '';
  smsForm.smsTencentRegion = s.smsTencentRegion || (tencentFallback ? s.smsRegionId : '') || 'ap-guangzhou';
  smsForm.smsTencentEndpoint = s.smsTencentEndpoint || (tencentFallback ? s.smsEndpoint : '') || '';
  smsForm.smsSdkAppId = s.smsSdkAppId || '';

  emailForm.emailEnabled = !!s.emailEnabled;
  emailForm.emailHost = s.emailHost || '';
  emailForm.emailPort = s.emailPort !== null && s.emailPort !== undefined ? Number(s.emailPort) : 465;
  emailForm.emailUsername = s.emailUsername || '';
  emailForm.emailPassword = s.emailPassword || '';
  emailForm.emailFrom = s.emailFrom || '';
  emailForm.emailSsl = s.emailSsl !== null && s.emailSsl !== undefined ? !!s.emailSsl : true;
  emailForm.emailTemplate = s.emailTemplate || '';
};

export const createSmsPayload = (form: SmsVerificationForm) => ({ ...form });
export const createEmailPayload = (form: EmailVerificationForm) => ({ ...form });
