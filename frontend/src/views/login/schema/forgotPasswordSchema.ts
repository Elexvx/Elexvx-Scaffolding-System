import type { FormRuleMap, ForgotPasswordFormData } from '../types';

export const createForgotPasswordRules = (formData: ForgotPasswordFormData): FormRuleMap => ({
  account: [{ required: true, message: '账号必填', type: 'error' as const }],
  phone: [
    { required: true, message: '请输入手机号（不支持空格）', type: 'error' as const },
    { pattern: /^1\d{10}$/, message: '手机号格式不正确（不支持空格）', type: 'error' as const },
  ],
  verifyCode: [{ required: true, message: '验证码必填', type: 'error' as const }],
  newPassword: [
    { required: true, message: '请输入新密码', type: 'error' as const },
    { min: 6, message: '至少 6 位字符', type: 'error' as const },
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', type: 'error' as const },
    { validator: (val) => val === formData.newPassword, message: '两次输入不一致', type: 'error' as const },
  ],
});
