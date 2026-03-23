/**
 * Elexvx Admin - Request Runtime
 * 首次编写时间：2026-03-24
 * Copyright (c) Elexvx. All rights reserved.
 */
import { message } from 'antd';

import type { ApiEnvelope, NormalizedPageResult, PageQuery } from '@/types/api';

export function unwrapApiEnvelope<T>(payload: ApiEnvelope<T> | T): T {
  if (!payload || typeof payload !== 'object' || !('code' in payload)) {
    return payload as T;
  }
  const envelope = payload as ApiEnvelope<T>;
  const isSuccess = envelope.success === true || (envelope.success == null && envelope.code === 0);
  if (isSuccess) {
    return envelope.data;
  }
  throw new Error(envelope.userTip || envelope.message || `请求失败(${envelope.code})`);
}

export function notifyRequestError(error: unknown) {
  const content = error instanceof Error ? error.message : '请求异常，请稍后重试';
  message.error(content);
}

export function normalizePageParams(params: PageQuery = {}) {
  const current = Number(params.current ?? 1);
  const pageSize = Number(params.pageSize ?? 10);
  const { current: _current, pageSize: _pageSize, ...rest } = params;
  return {
    ...rest,
    page: Number.isFinite(current) && current > 0 ? current - 1 : 0,
    size: Number.isFinite(pageSize) && pageSize > 0 ? pageSize : 10,
  };
}

export function normalizePageResult<T>(raw: unknown, fallback?: Partial<PageQuery>): NormalizedPageResult<T> {
  if (Array.isArray(raw)) {
    return {
      data: raw as T[],
      total: raw.length,
      page: Math.max(Number(fallback?.current ?? 1), 1),
      size: Math.max(Number(fallback?.pageSize ?? raw.length ?? 10), 1),
    };
  }
  if (!raw || typeof raw !== 'object') {
    return { data: [], total: 0, page: 1, size: Math.max(Number(fallback?.pageSize ?? 10), 1) };
  }
  const result = raw as Record<string, unknown>;
  const records = Array.isArray(result.records)
    ? result.records
    : Array.isArray(result.list)
      ? result.list
      : Array.isArray(result.items)
        ? result.items
        : Array.isArray(result.rows)
          ? result.rows
          : [];
  const current = Number(result.current ?? result.page ?? result.pageNum ?? fallback?.current ?? 1);
  const size = Number(result.size ?? result.pageSize ?? (fallback?.pageSize ?? records.length ?? 10));
  const total = Number(result.total ?? result.count ?? result.totalElements ?? records.length ?? 0);
  return {
    data: records as T[],
    total: Number.isFinite(total) ? total : records.length,
    page: Number.isFinite(current) ? current + (result.page !== undefined ? 1 : 0) : 1,
    size: Number.isFinite(size) && size > 0 ? size : 10,
  };
}
