import type { FormInstanceFunctions, UploadFile } from 'tdesign-vue-next';
import { computed, ref } from 'vue';

import type { AnnouncementMode, AnnouncementItem } from '../types';

export const useAnnouncementPageState = () => {
  const list = ref<AnnouncementItem[]>([]);
  const total = ref(0);
  const loading = ref(false);
  const saving = ref(false);
  const formVisible = ref(false);
  const formMode = ref<AnnouncementMode>('create');
  const formRef = ref<FormInstanceFunctions>();
  const files = ref<UploadFile[]>([]);
  const attachmentFiles = ref<UploadFile[]>([]);
  const drawerTitle = computed(() => (formMode.value === 'create' ? '新增公告' : '编辑公告'));
  return { list, total, loading, saving, formVisible, formMode, formRef, files, attachmentFiles, drawerTitle };
};
