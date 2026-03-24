import { request } from '@/utils/request';

export interface AnnouncementPayload {
  id?: number;
  title: string;
  summary?: string;
  content: string;
  type: string;
  priority: string;
  coverUrl?: string;
  attachmentUrl?: string;
  attachmentName?: string;
  status?: string;
}

export interface AnnouncementItem {
  id: number;
  title: string;
  summary?: string;
  content: string;
  type: string;
  priority: string;
  status: string;
  coverUrl?: string;
  attachmentUrl?: string;
  attachmentName?: string;
  publishAt?: string;
  createdAt?: string;
  updatedAt?: string;
  createdByName?: string;
}

export interface AnnouncementSummary {
  id: number;
  title: string;
  summary?: string;
  priority: string;
  type: string;
  status: string;
  coverUrl?: string;
  attachmentUrl?: string;
  attachmentName?: string;
  publishAt?: string;
  updatedAt?: string;
}

export function fetchAnnouncements(params: {
  page?: number;
  size?: number;
  keyword?: string;
  status?: string;
  priority?: string;
}) {
  return request.get<{ list: AnnouncementItem[]; total: number }>({
    url: '/announcement',
    params,
  });
}

export function fetchAnnouncementDetail(id: number) {
  return request.get<AnnouncementItem>({ url: `/announcement/${id}` });
}

export function fetchLatestAnnouncements(size = 8) {
  return request.get<AnnouncementSummary[]>({ url: '/announcement/latest', params: { size } });
}

export function createAnnouncement(payload: AnnouncementPayload) {
  return request.post<AnnouncementItem>({ url: '/announcement', data: payload });
}

export function updateAnnouncement(id: number, payload: AnnouncementPayload) {
  return request.put<AnnouncementItem>({ url: `/announcement/${id}`, data: payload });
}

export function publishAnnouncement(id: number, publish = true) {
  const url = `/announcement/${id}/publish?publish=${encodeURIComponent(String(publish))}`;
  return request.post<AnnouncementItem>({ url });
}

export function deleteAnnouncement(id: number) {
  return request.delete({ url: `/announcement/${id}` });
}
