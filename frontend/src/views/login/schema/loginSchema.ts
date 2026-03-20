import { computed } from 'vue';

import { t } from '@/locales';

import type { FormRuleMap, LoginFormData } from '../types';

export const createLoginRules = (captchaRequired: () => boolean) =>
  computed<FormRuleMap>(() => ({
    phone: [{ required: true, message: t('pages.login.required.phone'), type: 'error' as const }],
    email: [
      { required: true, message: t('pages.login.required.email'), type: 'error' as const },
      { email: true, message: t('pages.login.invalid.email'), type: 'error' as const },
    ],
    account: [
      { required: true, message: t('pages.login.required.account'), type: 'error' as const },
      { validator: (val) => /^[\w@.-]+$/.test(val), message: t('pages.login.invalid.account'), type: 'error' as const },
    ],
    password: [{ required: true, message: t('pages.login.required.password'), type: 'error' as const }],
    verifyCode: [{ required: true, message: t('pages.login.required.verification'), type: 'error' as const }],
    agreed: [{ validator: (val) => Boolean(val), message: t('pages.login.agreeTerms'), type: 'error' as const }],
    captcha: captchaRequired()
      ? [{ required: true, message: t('pages.login.required.verification'), type: 'error' as const }]
      : [],
  }));

export const createLoginPayload = (formData: LoginFormData, captchaId: string) => ({
  ...formData,
  captchaId,
  captchaCode: formData.captcha,
});
