import { allowedAttachmentExtensions, maxAttachmentSize } from '../constants/announcementOptions';

export const isAttachmentFileValid = (file: any) => {
  const rawFile: File | undefined = file?.raw || file;
  const name = String(rawFile?.name || file?.name || '');
  const size = Number(rawFile?.size || file?.size || 0);
  const ext = name.includes('.') ? name.split('.').pop()?.toLowerCase() : '';
  if (!ext || !allowedAttachmentExtensions.has(ext)) {
    return { valid: false, message: '附件格式不支持，请上传常见文档/图片/PDF/PPT/表格文件' };
  }
  if (size > maxAttachmentSize) {
    return { valid: false, message: '附件过大，请上传 20MB 以内文件' };
  }
  return { valid: true, message: '' };
};

export const parseApiError = (error: unknown, fallback: string) => {
  const raw = String((error as any)?.message || fallback);
  return raw.replace(/\s*\[\d{3}\]\s*$/, '').trim() || fallback;
};
