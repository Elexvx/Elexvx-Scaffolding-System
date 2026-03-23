import { REFRESH_TOKEN_KEY, TOKEN_EXPIRES_AT_KEY, TOKEN_KEY } from '@/constants/storage';

function safeStorage() {
  if (typeof window === 'undefined') {
    return null;
  }
  try {
    return window.localStorage;
  } catch {
    return null;
  }
}

export function getToken() {
  return safeStorage()?.getItem(TOKEN_KEY) || '';
}

export function setToken(token: string) {
  safeStorage()?.setItem(TOKEN_KEY, token);
}

export function removeToken() {
  safeStorage()?.removeItem(TOKEN_KEY);
}

export function getRefreshToken() {
  return safeStorage()?.getItem(REFRESH_TOKEN_KEY) || '';
}

export function setRefreshToken(refreshToken: string) {
  safeStorage()?.setItem(REFRESH_TOKEN_KEY, refreshToken);
}

export function removeRefreshToken() {
  safeStorage()?.removeItem(REFRESH_TOKEN_KEY);
}

export function getTokenExpiresAt() {
  const raw = safeStorage()?.getItem(TOKEN_EXPIRES_AT_KEY);
  if (!raw) return 0;
  const value = Number(raw);
  return Number.isFinite(value) ? value : 0;
}

export function isTokenExpired() {
  const expiresAt = getTokenExpiresAt();
  return expiresAt > 0 && Date.now() >= expiresAt;
}

export function setTokenExpiresAt(expiresAt: number) {
  safeStorage()?.setItem(TOKEN_EXPIRES_AT_KEY, String(expiresAt));
}

export function clearAuthStorage() {
  removeToken();
  removeRefreshToken();
  safeStorage()?.removeItem(TOKEN_EXPIRES_AT_KEY);
}
