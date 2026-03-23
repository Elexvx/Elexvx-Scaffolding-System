import { REFRESH_TOKEN_KEY, TOKEN_EXPIRES_AT_KEY, TOKEN_KEY } from '@/constants/storage';

export function getToken() {
  return localStorage.getItem(TOKEN_KEY) || '';
}

export function setToken(token: string) {
  localStorage.setItem(TOKEN_KEY, token);
}

export function removeToken() {
  localStorage.removeItem(TOKEN_KEY);
}

export function getRefreshToken() {
  return localStorage.getItem(REFRESH_TOKEN_KEY) || '';
}

export function setRefreshToken(refreshToken: string) {
  localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
}

export function removeRefreshToken() {
  localStorage.removeItem(REFRESH_TOKEN_KEY);
}

export function setTokenExpiresAt(expiresAt: number) {
  localStorage.setItem(TOKEN_EXPIRES_AT_KEY, String(expiresAt));
}

export function clearAuthStorage() {
  removeToken();
  removeRefreshToken();
  localStorage.removeItem(TOKEN_EXPIRES_AT_KEY);
}
