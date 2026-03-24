import type { AnnouncementItem, AnnouncementPayload } from '@/api/announcement';

export interface AnnouncementQuery {
  keyword: string;
  priority: string;
  status: string;
  page: number;
  size: number;
}

export interface AnnouncementFormState extends AnnouncementPayload {}

export interface AnnouncementOptionItem {
  label: string;
  value: string;
}

export type AnnouncementMode = 'create' | 'edit';

export interface AnnouncementUploadContext {
  response?: Record<string, any>;
  file?: { name?: string };
  error?: { message?: string };
}

export type { AnnouncementItem, AnnouncementPayload };
