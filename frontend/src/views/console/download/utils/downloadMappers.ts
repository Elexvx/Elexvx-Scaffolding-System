import type { FileResourcePayload } from '@/api/download';

import type { DownloadFormState } from '../types';

export const buildDownloadPayload = (form: DownloadFormState): FileResourcePayload => ({
  content: String(form.content || '').trim(),
  fileName: String(form.fileName || '').trim(),
  suffix: String(form.suffix || '').trim(),
  fileUrl: String(form.fileUrl || '').trim(),
});

export const buildFileFingerprint = (file: File) => {
  const origin = `${file.name}|${file.size}|${file.lastModified}`;
  if (typeof window === 'undefined') return origin;
  try {
    return window.btoa(unescape(encodeURIComponent(origin)));
  } catch {
    return window.btoa(origin);
  }
};
