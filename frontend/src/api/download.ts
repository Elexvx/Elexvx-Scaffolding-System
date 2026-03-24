import { request } from '@/utils/request';

export interface FileResourceItem {
  id: number;
  content: string;
  fileName: string;
  suffix?: string;
  fileUrl: string;
  createdAt?: string;
  createdByName?: string;
}

export interface FileResourcePayload {
  content: string;
  fileName: string;
  suffix: string;
  fileUrl: string;
}

export function fetchFileResources(params: { page?: number; size?: number }) {
  return request.get<{ list: FileResourceItem[]; total: number }>({
    url: '/system/file/resource',
    params,
  });
}

export function createFileResource(payload: FileResourcePayload) {
  return request.post<FileResourceItem>({
    url: '/system/file/resource',
    data: payload,
  });
}

export function updateFileResource(id: number, payload: FileResourcePayload) {
  return request.put<FileResourceItem>({
    url: `/system/file/resource/${id}`,
    data: payload,
  });
}

export function deleteFileResource(id: number) {
  return request.delete({
    url: `/system/file/resource/${id}`,
  });
}

export interface FileUploadSession {
  uploadId: string;
  fileName: string;
  fileSize: number;
  chunkSize: number;
  totalChunks: number;
  uploadedChunks: number[];
}

export interface FileUploadInitPayload {
  fileName: string;
  fileSize: number;
  chunkSize?: number;
  fingerprint: string;
}

export interface FileUploadCompletePayload {
  uploadId: string;
  page?: string;
  folder?: string;
}

export function initFileUploadSession(payload: FileUploadInitPayload) {
  return request.post<FileUploadSession>({
    url: '/system/file/upload/init',
    data: payload,
  });
}

export function fetchFileUploadStatus(uploadId: string) {
  return request.get<FileUploadSession>({
    url: '/system/file/upload/status',
    params: { uploadId },
  });
}

export function uploadFileChunk(uploadId: string, chunkIndex: number, chunk: Blob) {
  const formData = new FormData();
  formData.append('uploadId', uploadId);
  formData.append('chunkIndex', String(chunkIndex));
  formData.append('chunk', chunk);
  return request.post({
    url: '/system/file/upload/chunk',
    data: formData,
  });
}

export function completeFileUpload(payload: FileUploadCompletePayload) {
  return request.post<{ url: string }>({
    url: '/system/file/upload/complete',
    data: payload,
  });
}

export function cancelFileUploadSession(uploadId: string) {
  return request.post({
    url: '/system/file/upload/cancel',
    data: { uploadId },
  });
}
