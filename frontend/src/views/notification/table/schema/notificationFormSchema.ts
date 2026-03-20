import type { FormRule } from 'tdesign-vue-next';

export const notificationFormRules: Record<string, FormRule[]> = {
  title: [{ required: true, message: '请输入标题', type: 'error' }],
  content: [{ required: true, message: '请输入正文', type: 'error' }],
  type: [{ required: true, message: '请选择类型', type: 'error' }],
  priority: [{ required: true, message: '请选择优先级', type: 'error' }],
};
