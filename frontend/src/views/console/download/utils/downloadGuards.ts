import { officeExtensions } from '../constants/downloadOptions';

export const resolveSuffix = (name: string) => {
  if (!name) return '';
  const dot = name.lastIndexOf('.');
  if (dot < 0 || dot === name.length - 1) return '';
  return name.substring(dot + 1).toLowerCase();
};

export const isOfficeFile = (suffix?: string) => Boolean(suffix && officeExtensions.has(suffix));

export const buildAbsoluteUrl = (url: string) => {
  if (!url) return '';
  if (/^https?:\/\//i.test(url)) return url;
  const base = typeof window !== 'undefined' ? window.location.origin : '';
  const normalized = url.startsWith('/') ? url : `/${url}`;
  return `${base}${normalized}`;
};

export const parseApiError = (error: unknown, fallback: string) => {
  const raw = String((error as any)?.message || fallback);
  return raw.replace(/\s*\[\d{3}\]\s*$/, '').trim() || fallback;
};
