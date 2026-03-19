import type { FormRule } from 'tdesign-vue-next';
import { computed, type Ref } from 'vue';

import type { DownloadFormState } from '../types';

export const createDownloadForm = (): DownloadFormState => ({
  content: '',
  fileName: '',
  suffix: '',
  fileUrl: '',
});

export const useDownloadFormRules = (pendingFile: Ref<File | null>) =>
  computed<Record<string, FormRule[]>>(() => ({
    content: [{ required: true, message: '请输入内容', trigger: 'blur' }],
    fileUrl: [
      {
        validator: (val: string) => !!val || !!pendingFile.value,
        message: '请选择文件',
        trigger: 'change',
      },
    ],
  }));
