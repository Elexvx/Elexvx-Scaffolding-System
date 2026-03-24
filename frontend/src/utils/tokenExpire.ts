import { MessagePlugin } from 'tdesign-vue-next';

import router from '@/router';
import { useNotificationStore, useUserStore } from '@/store';
import { clearTokenStorage } from '@/utils/secureToken';

/**
 * Token 过期检测和处理工具
 */

/**
 * 使用方式：
 * - 登录成功后由 store/modules/user.ts 根据 expiresIn 调用 setTokenExpireTimer
 * - 路由守卫（src/permission.ts）在启动时会恢复 token 并重建定时器
 *
 * 处理策略：
 * - 提前 5 分钟 warning 提示
 * - 到期后清理登录态并 hard redirect 到登录页，保证多 tab/缓存场景下状态一致
 */

/** Token 过期通知标记 */
let tokenExpireNotified = false;
let tokenExpireWarnTimer: number | null = null;
let tokenExpireHardTimer: number | null = null;
const authPages = new Set(['/login', '/register', '/forgot']);
const isAuthPage = (path?: string) => Boolean(path && authPages.has(path));
const UNAUTHORIZED_NOTICE_KEY = 'elexvx.auth.invalid.notice';
const LEGACY_UNAUTHORIZED_NOTICE_KEY = 'tdesign.auth.invalid.notice';
const TOKEN_EXPIRED_NOTICE = '当前登录状态失效，请重新登录';

const saveUnauthorizedNotice = (message: string) => {
  try {
    migrateSessionStorageKey(UNAUTHORIZED_NOTICE_KEY, [LEGACY_UNAUTHORIZED_NOTICE_KEY]);
    sessionStorage.setItem(UNAUTHORIZED_NOTICE_KEY, message);
  } catch {}
};

const buildHardLoginHref = (redirect?: string) => {
  const query: Record<string, string> = { _t: String(Date.now()) };
  if (redirect) query.redirect = redirect;
  return router.resolve({ path: '/login', query }).href;
};

/**
 * 设置 token 过期时间
 * @param token token 字符串
 * @param expiresIn 过期时间（秒）
 */
export function setTokenExpireTimer(token: string, expiresIn: number) {
  // 清除之前的定时器
  clearTokenExpireTimer();
  tokenExpireNotified = false;

  if (!token || !expiresIn || expiresIn <= 0) {
    return;
  }

  const notifyTime = Math.max((expiresIn - 300) * 1000, 1000);
  tokenExpireWarnTimer = window.setTimeout(() => {
    notifyTokenExpire();
  }, notifyTime);
  tokenExpireHardTimer = window.setTimeout(
    () => {
      handleTokenExpired();
    },
    Math.max(expiresIn * 1000, 1000),
  );
}

/**
 * 通知 token 即将过期
 */
function notifyTokenExpire() {
  const currentPath = router.currentRoute.value?.path;

  // 避免重复提示和在登录页提示
  if (tokenExpireNotified || isAuthPage(currentPath)) {
    return;
  }

  tokenExpireNotified = true;

  // 显示提示信息
  MessagePlugin.warning({
    content: '您的登录状态即将过期，请及时刷新或重新登录',
    duration: 3000,
  });
}

/**
 * 处理 token 过期
 */
export function handleTokenExpired() {
  const userStore = useUserStore();
  const currentRoute = router.currentRoute.value;
  const currentPath = currentRoute?.path;

  // 避免重复处理和在登录页处理
  if (isAuthPage(currentPath)) {
    return;
  }

  clearTokenExpireTimer();

  // 清除 token 和用户信息
  userStore.token = '';
  userStore.refreshToken = '';
  userStore.tokenExpiresAt = null;
  userStore.userInfoLoaded = false;
  clearTokenStorage();
  useNotificationStore().stopSocket();
  saveUnauthorizedNotice(TOKEN_EXPIRED_NOTICE);
  const hardHref = buildHardLoginHref(currentRoute?.fullPath || currentPath);
  window.location.replace(hardHref);
}

/**
 * 清除 token 过期定时器
 */
export function clearTokenExpireTimer() {
  if (tokenExpireWarnTimer) {
    clearTimeout(tokenExpireWarnTimer);
    tokenExpireWarnTimer = null;
  }
  if (tokenExpireHardTimer) {
    clearTimeout(tokenExpireHardTimer);
    tokenExpireHardTimer = null;
  }
}

/**
 * 检查本地存储的 token 是否过期
 * @returns true 表示 token 已过期，false 表示有效
 */
export function isLocalTokenExpired(): boolean {
  const userStore = useUserStore();
  const token = userStore.token;

  if (!token) {
    return false;
  }

  // 尝试从 token 解析过期时间（JWT 格式）
  const expiresAt = extractTokenExpireTime(token);
  const storedExpiresAt = userStore.tokenExpiresAt;

  if (expiresAt || storedExpiresAt) {
    const now = Date.now();
    return now > (expiresAt || storedExpiresAt || 0);
  }

  // 如果无法解析 token 过期时间，则认为有效
  return false;
}

/**
 * 从 JWT token 中提取过期时间
 * @param token JWT token
 * @returns 过期时间戳（毫秒）或 null
 */
function extractTokenExpireTime(token: string): number | null {
  try {
    // JWT 格式: header.payload.signature
    const parts = token.split('.');
    if (parts.length !== 3) {
      return null;
    }

    // 解码 payload（第二部分）
    const payload = parts[1];
    // 添加补齐的 padding
    const padded = payload + '='.repeat((4 - (payload.length % 4)) % 4);
    const decoded = atob(padded);
    const data = JSON.parse(decoded);

    // JWT 中的 exp 字段是秒级时间戳，需要转换为毫秒
    if (data.exp) {
      return data.exp * 1000;
    }

    return null;
  } catch (error) {
    console.error('Failed to extract token expire time:', error);
    return null;
  }
}

/**
 * 统一解析 token 的过期时间（优先从 JWT 解析）
 */
export function resolveTokenExpiresAt(token?: string, storedExpiresAt?: number | null): number | null {
  if (token) {
    const expiresAt = extractTokenExpireTime(token);
    if (expiresAt != null) {
      return expiresAt;
    }
  }
  return storedExpiresAt ?? null;
}

/**
 * 检查 token 是否即将过期（5 分钟内）
 * @returns true 表示即将过期
 */
export function isTokenExpiringSoon(): boolean {
  const userStore = useUserStore();
  const token = userStore.token;

  if (!token) {
    return false;
  }

  const expiresAt = extractTokenExpireTime(token) || userStore.tokenExpiresAt;

  if (expiresAt) {
    const now = Date.now();
    const timeRemaining = expiresAt - now;
    // 5 分钟 = 300000 毫秒
    return timeRemaining > 0 && timeRemaining < 300000;
  }

  return false;
}
import { migrateSessionStorageKey } from '@/utils/storage/compat';
