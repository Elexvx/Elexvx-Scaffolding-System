import { reactive } from 'vue';

import { request } from '@/utils/request';

import { assignVerificationSettings, createEmailPayload, createSmsPayload } from '../utils/verificationMappers';
import type { EmailVerificationForm, SmsVerificationForm } from '../types';

export function useVerificationPanelForm() {
  const smsForm = reactive<SmsVerificationForm>({
    smsEnabled: false,
    smsProvider: 'aliyun',
    smsAliyunEnabled: false,
    smsAliyunAccessKeyId: '',
    smsAliyunAccessKeySecret: '',
    smsAliyunSignName: '',
    smsAliyunTemplateCode: '',
    smsAliyunRegionId: 'cn-hangzhou',
    smsAliyunEndpoint: '',
    smsTencentEnabled: false,
    smsTencentSecretId: '',
    smsTencentSecretKey: '',
    smsTencentSignName: '',
    smsTencentTemplateId: '',
    smsTencentRegion: 'ap-guangzhou',
    smsTencentEndpoint: '',
    smsSdkAppId: '',
  });
  const emailForm = reactive<EmailVerificationForm>({
    emailEnabled: false,
    emailHost: '',
    emailPort: 465,
    emailUsername: '',
    emailPassword: '',
    emailFrom: '',
    emailSsl: true,
    emailTemplate: '',
  });

  const load = async () => {
    const settings = await request.get<any>({ url: '/system/ui' });
    assignVerificationSettings(smsForm, emailForm, settings);
  };

  return {
    emailForm,
    load,
    smsForm,
    buildEmailPayload: () => createEmailPayload(emailForm),
    buildSmsPayload: () => createSmsPayload(smsForm),
  };
}
