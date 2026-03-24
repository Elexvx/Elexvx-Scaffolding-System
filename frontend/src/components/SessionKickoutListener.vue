<template><span /></template>
<script setup lang="ts">
import { onBeforeUnmount, watch } from 'vue';
import { useRoute } from 'vue-router';

import { useUserStore } from '@/store';
import { buildSseUrl } from '@/utils/sse';
import { handleTokenExpired } from '@/utils/tokenExpire';

const userStore = useUserStore();
const route = useRoute();

const authPages = new Set(['/login', '/register', '/forgot']);
const isAuthPage = (path?: string) => Boolean(path && authPages.has(path));

let source: EventSource | null = null;
let reconnectTimer: number | null = null;
let healthCheckTimer: number | null = null;
let handlingKickout = false;
let lastAliveAt = 0;

const RECONNECT_DELAY_MS = 2000;
const HEALTH_CHECK_INTERVAL_MS = 15000;
const SOURCE_STALE_TIMEOUT_MS = 45000;

const clearReconnect = () => {
  if (reconnectTimer) {
    window.clearTimeout(reconnectTimer);
    reconnectTimer = null;
  }
};

const clearHealthCheck = () => {
  if (healthCheckTimer) {
    window.clearInterval(healthCheckTimer);
    healthCheckTimer = null;
  }
};

const markAlive = () => {
  lastAliveAt = Date.now();
};

const startHealthCheck = () => {
  clearHealthCheck();
  markAlive();
  healthCheckTimer = window.setInterval(() => {
    if (!source || !userStore.token || isAuthPage(route.path)) return;
    if (Date.now() - lastAliveAt < SOURCE_STALE_TIMEOUT_MS) return;
    closeSource();
    if (userStore.token && !isAuthPage(route.path)) {
      openSource(userStore.token);
    }
  }, HEALTH_CHECK_INTERVAL_MS);
};

const closeSource = () => {
  if (source) {
    source.close();
    source = null;
  }
  clearReconnect();
  clearHealthCheck();
};

const kickoutNow = () => {
  if (handlingKickout || isAuthPage(route.path)) return;
  handlingKickout = true;
  closeSource();
  handleTokenExpired();
};

const verifyTokenStillValid = async (token: string) => {
  if (!token) return false;
  try {
    const url = buildSseUrl('/auth/user', { _t: String(Date.now()) });
    const resp = await fetch(url, {
      method: 'GET',
      headers: {
        Authorization: token,
      },
      credentials: 'include',
      cache: 'no-store',
    });
    return resp.status !== 401;
  } catch {
    // Network errors should not force logout immediately.
    return true;
  }
};

const scheduleReconnect = (token: string) => {
  clearReconnect();
  reconnectTimer = window.setTimeout(() => {
    if (userStore.token === token && !isAuthPage(route.path)) {
      openSource(token);
    }
  }, RECONNECT_DELAY_MS);
};

const openSource = (token: string) => {
  if (!token || isAuthPage(route.path) || typeof EventSource === 'undefined') return;
  closeSource();
  const url = buildSseUrl('/auth/concurrent/stream', { token, _t: String(Date.now()) });
  source = new EventSource(url);
  source.onopen = () => {
    markAlive();
  };
  source.onmessage = () => {
    markAlive();
  };
  source.addEventListener('connected', () => {
    markAlive();
  });
  source.addEventListener('ping', () => {
    markAlive();
  });
  source.addEventListener('force-logout', () => {
    markAlive();
    kickoutNow();
  });
  // Backward-compatible event name.
  source.addEventListener('concurrent-login', () => {
    markAlive();
    kickoutNow();
  });
  source.onerror = async () => {
    const currentToken = userStore.token;
    closeSource();
    if (!currentToken || isAuthPage(route.path)) return;
    const stillValid = await verifyTokenStillValid(currentToken);
    if (!stillValid) {
      kickoutNow();
      return;
    }
    scheduleReconnect(currentToken);
  };
  startHealthCheck();
};

watch(
  () => userStore.token,
  (token) => {
    handlingKickout = false;
    if (token && !isAuthPage(route.path)) {
      openSource(token);
    } else {
      closeSource();
    }
  },
  { immediate: true },
);

watch(
  () => route.path,
  (path) => {
    if (isAuthPage(path)) {
      closeSource();
      return;
    }
    if (userStore.token) {
      openSource(userStore.token);
    }
  },
);

onBeforeUnmount(() => {
  closeSource();
});
</script>
