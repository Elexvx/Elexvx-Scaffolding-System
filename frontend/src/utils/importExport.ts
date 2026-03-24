import type { AxiosResponse } from 'axios';

import { resolveApiHost } from '@/utils/apiHost';

const normalizeLeadingSlash = (value?: string) => {
  const raw = String(value || '');
  if (!raw) return '';
  return raw.startsWith('/') ? raw : `/${raw}`;
};

const normalizePrefix = (value?: string) => {
  const raw = String(value || '');
  if (!raw) return '';
  const withLeading = normalizeLeadingSlash(raw);
  return withLeading.replace(/\/+$/, '');
};

const normalizeApiHost = (value?: string) => {
  const raw = String(value || '');
  if (!raw) return '';
  return raw.replace(/\/+$/, '');
};

export const resolveApiActionUrl = (path: string) => {
  const apiUrlPrefix = normalizePrefix(import.meta.env.VITE_API_URL_PREFIX);
  const apiHost = normalizeApiHost(resolveApiHost());
  const normalizedPath = normalizeLeadingSlash(path);
  return apiHost ? `${apiHost}${apiUrlPrefix}${normalizedPath}` : `${apiUrlPrefix}${normalizedPath}`;
};

export const resolveFilenameFromDisposition = (disposition: unknown, fallback: string) => {
  const raw = String(disposition || '');
  if (!raw) return fallback;
  const encoded = raw.match(/filename\*\s*=\s*utf-8''([^;]+)/i)?.[1];
  const plain = raw.match(/filename\s*=\s*"?([^";]+)"?/i)?.[1];
  const candidate = encoded || plain;
  if (!candidate) return fallback;
  try {
    return decodeURIComponent(candidate);
  } catch {
    return candidate;
  }
};

export const triggerBlobDownload = (blob: Blob, fileName: string) => {
  if (typeof document === 'undefined') return;
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = fileName;
  link.rel = 'noreferrer';
  link.click();
  URL.revokeObjectURL(url);
};

const normalizeHeaderValue = (value?: unknown) => String(value || '').trim();

const getHeader = (headers: Record<string, unknown> | undefined, key: string) => {
  if (!headers) return '';
  const lower = key.toLowerCase();
  return normalizeHeaderValue(headers[lower] ?? headers[key]);
};

const isLikelyJsonType = (contentType: string) => {
  const ct = contentType.toLowerCase();
  return ct.includes('application/json') || ct.includes('text/json') || ct.includes('text/plain');
};

const parseApiErrorMessage = (text: string) => {
  const raw = String(text || '').trim();
  if (!raw) return '';
  try {
    const data = JSON.parse(raw);
    const code = data?.code;
    const message = data?.message;
    if (code !== undefined && code !== 0) {
      return String(message || `下载失败 [${code}]`);
    }
  } catch {
    if (raw.startsWith('<!DOCTYPE html') || raw.startsWith('<html')) {
      return '下载失败（服务返回了 HTML 页面）';
    }
  }
  return '';
};

export const downloadBlobResponse = async (response: AxiosResponse<any>, fallbackFileName: string) => {
  const blob = response?.data instanceof Blob ? (response.data as Blob) : new Blob([response?.data || '']);
  const headers = (response as any)?.headers || {};
  const disposition = getHeader(headers, 'content-disposition');
  const contentType = getHeader(headers, 'content-type');
  const fileName = resolveFilenameFromDisposition(disposition, fallbackFileName);

  if (contentType && isLikelyJsonType(contentType)) {
    const text = await blob.text();
    const message = parseApiErrorMessage(text);
    if (message) throw new Error(message);
    throw new Error('下载失败');
  }

  if (!contentType) {
    const peek = await blob
      .slice(0, 512)
      .text()
      .catch(() => '');
    const message = parseApiErrorMessage(peek);
    if (message) throw new Error(message);
  }

  triggerBlobDownload(blob, fileName);
};
