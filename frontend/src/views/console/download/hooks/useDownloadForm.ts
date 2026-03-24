import type { FormInstanceFunctions, SubmitContext, UploadFile } from 'tdesign-vue-next';
import { MessagePlugin } from 'tdesign-vue-next';
import { computed, reactive, type Ref } from 'vue';

import type { FileResourceItem } from '@/api/download';
import { cancelFileUploadSession, completeFileUpload, createFileResource, initFileUploadSession, updateFileResource, uploadFileChunk } from '@/api/download';

import { uploadChunkSize } from '../constants/downloadOptions';
import { createDownloadForm, useDownloadFormRules } from '../schema/downloadFormSchema';
import { parseApiError, resolveSuffix } from '../utils/downloadGuards';
import { buildFileFingerprint, buildDownloadPayload } from '../utils/downloadMappers';

interface UseDownloadFormOptions {
  formRef: Ref<FormInstanceFunctions | undefined>;
  formVisible: Ref<boolean>;
  formMode: Ref<'create' | 'edit'>;
  currentId: Ref<number | null>;
  files: Ref<UploadFile[]>;
  pendingFile: Ref<File | null>;
  uploadProgress: Ref<number>;
  uploadingFile: Ref<boolean>;
  uploadError: Ref<string>;
  uploadSessionId: Ref<string | null>;
  saving: Ref<boolean>;
  reload: () => Promise<void>;
}

export const useDownloadForm = (options: UseDownloadFormOptions) => {
  const { formRef, formVisible, formMode, currentId, files, pendingFile, uploadProgress, uploadingFile, uploadError, uploadSessionId, saving, reload } = options;
  const form = reactive(createDownloadForm());
  const rules = useDownloadFormRules(pendingFile);
  const drawerTitle = computed(() => (formMode.value === 'create' ? '添加下载文件' : '编辑下载文件'));

  const updateUploadProgress = (percent: number) => {
    const safePercent = Math.min(100, Math.max(0, percent));
    uploadProgress.value = safePercent;
    if (files.value.length) {
      files.value = [
        {
          ...files.value[0],
          status: safePercent >= 100 ? 'success' : 'progress',
          percent: safePercent,
        },
      ];
    }
  };

  const resetUploadState = async () => {
    uploadProgress.value = 0;
    updateUploadProgress(0);
    uploadingFile.value = false;
    uploadError.value = '';
    pendingFile.value = null;
    files.value = [];
    if (uploadSessionId.value) {
      try {
        await cancelFileUploadSession(uploadSessionId.value);
      } catch {
        //
      }
      uploadSessionId.value = null;
    }
  };

  const resetForm = () => {
    form.content = '';
    form.fileName = '';
    form.fileUrl = '';
    form.suffix = '';
    void resetUploadState();
  };

  const openCreate = () => {
    formMode.value = 'create';
    currentId.value = null;
    resetForm();
    formVisible.value = true;
  };

  const openEdit = (row: FileResourceItem) => {
    formMode.value = 'edit';
    currentId.value = row.id;
    form.content = row.content;
    form.fileName = row.fileName;
    form.fileUrl = row.fileUrl;
    form.suffix = row.suffix || resolveSuffix(row.fileName);
    files.value = row.fileUrl
      ? [
          {
            url: row.fileUrl,
            name: row.fileName,
            status: 'success',
            percent: 100,
            uid: String(Date.now()),
          },
        ]
      : [];
    pendingFile.value = null;
    uploadProgress.value = 0;
    uploadError.value = '';
    uploadSessionId.value = null;
    formVisible.value = true;
  };

  const ensureFileUploaded = async () => {
    if (!pendingFile.value) {
      if (!form.fileUrl) throw new Error('请先上传文件');
      return;
    }
    uploadingFile.value = true;
    uploadError.value = '';
    try {
      const fingerprint = buildFileFingerprint(pendingFile.value);
      const session = await initFileUploadSession({
        fileName: pendingFile.value.name,
        fileSize: pendingFile.value.size,
        chunkSize: uploadChunkSize,
        fingerprint,
      });
      uploadSessionId.value = session.uploadId;
      const chunkSize = session.chunkSize || uploadChunkSize;
      const total = session.totalChunks || Math.max(1, Math.ceil(pendingFile.value.size / chunkSize));
      const uploadedChunks = new Set(session.uploadedChunks || []);
      updateUploadProgress(Math.round((uploadedChunks.size / total) * 100));
      for (let index = 0; index < total; index += 1) {
        if (uploadedChunks.has(index)) continue;
        const start = index * chunkSize;
        const end = Math.min(pendingFile.value.size, start + chunkSize);
        const chunk = pendingFile.value.slice(start, end);
        await uploadFileChunk(session.uploadId, index, chunk);
        uploadedChunks.add(index);
        updateUploadProgress(Math.round((uploadedChunks.size / total) * 100));
      }
      const result = await completeFileUpload({ uploadId: session.uploadId, page: 'console-download' });
      form.fileUrl = result.url;
      form.fileName = pendingFile.value.name;
      form.suffix = resolveSuffix(pendingFile.value.name);
      updateUploadProgress(100);
      files.value = [
        {
          uid: `${Date.now()}-${pendingFile.value.name}`,
          name: pendingFile.value.name,
          status: 'success',
          percent: 100,
        },
      ];
      pendingFile.value = null;
      uploadSessionId.value = null;
    } catch (error) {
      uploadError.value = parseApiError(error, '上传失败');
      throw error;
    } finally {
      uploadingFile.value = false;
    }
  };

  const submit = async (context: SubmitContext) => {
    if (context.validateResult !== true) return;
    saving.value = true;
    try {
      await ensureFileUploaded();
      const payload = buildDownloadPayload(form);
      if (formMode.value === 'create') {
        await createFileResource(payload);
        MessagePlugin.success('添加成功');
      } else if (currentId.value !== null) {
        await updateFileResource(currentId.value, payload);
        MessagePlugin.success('更新成功');
      }
      formVisible.value = false;
      await reload();
    } catch (error) {
      MessagePlugin.error(parseApiError(error, '保存失败'));
    } finally {
      saving.value = false;
    }
  };

  const confirmSubmit = () => {
    if (saving.value || uploadingFile.value) return;
    formRef.value?.submit();
  };

  const handleBeforeUpload = (file: UploadFile) => {
    const rawFile = ((file as any)?.raw || (file as any)) as any;
    if (!rawFile || typeof rawFile.name !== 'string' || typeof rawFile.size !== 'number') return true;
    pendingFile.value = rawFile as File;
    files.value = [
      {
        uid: `${Date.now()}-${rawFile.name}`,
        name: rawFile.name,
        status: 'waiting',
        percent: 0,
      },
    ];
    uploadError.value = '';
    uploadProgress.value = 0;
    return true;
  };

  const handleSelectChange = (selected: File[]) => {
    if (!selected?.length) return;
    pendingFile.value = selected[0];
    uploadError.value = '';
    uploadProgress.value = 0;
    (formRef.value as any)?.validate?.({ fields: ['fileUrl'] });
  };

  const handleUploadRemove = () => {
    form.fileUrl = '';
    form.fileName = '';
    form.suffix = '';
    void resetUploadState();
    (formRef.value as any)?.validate?.({ fields: ['fileUrl'] });
  };

  return {
    form,
    rules,
    drawerTitle,
    submit,
    openCreate,
    openEdit,
    resetUploadState,
    handleBeforeUpload,
    handleSelectChange,
    handleUploadRemove,
    confirmSubmit,
  };
};
