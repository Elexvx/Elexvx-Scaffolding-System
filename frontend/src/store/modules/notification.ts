import { defineStore } from 'pinia';

import { fetchMessages as fetchMessageList, markAllMessagesRead, markMessageRead } from '@/api/message';
import type { NotificationSocketPayload } from '@/api/notification';
import { createNotificationSocketUrl } from '@/api/notification';
import { useUserStore } from '@/store';
import type { NotificationItem } from '@/types/interface';
import { migrateLocalStorageKey } from '@/utils/storage/compat';

type MsgDataType = NotificationItem[];

export const useNotificationStore = defineStore('notification', {
  state: () => ({
    msgData: [] as NotificationItem[],
    socket: null as WebSocket | null,
    socketConnected: false,
    reconnectTimer: null as ReturnType<typeof setTimeout> | null,
    heartbeatTimer: null as ReturnType<typeof setInterval> | null,
  }),
  getters: {
    unreadMsg: (state) => state.msgData.filter((item: NotificationItem) => item.status),
    readMsg: (state) => state.msgData.filter((item: NotificationItem) => !item.status),
  },
  actions: {
    async fetchMessages() {
      try {
        const list = await fetchMessageList();
        if (Array.isArray(list)) {
          // 去重处理，防止后端或持久化导致重复
          const uniqueMap = new Map();
          list.forEach((item) => {
            if (!uniqueMap.has(item.id)) {
              uniqueMap.set(item.id, item);
            }
          });
          this.msgData = Array.from(uniqueMap.values());
        } else {
          this.msgData = [];
        }
      } catch {
        this.msgData = [];
      }
    },
    async markRead(id: string, read = true) {
      await markMessageRead(id, read);
      this.msgData = this.msgData.map((item) => (item.id === id ? { ...item, status: !read } : item));
    },
    async markAllRead() {
      await markAllMessagesRead();
      this.msgData = this.msgData.map((item) => ({ ...item, status: false }));
    },
    setMsgData(data: MsgDataType) {
      this.msgData = data;
    },
    startSocket() {
      if (typeof window === 'undefined') return;
      if (this.socket) return;
      const url = createNotificationSocketUrl();
      if (!url) return;
      const socket = new WebSocket(url);
      this.socket = socket;
      socket.onopen = () => {
        this.socketConnected = true;
        this.sendAuth();
        this.startHeartbeat();
      };
      socket.onmessage = (event) => {
        this.handleSocketMessage(event.data);
      };
      socket.onclose = () => {
        this.handleDisconnect();
      };
      socket.onerror = () => {
        this.handleDisconnect();
      };
    },
    stopSocket() {
      if (this.reconnectTimer) {
        clearTimeout(this.reconnectTimer);
        this.reconnectTimer = null;
      }
      if (this.heartbeatTimer) {
        clearInterval(this.heartbeatTimer);
        this.heartbeatTimer = null;
      }
      if (this.socket) {
        this.socket.close();
        this.socket = null;
      }
      this.socketConnected = false;
    },
    sendAuth() {
      const userStore = useUserStore();
      if (!this.socket || this.socket.readyState !== WebSocket.OPEN) return;
      if (!userStore.token) return;
      const payload: NotificationSocketPayload = {
        type: 'auth',
        token: userStore.token,
        timestamp: Date.now(),
      };
      this.socket.send(JSON.stringify(payload));
    },
    startHeartbeat() {
      if (this.heartbeatTimer) {
        clearInterval(this.heartbeatTimer);
      }
      this.heartbeatTimer = setInterval(() => {
        if (!this.socket || this.socket.readyState !== WebSocket.OPEN) return;
        const payload: NotificationSocketPayload = { type: 'ping', timestamp: Date.now() };
        this.socket.send(JSON.stringify(payload));
      }, 30000);
    },
    handleSocketMessage(raw: string) {
      let payload: NotificationSocketPayload | null = null;
      try {
        payload = JSON.parse(raw);
      } catch {
        payload = null;
      }
      if (!payload?.type) return;
      if (payload.type === 'notification') {
        this.fetchMessages();
      }
    },
    handleDisconnect() {
      this.socketConnected = false;
      if (this.heartbeatTimer) {
        clearInterval(this.heartbeatTimer);
        this.heartbeatTimer = null;
      }
      if (this.socket) {
        this.socket.close();
        this.socket = null;
      }
      const userStore = useUserStore();
      if (!userStore.token) return;
      if (this.reconnectTimer) return;
      this.reconnectTimer = setTimeout(() => {
        this.reconnectTimer = null;
        this.startSocket();
      }, 3000);
    },
  },
  persist: {
    key: 'elexvx.notification',
    paths: ['msgData'],
  },
});

if (typeof window !== 'undefined') {
  migrateLocalStorageKey('elexvx.notification', ['notification']);
}
