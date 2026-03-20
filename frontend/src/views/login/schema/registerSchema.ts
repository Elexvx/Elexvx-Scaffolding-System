import { computed } from 'vue';

import type { FormRuleMap, RegisterFormData } from '../types';

export const createRegisterRules = (
  formData: RegisterFormData,
  captchaRequired: () => boolean,
  validatePasswordPolicy: (value: string) => boolean,
  passwordRequirementMessage: () => string,
) =>
  computed<FormRuleMap>(() => ({
    account: [
      { required: true, message: '请输入用户名', type: 'error' as const },
      { max: 64, message: '用户名长度不能超过 64 位', type: 'error' as const },
      {
        validator: (val) => /^[\w@.-]+$/.test(val),
        message: '用户名仅支持字母、数字及_@.-，不支持空格',
        type: 'error' as const,
      },
    ],
    password: [
      { required: true, message: '请输入密码', type: 'error' as const },
      {
        validator: (val) => validatePasswordPolicy(val || ''),
        message: `密码需满足：${passwordRequirementMessage() || '安全规则'}`,
        type: 'error' as const,
      },
    ],
    confirmPassword: [
      { required: true, message: '请确认密码', type: 'error' as const },
      { validator: (val) => val === formData.password, message: '两次密码输入不一致', type: 'error' as const },
    ],
    captcha: captchaRequired() ? [{ required: true, message: '请输入验证码', type: 'error' as const }] : [],
  }));

export const createRegisterPayload = (formData: RegisterFormData, captchaId: string) => ({
  account: formData.account?.trim() || '',
  password: formData.password,
  confirmPassword: formData.confirmPassword,
  captchaId,
  captchaCode: formData.captcha,
});
