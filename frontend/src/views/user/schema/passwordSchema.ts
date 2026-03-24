import type { FormRule } from 'tdesign-vue-next';

import type { PasswordFormModel } from '../types';

export const createPasswordFormModel = (): PasswordFormModel => ({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
});

export const createPasswordRules = (passwordForm: PasswordFormModel): Record<string, FormRule[]> => ({
  oldPassword: [{ required: true, message: '请输入当前密码', type: 'error' }],
  newPassword: [
    { required: true, message: '请输入新密码', type: 'error' },
    { min: 6, max: 20, message: '密码长度应为6-20位', type: 'error' },
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', type: 'error' },
    { validator: (val: string) => val === passwordForm.newPassword, message: '两次输入的密码不一致', type: 'error' },
  ],
});
