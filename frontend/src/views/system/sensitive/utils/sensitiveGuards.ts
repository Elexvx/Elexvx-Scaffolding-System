import type { SensitiveImportResult } from '@/api/system/sensitive';

import type { SensitiveImportParseResult } from '../types';

export const trimSensitiveWord = (value: string) => String(value || '').trim();

export const parseSensitiveImportResponse = (responseRaw: unknown): SensitiveImportParseResult => {
  const response = (responseRaw || {}) as Record<string, any>;
  if (response.code !== undefined && response.code !== 0) {
    return {
      message: response.message || '导入失败',
      errors: [],
      hasErrorCode: true,
    };
  }
  const result: SensitiveImportResult | undefined = response.data || response.result || response?.data?.data;
  if (!result) {
    return {
      message: '导入完成，但未获取到统计结果',
      errors: [],
      hasErrorCode: false,
    };
  }
  return {
    message: `导入完成：读取 ${result.total} 条，成功 ${result.imported} 条，跳过 ${result.skipped} 条，失败 ${result.failed} 条`,
    errors: result.errors || [],
    hasErrorCode: false,
  };
};
