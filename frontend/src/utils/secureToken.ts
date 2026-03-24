import { migrateLocalStorageKey, migrateSessionStorageKey } from '@/utils/storage/compat';

/**
 * Token 存储与简单加密封装（浏览器端）。
 *
 * - 使用 WebCrypto AES-GCM 对 token/refreshToken 进行加密后再落盘（localStorage）
 * - 加密密钥以 raw 形式持久化（localStorage/sessionStorage），用于“同浏览器同站点”解密恢复
 * - clearTokenStorage：统一清理登录态相关缓存，避免各处分散删除 key
 *
 * 注意：这不是为了对抗高强度攻击，而是用于降低明文 token 暴露风险（例如误截图/日志/简单读取）。
 */
const TOKEN_STORAGE_KEY = 'elexvx.auth.token';
const TOKEN_KEY_STORAGE_KEY = 'elexvx.auth.token.key';
const REFRESH_TOKEN_STORAGE_KEY = 'elexvx.auth.refreshToken';
const LEGACY_TOKEN_STORAGE_KEY = 'tdesign.auth.token';
const LEGACY_TOKEN_KEY_STORAGE_KEY = 'tdesign.auth.token.key';
const LEGACY_REFRESH_TOKEN_STORAGE_KEY = 'tdesign.auth.refreshToken';

const encoder = new TextEncoder();
const decoder = new TextDecoder();

const toBase64 = (bytes: Uint8Array) => {
  let binary = '';
  bytes.forEach((b) => {
    binary += String.fromCharCode(b);
  });
  return btoa(binary);
};

const fromBase64 = (value: string) => {
  const binary = atob(value);
  const bytes = new Uint8Array(binary.length);
  for (let i = 0; i < binary.length; i += 1) {
    bytes[i] = binary.charCodeAt(i);
  }
  return bytes;
};

const getSubtle = () => {
  if (typeof window === 'undefined') return null;
  return window.crypto?.subtle ?? null;
};

const readStoredKey = () => {
  if (typeof window === 'undefined') return null;
  migrateLocalStorageKey(TOKEN_KEY_STORAGE_KEY, [LEGACY_TOKEN_KEY_STORAGE_KEY]);
  migrateSessionStorageKey(TOKEN_KEY_STORAGE_KEY, [LEGACY_TOKEN_KEY_STORAGE_KEY]);
  const stored = localStorage.getItem(TOKEN_KEY_STORAGE_KEY);
  if (stored) return stored;
  return sessionStorage.getItem(TOKEN_KEY_STORAGE_KEY);
};

const persistKey = (value: string) => {
  if (typeof window === 'undefined') return;
  localStorage.setItem(TOKEN_KEY_STORAGE_KEY, value);
  sessionStorage.removeItem(TOKEN_KEY_STORAGE_KEY);
};

const loadSessionKey = async () => {
  if (typeof window === 'undefined') return null;
  const subtle = getSubtle();
  if (!subtle) return null;
  const stored = readStoredKey();
  if (!stored) return null;
  if (sessionStorage.getItem(TOKEN_KEY_STORAGE_KEY) && !localStorage.getItem(TOKEN_KEY_STORAGE_KEY)) {
    persistKey(stored);
  }
  const raw = fromBase64(stored);
  return subtle.importKey('raw', raw, { name: 'AES-GCM' }, false, ['encrypt', 'decrypt']);
};

const ensureSessionKey = async () => {
  if (typeof window === 'undefined') return null;
  const subtle = getSubtle();
  if (!subtle) return null;
  const existing = await loadSessionKey();
  if (existing) return existing;
  const key = await subtle.generateKey({ name: 'AES-GCM', length: 256 }, true, ['encrypt', 'decrypt']);
  const raw = new Uint8Array(await subtle.exportKey('raw', key));
  persistKey(toBase64(raw));
  return key;
};

const encryptToken = async (token: string) => {
  const subtle = getSubtle();
  if (!subtle) return token;
  const key = await ensureSessionKey();
  if (!key) return token;
  const iv = window.crypto.getRandomValues(new Uint8Array(12));
  const cipher = await subtle.encrypt({ name: 'AES-GCM', iv }, key, encoder.encode(token));
  const payload = new Uint8Array(iv.length + cipher.byteLength);
  payload.set(iv, 0);
  payload.set(new Uint8Array(cipher), iv.length);
  return toBase64(payload);
};

const decryptToken = async (payload: string) => {
  const subtle = getSubtle();
  if (!subtle) return payload;
  const key = await loadSessionKey();
  if (!key) return '';
  const bytes = fromBase64(payload);
  if (bytes.length <= 12) return '';
  const iv = bytes.slice(0, 12);
  const data = bytes.slice(12);
  try {
    const plain = await subtle.decrypt({ name: 'AES-GCM', iv }, key, data);
    return decoder.decode(plain);
  } catch {
    return '';
  }
};

export const getTokenStorageKey = () => TOKEN_STORAGE_KEY;

export const saveToken = async (token: string) => {
  if (typeof window === 'undefined') return;
  if (!token) {
    localStorage.removeItem(TOKEN_STORAGE_KEY);
    return;
  }
  const encrypted = await encryptToken(token);
  localStorage.setItem(TOKEN_STORAGE_KEY, encrypted);
};

export const loadToken = async () => {
  if (typeof window === 'undefined') return '';
  migrateLocalStorageKey(TOKEN_STORAGE_KEY, [LEGACY_TOKEN_STORAGE_KEY]);
  const stored = localStorage.getItem(TOKEN_STORAGE_KEY);
  if (!stored) return '';
  return decryptToken(stored);
};

export const saveRefreshToken = async (token: string) => {
  if (typeof window === 'undefined') return;
  if (!token) {
    localStorage.removeItem(REFRESH_TOKEN_STORAGE_KEY);
    return;
  }
  const encrypted = await encryptToken(token);
  localStorage.setItem(REFRESH_TOKEN_STORAGE_KEY, encrypted);
};

export const loadRefreshToken = async () => {
  if (typeof window === 'undefined') return '';
  migrateLocalStorageKey(REFRESH_TOKEN_STORAGE_KEY, [LEGACY_REFRESH_TOKEN_STORAGE_KEY]);
  const stored = localStorage.getItem(REFRESH_TOKEN_STORAGE_KEY);
  if (!stored) return '';
  return decryptToken(stored);
};

export const clearTokenStorage = () => {
  if (typeof window === 'undefined') return;
  localStorage.removeItem(TOKEN_STORAGE_KEY);
  localStorage.removeItem(REFRESH_TOKEN_STORAGE_KEY);
  localStorage.removeItem(TOKEN_KEY_STORAGE_KEY);
  localStorage.removeItem(LEGACY_TOKEN_STORAGE_KEY);
  localStorage.removeItem(LEGACY_REFRESH_TOKEN_STORAGE_KEY);
  localStorage.removeItem(LEGACY_TOKEN_KEY_STORAGE_KEY);
  sessionStorage.removeItem(TOKEN_KEY_STORAGE_KEY);
  sessionStorage.removeItem(LEGACY_TOKEN_KEY_STORAGE_KEY);
};
