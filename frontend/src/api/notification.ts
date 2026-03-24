import { resolveApiHost } from '@/utils/apiHost';
import { request } from '@/utils/request';

export interface NotificationSocketPayload {
  type: string;
  token?: string;
  clientId?: string;
  userId?: number;
  message?: string;
  category?: string;
  priority?: string;
  timestamp?: number;
}

export interface NotificationPayload {
  id?: number;
  title: string;
  summary?: string;
  content: string;
  priority: string;
  status?: string;
  type?: string;
  coverUrl?: string;
  attachmentUrl?: string;
  attachmentName?: string;
}

export interface NotificationItem {
  id: number;
  title: string;
  summary?: string;
  content: string;
  priority: string;
  status: string;
  type?: string;
  coverUrl?: string;
  attachmentUrl?: string;
  attachmentName?: string;
  publishAt?: string;
  createdAt?: string;
  updatedAt?: string;
  createdByName?: string;
}

export interface NotificationSummary {
  id: number;
  title: string;
  summary?: string;
  priority: string;
  type?: string;
  status: string;
  coverUrl?: string;
  attachmentUrl?: string;
  attachmentName?: string;
  publishAt?: string;
  updatedAt?: string;
}

export function fetchNotifications(params: {
  page?: number;
  size?: number;
  keyword?: string;
  status?: string;
  priority?: string;
}) {
  return request.get<{ list: NotificationItem[]; total: number }>({
    url: '/notification',
    params,
  });
}

export function fetchNotificationDetail(id: number) {
  return request.get<NotificationItem>({ url: `/notification/${id}` });
}

export function fetchLatestNotifications(size = 8) {
  return request.get<NotificationSummary[]>({ url: '/notification/latest', params: { size } });
}

export function createNotification(payload: NotificationPayload) {
  return request.post<NotificationItem>({ url: '/notification', data: payload });
}

export function updateNotification(id: number, payload: NotificationPayload) {
  return request.put<NotificationItem>({ url: `/notification/${id}`, data: payload });
}

export function publishNotification(id: number, publish = true) {
  const url = `/notification/${id}/publish?publish=${encodeURIComponent(String(publish))}`;
  return request.post<NotificationItem>({ url });
}

export function deleteNotification(id: number) {
  return request.delete({ url: `/notification/${id}` });
}

export function broadcastNotification(payload: NotificationPayload) {
  return request.post<NotificationItem>({ url: '/notification/broadcast', data: payload });
}

const resolveSocketBase = () => {
  const envUrl = import.meta.env.VITE_NETTY_WS_URL;
  if (envUrl) return envUrl;
  const apiHost = resolveApiHost();
  if (apiHost) {
    try {
      const parsed = new URL(apiHost);
      const protocol = parsed.protocol === 'https:' ? 'wss:' : 'ws:';
      const portOverride = import.meta.env.VITE_NETTY_PORT;
      const port = portOverride || parsed.port;
      const host = parsed.hostname;
      const finalPort = port ? `:${port}` : '';
      return `${protocol}//${host}${finalPort}`;
    } catch {
      return '';
    }
  }
  if (typeof window !== 'undefined') {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const portOverride = import.meta.env.VITE_NETTY_PORT;
    const port = portOverride || window.location.port;
    const host = window.location.hostname;
    const finalPort = port ? `:${port}` : '';
    return `${protocol}//${host}${finalPort}`;
  }
  return '';
};

export const createNotificationSocketUrl = () => {
  const base = resolveSocketBase();
  if (!base) return '';
  return `${base}/ws`;
};
