import { computed } from 'vue';

import { useDictionary } from '@/hooks/useDictionary';
import { buildDictOptions } from '@/utils/dict';

import { FALLBACK_SMS_PROVIDER_OPTIONS, SMS_PROVIDER_VALUES } from '../constants/verificationOptions';
import type { SmsVerificationForm, EmailVerificationForm } from '../types';

export function useVerificationSections(smsForm: SmsVerificationForm, emailForm: EmailVerificationForm) {
  const smsProviderDict = useDictionary('sms_provider');
  const smsProviderOptions = computed(() =>
    buildDictOptions(smsProviderDict.items.value, FALLBACK_SMS_PROVIDER_OPTIONS, SMS_PROVIDER_VALUES),
  );
  const smsReadonly = computed(() => !smsForm.smsEnabled);
  const emailReadonly = computed(() => !emailForm.emailEnabled);
  const showAliyun = computed(() => smsForm.smsProvider === 'aliyun');
  const showTencent = computed(() => smsForm.smsProvider === 'tencent');
  return { emailReadonly, showAliyun, showTencent, smsProviderDict, smsProviderOptions, smsReadonly };
}
