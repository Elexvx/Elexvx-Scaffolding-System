import { buildDictOptions, resolveLabel } from '@/utils/dict';

import { fallbackPriorityOptions, fallbackStatusOptions, fallbackTypeOptions, priorityLabelMap, statusLabelMap } from '../constants/announcementOptions';
import type { AnnouncementFormState, AnnouncementItem, AnnouncementPayload } from '../types';

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

export const buildAnnouncementOptions = (items: any[], type: 'priority' | 'status' | 'type') => {
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

export const createAnnouncementForm = (): AnnouncementFormState => ({
  title: '',
  summary: '',
  content: '',
  type: 'announcement',
  priority: 'middle',
  status: 'draft',
  coverUrl: '',
  attachmentUrl: '',
  attachmentName: '',
});

export const fillAnnouncementForm = (form: AnnouncementFormState, row?: AnnouncementItem) => {
  Object.assign(
    form,
    row
      ? {
          id: row.id,
          title: row.title,
          summary: row.summary || '',
          content: row.content,
          type: row.type || 'announcement',
          priority: row.priority,
          status: row.status,
          coverUrl: row.coverUrl || '',
          attachmentUrl: row.attachmentUrl || '',
          attachmentName: row.attachmentName || '',
        }
      : { id: undefined, ...createAnnouncementForm() },
  );
};

export const buildAnnouncementPayload = (form: AnnouncementFormState): AnnouncementPayload => ({
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
