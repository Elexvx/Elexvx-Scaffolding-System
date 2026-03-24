import type { FormInstanceFunctions, UploadFile } from 'tdesign-vue-next';
import { computed, ref } from 'vue';

import type { FileResourceItem } from '@/api/download';

import type { DownloadPreviewContext } from '../types';

export const useDownloadPageState = () => {
  const list = ref<FileResourceItem[]>([]);
  const total = ref(0);
  const loading = ref(false);
  const saving = ref(false);
  const formVisible = ref(false);
  const formMode = ref<'create' | 'edit'>('create');
  const currentId = ref<number | null>(null);

  const formRef = ref<FormInstanceFunctions>();
  const files = ref<UploadFile[]>([]);
  const pendingFile = ref<File | null>(null);
  const uploadProgress = ref(0);
  const uploadingFile = ref(false);
  const uploadError = ref('');
  const uploadSessionId = ref<string | null>(null);

  const previewVisible = ref(false);
  const previewContext = ref<DownloadPreviewContext | null>(null);
  const previewBodyOverflowBackup = ref<string | null>(null);
  const previewMainOverflowBackup = ref<string | null>(null);

  const pagination = (query: { page: number; size: number }) =>
    computed(() => ({
      current: query.page + 1,
      pageSize: query.size,
      total: total.value,
    }));

  return {
    list,
    total,
    loading,
    saving,
    formVisible,
    formMode,
    currentId,
    formRef,
    files,
    pendingFile,
    uploadProgress,
    uploadingFile,
    uploadError,
    uploadSessionId,
    previewVisible,
    previewContext,
    previewBodyOverflowBackup,
    previewMainOverflowBackup,
    pagination,
  };
};
