import { resolveApiHost } from '@/utils/apiHost';

/**
 * SSE URL 构造工具。
 *
 * 统一规则：host（resolveApiHost）+ VITE_API_URL_PREFIX + path + query。
 * 适用于 EventSource/Fetch SSE 等需要完整绝对地址的场景。
 */
function joinUrl(base: string, path: string) {
  if (!base) return path;
  if (!path) return base;
  if (base.endsWith('/') && path.startsWith('/')) return `${base.slice(0, -1)}${path}`;
  if (!base.endsWith('/') && !path.startsWith('/')) return `${base}/${path}`;
  return `${base}${path}`;
}

export function buildSseUrl(path: string, params: Record<string, string> = {}) {
  const host = resolveApiHost();
  const prefix = import.meta.env.VITE_API_URL_PREFIX || '';
  const base = `${host}${prefix}`;
  const url = joinUrl(base, path);
  const query = new URLSearchParams(params).toString();
  return query ? `${url}?${query}` : url;
}
