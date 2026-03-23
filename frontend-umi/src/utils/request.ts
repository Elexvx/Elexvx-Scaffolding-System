/**
 * Elexvx Admin - Request Runtime
 * 首次编写时间：2026-03-24
 * Copyright (c) Elexvx. All rights reserved.
 */
import { message } from 'antd';

import type { ApiEnvelope } from '@/types/api';

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

export function normalizePageResult<T>(raw: unknown): { data: T[]; total: number } {
  if (!raw || typeof raw !== 'object') {
    return { data: [], total: 0 };
  }
  const result = raw as Record<string, unknown>;
  const records = Array.isArray(result.records)
    ? result.records
    : Array.isArray(result.list)
      ? result.list
      : Array.isArray(result.items)
        ? result.items
        : [];
  const total = Number(result.total ?? result.count ?? records.length ?? 0);
  return { data: records as T[], total };
}
