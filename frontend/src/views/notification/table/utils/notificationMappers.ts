import { buildDictOptions, resolveLabel } from '@/utils/dict';

import { fallbackPriorityOptions, fallbackStatusOptions, fallbackTypeOptions, priorityLabelMap, statusLabelMap } from '../constants/notificationOptions';
import type { NotificationFormState, NotificationItem, NotificationPayload } from '../types';

export const formatUploadResponse = (res: any) => {
  let payload = res;
  if (typeof res === 'string') {
    try {
      payload = JSON.parse(res);
    } catch {
      payload = { message: res };
    }
  }
  if (payload?.code === 0) {
    return { ...payload, url: payload?.data?.url };
  }
  return { ...payload, error: payload?.message || '上传失败' };
};

export const buildNotificationOptions = (items: any[], type: 'priority' | 'status' | 'type') => {
  if (type === 'priority') return buildDictOptions(items, fallbackPriorityOptions);
  if (type === 'status') return buildDictOptions(items, fallbackStatusOptions);
  return buildDictOptions(items, fallbackTypeOptions);
};

export const getPriorityTheme = (value?: string) => {
  switch ((value || '').toLowerCase()) {
    case 'high':
      return 'danger';
    case 'middle':
      return 'warning';
    default:
      return 'primary';
  }
};

export const getStatusTheme = (value?: string) => {
  switch (value) {
    case 'published':
      return 'success';
    case 'withdrawn':
      return 'warning';
    default:
      return 'default';
  }
};

export const getPriorityLabel = (value: string | undefined, dictItems: any[]) => resolveLabel(value, dictItems, priorityLabelMap);
export const getStatusLabel = (value: string | undefined, dictItems: any[]) => resolveLabel(value, dictItems, statusLabelMap);

export const createNotificationForm = (): NotificationFormState => ({
  title: '',
  summary: '',
  content: '',
  type: 'notification',
  priority: 'middle',
  status: 'draft',
  coverUrl: '',
  attachmentUrl: '',
  attachmentName: '',
});

export const fillNotificationForm = (form: NotificationFormState, row?: NotificationItem) => {
  Object.assign(
    form,
    row
      ? {
          id: row.id,
          title: row.title,
          summary: row.summary || '',
          content: row.content,
          type: row.type || 'notification',
          priority: row.priority,
          status: row.status,
          coverUrl: row.coverUrl || '',
          attachmentUrl: row.attachmentUrl || '',
          attachmentName: row.attachmentName || '',
        }
      : { id: undefined, ...createNotificationForm() },
  );
};

export const buildNotificationPayload = (form: NotificationFormState): NotificationPayload => ({
  title: String(form.title || '').trim(),
  summary: form.summary ? String(form.summary).trim() : undefined,
  content: form.content,
  type: form.type,
  priority: form.priority,
  status: form.status,
  coverUrl: form.coverUrl ? String(form.coverUrl).trim() : undefined,
  attachmentUrl: form.attachmentUrl ? String(form.attachmentUrl).trim() : undefined,
  attachmentName: form.attachmentName ? String(form.attachmentName).trim() : undefined,
});
