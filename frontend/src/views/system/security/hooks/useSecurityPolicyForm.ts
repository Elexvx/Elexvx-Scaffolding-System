import { ref } from 'vue';
import { MessagePlugin } from 'tdesign-vue-next';

import { request } from '@/utils/request';

import { createDefaultSecurityForm } from '../constants/securityOptions';
import { mergeSecuritySettings } from '../utils/securityMappers';

export const useSecurityPolicyForm = (saving: any) => {
  const form = ref(createDefaultSecurityForm());
  const loadSettings = async () => {
    const data = await request.get<any>({ url: '/system/ui' });
    if (data) form.value = mergeSecuritySettings(data, form.value);
  };
  const saveSettings = async () => {
    saving.value = true;
    try {
      await request.post({ url: '/system/ui', data: { ...form.value } });
      await loadSettings();
      MessagePlugin.success('安全设置已保存');
    } catch (err: any) {
      MessagePlugin.error(err?.message || '保存失败');
    } finally {
      saving.value = false;
    }
  };
  return { form, loadSettings, saveSettings };
};
