import type { FileResourceItem, FileResourcePayload } from '@/api/download';

export interface DownloadFilters {
  page: number;
  size: number;
}

export interface DownloadPreviewContext {
  fileUrl: string;
  fileName: string;
  suffix?: string;
}

export interface DownloadFormState extends FileResourcePayload {}

export type DownloadRow = FileResourceItem;
