import type { FormRule } from 'tdesign-vue-next';

import type { SensitiveWordCreateForm } from '../types';

export const createSensitiveWordCreateForm = (): SensitiveWordCreateForm => ({
  word: '',
});

export const resetSensitiveWordCreateForm = (form: SensitiveWordCreateForm) => {
  form.word = '';
};

export const sensitiveWordCreateRules: Record<string, FormRule[]> = {
  word: [{ required: true, message: '请输入敏感词', type: 'error' }],
};
