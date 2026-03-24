import type { FormInstanceFunctions, UploadFile } from 'tdesign-vue-next';
import { computed, ref } from 'vue';

import type { NotificationMode, NotificationItem } from '../types';

export const useNotificationPageState = () => {
  const list = ref<NotificationItem[]>([]);
  const total = ref(0);
  const loading = ref(false);
  const saving = ref(false);
  const formVisible = ref(false);
  const formMode = ref<NotificationMode>('create');
  const formRef = ref<FormInstanceFunctions>();
  const files = ref<UploadFile[]>([]);
  const attachmentFiles = ref<UploadFile[]>([]);
  const drawerTitle = computed(() => (formMode.value === 'create' ? '新增通知' : '编辑通知'));
  return { list, total, loading, saving, formVisible, formMode, formRef, files, attachmentFiles, drawerTitle };
};
