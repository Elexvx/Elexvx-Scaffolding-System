import type { NotificationItem, NotificationPayload } from '@/api/notification';

export interface NotificationQuery {
  keyword: string;
  priority: string;
  status: string;
  page: number;
  size: number;
}

export interface NotificationFormState extends NotificationPayload {}

export interface NotificationOptionItem {
  label: string;
  value: string;
}

export type NotificationMode = 'create' | 'edit';

export interface NotificationUploadContext {
  response?: Record<string, any>;
  file?: { name?: string };
  error?: { message?: string };
}

export type { NotificationItem, NotificationPayload };
