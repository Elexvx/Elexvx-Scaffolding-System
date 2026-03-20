import type { SubmitContext, SuccessContext } from 'tdesign-vue-next';
import { MessagePlugin } from 'tdesign-vue-next';
import { computed, reactive } from 'vue';

import { createNotification, updateNotification } from '@/api/notification';
import { useDictionary } from '@/hooks/useDictionary';
import { useUserStore } from '@/store';

import { notificationFormRules } from '../schema/notificationFormSchema';
import { isAttachmentFileValid, parseApiError } from '../utils/notificationGuards';
import { buildNotificationOptions, buildNotificationPayload, createNotificationForm, fillNotificationForm, formatUploadResponse } from '../utils/notificationMappers';

export const useNotificationForm = (options: {
  formMode: any;
  formVisible: any;
  formRef: any;
  files: any;
  attachmentFiles: any;
  saving: any;
  reload: () => Promise<void>;
}) => {
  const { formMode, formVisible, formRef, files, attachmentFiles, saving, reload } = options;
  const form = reactive(createNotificationForm());
  const rules = notificationFormRules;
  const userStore = useUserStore();
  const uploadHeaders = computed(() => ({ Authorization: userStore.token }));
  const typeDict = useDictionary('notification_type');
  const priorityDict = useDictionary('notification_priority');
  const statusDict = useDictionary('notification_status');
  const typeOptions = computed(() => buildNotificationOptions(typeDict.items.value, 'type'));
  const priorityOptions = computed(() => buildNotificationOptions(priorityDict.items.value, 'priority'));
  const statusOptions = computed(() => buildNotificationOptions(statusDict.items.value, 'status'));

  const loadDictionaries = async () => Promise.all([typeDict.load(), priorityDict.load(), statusDict.load()]);

  const openCreate = () => {
    formMode.value = 'create';
    fillNotificationForm(form);
    files.value = [];
    attachmentFiles.value = [];
    formVisible.value = true;
  };

  const openEdit = (row: any) => {
    formMode.value = 'edit';
    fillNotificationForm(form, row);
    files.value = row.coverUrl ? [{ url: row.coverUrl, name: '封面图片', status: 'success' }] : [];
    attachmentFiles.value = row.attachmentUrl ? [{ url: row.attachmentUrl, name: row.attachmentName || '附件', status: 'success' }] : [];
    formVisible.value = true;
  };

  const handleUploadSuccess = (context: SuccessContext) => {
    const response = (context?.response || {}) as any;
    if (response?.code !== undefined && response.code !== 0) return MessagePlugin.error(response?.message || '封面图片上传失败');
    const url = response?.url || response?.data?.url;
    if (!url) return MessagePlugin.error('封面图片上传失败：未返回地址');
    form.coverUrl = url;
    files.value = [{ url, name: '封面图片', status: 'success' }];
  };
  const handleRemove = () => {
    form.coverUrl = '';
    files.value = [];
  };
  const beforeAttachmentUpload = (file: any) => {
    const result = isAttachmentFileValid(file);
    if (!result.valid) MessagePlugin.error(result.message);
    return result.valid;
  };
  const handleAttachmentUploadSuccess = (context: SuccessContext) => {
    const { response, file } = context as any;
    if (response?.code !== undefined && response.code !== 0) return MessagePlugin.error(response?.message || '附件上传失败');
    const url = response?.url || response?.data?.url || response?.data?.data?.url;
    if (!url) return MessagePlugin.error('附件上传失败：未返回地址');
    const name = String(file?.name || '附件');
    form.attachmentUrl = url;
    form.attachmentName = name;
    attachmentFiles.value = [{ url, name, status: 'success' }];
  };
  const handleAttachmentRemove = () => {
    form.attachmentUrl = '';
    form.attachmentName = '';
    attachmentFiles.value = [];
  };
  const handleUploadFail = (label: string, context: any) => {
    const msg = context?.response?.message || context?.response?.error || context?.error?.message || '上传失败';
    MessagePlugin.error(`${label}上传失败: ${msg}`);
  };
  const submit = async (ctx: SubmitContext) => {
    if (ctx.validateResult !== true) return;
    saving.value = true;
    try {
      const payload = buildNotificationPayload(form);
      if (formMode.value === 'create') {
        await createNotification(payload);
        MessagePlugin.success('新增成功');
      } else {
        await updateNotification(form.id as number, payload);
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
  const confirmSubmit = () => formRef.value?.submit();

  return {
    form,
    rules,
    uploadHeaders,
    formatUploadResponse,
    typeOptions,
    priorityOptions,
    statusOptions,
    typeDict,
    priorityDict,
    statusDict,
    loadDictionaries,
    openCreate,
    openEdit,
    handleUploadSuccess,
    handleRemove,
    beforeAttachmentUpload,
    handleAttachmentUploadSuccess,
    handleAttachmentRemove,
    handleUploadFail,
    submit,
    confirmSubmit,
  };
};
