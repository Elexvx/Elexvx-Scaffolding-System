import type { NotificationOptionItem } from '../types';

export const fallbackPriorityOptions: NotificationOptionItem[] = [
  { label: '高', value: 'high' },
  { label: '中', value: 'middle' },
  { label: '低', value: 'low' },
];

export const fallbackStatusOptions: NotificationOptionItem[] = [
  { label: '草稿', value: 'draft' },
  { label: '已发布', value: 'published' },
  { label: '已撤回', value: 'withdrawn' },
];

export const fallbackTypeOptions: NotificationOptionItem[] = [{ label: '通知', value: 'notification' }];

export const priorityLabelMap: Record<string, string> = {
  high: '高',
  middle: '中',
  low: '低',
};

export const statusLabelMap: Record<string, string> = {
  draft: '草稿',
  published: '已发布',
  withdrawn: '已撤回',
};

export const allowedAttachmentExtensions = new Set([
  'pdf',
  'doc',
  'docx',
  'xls',
  'xlsx',
  'csv',
  'ppt',
  'pptx',
  'png',
  'jpg',
  'jpeg',
  'gif',
  'webp',
]);

export const maxAttachmentSize = 20 * 1024 * 1024;
