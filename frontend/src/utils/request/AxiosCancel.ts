import type { AxiosRequestConfig, Canceler } from 'axios';
import axios from 'axios';
import isFunction from 'lodash/isFunction';

import type { RequestDedupeOptions } from '@/types/axios';

interface PendingItem {
  cancel: Canceler;
  createdAt: number;
}

const DEFAULT_DEDUPE_WINDOW_MS = 1000;
const DEFAULT_DEDUPE_STRATEGY: Required<RequestDedupeOptions>['strategy'] = 'cancelPrevious';

// 存储请求与取消令牌的键值对列表
let pendingMap = new Map<string, PendingItem>();

const isPlainObject = (value: unknown): value is Record<string, unknown> =>
  Object.prototype.toString.call(value) === '[object Object]';

const stableStringify = (value: unknown): string => {
  if (value == null) return '';
  if (typeof value === 'string') return value;
  if (typeof value === 'number' || typeof value === 'boolean') return String(value);
  if (Array.isArray(value)) return `[${value.map((item) => stableStringify(item)).join(',')}]`;
  if (isPlainObject(value)) {
    const sortedEntries = Object.entries(value)
      .filter(([, val]) => val !== undefined)
      .sort(([a], [b]) => a.localeCompare(b));
    return `{${sortedEntries.map(([k, v]) => `${k}:${stableStringify(v)}`).join(',')}}`;
  }
  try {
    return JSON.stringify(value);
  } catch {
    return String(value);
  }
};

const getPathWithoutQuery = (url?: string): string => {
  if (!url) return '';
  const base = url.split('?')[0] || '';
  return base;
};

const isSkipUrl = (url: string, skipUrls: Array<string | RegExp> = []): boolean => {
  if (!url || !skipUrls.length) return false;
  return skipUrls.some((pattern) => {
    if (typeof pattern === 'string') {
      return url.includes(pattern);
    }
    return pattern.test(url);
  });
};

const getDedupeOptions = (config: AxiosRequestConfig, defaults?: RequestDedupeOptions): RequestDedupeOptions => {
  const requestOptions = (config as Recordable)?.requestOptions as { dedupe?: RequestDedupeOptions } | undefined;
  return {
    ...(defaults || {}),
    ...(requestOptions?.dedupe || {}),
  };
};

/**
 * 获取请求唯一 key（method + url + params + data）
 */
export const getPendingUrl = (config: AxiosRequestConfig) => {
  const method = String(config.method || 'get').toUpperCase();
  const url = String(config.url || '');
  const params = stableStringify(config.params);
  const data = stableStringify(config.data);
  return [method, url, params, data].join('&');
};

/**
 * @description 请求管理器
 */
export class AxiosCanceler {
  /**
   * 添加请求到列表中
   * @param config
   */
  addPending(config: AxiosRequestConfig, defaults?: RequestDedupeOptions) {
    const dedupe = getDedupeOptions(config, defaults);
    const enabled = dedupe.enabled ?? true;
    if (!enabled) return;

    const path = getPathWithoutQuery(String(config.url || ''));
    if (isSkipUrl(path, dedupe.skipUrls)) return;

    const pendingKey = getPendingUrl(config);
    const now = Date.now();
    const windowMs = dedupe.windowMs ?? DEFAULT_DEDUPE_WINDOW_MS;
    const strategy = dedupe.strategy ?? DEFAULT_DEDUPE_STRATEGY;

    const existing = pendingMap.get(pendingKey);
    if (existing && now - existing.createdAt <= windowMs) {
      if (strategy === 'cancelCurrent') {
        config.cancelToken =
          config.cancelToken ||
          new axios.CancelToken((cancel) => {
            cancel(`[dedupe] canceled current request: ${pendingKey}`);
          });
        return;
      }
      existing.cancel(`[dedupe] canceled previous request: ${pendingKey}`);
      pendingMap.delete(pendingKey);
    }

    config.cancelToken =
      config.cancelToken ||
      new axios.CancelToken((cancel) => {
        if (!pendingMap.has(pendingKey)) {
          pendingMap.set(pendingKey, { cancel, createdAt: now });
        }
      });
  }

  /**
   * 移除现有的所有请求
   */
  removeAllPending() {
    pendingMap.forEach(({ cancel }) => {
      if (cancel && isFunction(cancel)) cancel();
    });
    pendingMap.clear();
  }

  /**
   * 移除指定请求
   * @param config
   */
  removePending(config: AxiosRequestConfig) {
    const pendingKey = getPendingUrl(config);

    if (pendingMap.has(pendingKey)) {
      pendingMap.delete(pendingKey);
    }
  }

  /**
   * 重置
   */
  reset() {
    pendingMap = new Map<string, PendingItem>();
  }
}
