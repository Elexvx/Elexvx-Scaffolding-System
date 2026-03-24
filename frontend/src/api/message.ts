import { request } from '@/utils/request';

export interface MessageItem {
  id: string;
  content: string;
  type: string;
  status: boolean;
  collected: boolean;
  date: string;
  quality: string;
}

export interface MessageSendPayload {
  toUserId: number;
  content: string;
  type?: string;
  quality?: string;
}

export interface MessageBroadcastPayload {
  content: string;
  type?: string;
  quality?: string;
}

export function fetchMessages() {
  return request.get<MessageItem[]>({ url: '/message/list' });
}

export function sendMessage(payload: MessageSendPayload) {
  return request.post<MessageItem>({
    url: '/message',
    data: {
      ...payload,
      type: payload.type || 'message',
      quality: payload.quality || 'middle',
    },
  });
}

export function broadcastMessage(payload: MessageBroadcastPayload) {
  return request.post<number>({
    url: '/message/broadcast',
    data: {
      ...payload,
      type: payload.type || 'message',
      quality: payload.quality || 'middle',
    },
  });
}

export function markMessageRead(id: string, read = true) {
  return request.post({ url: read ? '/message/read' : '/message/unread', params: { id } }, { joinParamsToUrl: true });
}

export function markAllMessagesRead() {
  return request.post<number>({ url: '/message/read-all' });
}

export function deleteMessage(id: string) {
  return request.delete({ url: `/message/${id}` });
}
