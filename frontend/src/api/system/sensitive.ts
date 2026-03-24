import { request } from '@/utils/request';

export interface SensitiveWord {
  id: number;
  word: string;
  enabled: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface SensitivePageSetting {
  id?: number;
  pageKey: string;
  pageName?: string;
  enabled: boolean;
}

export interface SensitiveSettings {
  enabled: boolean;
  pages: SensitivePageSetting[];
}

export interface SensitiveImportResult {
  total: number;
  imported: number;
  skipped: number;
  failed: number;
  errors: string[];
}

export interface PageResult<T> {
  list: T[];
  total: number;
}

export function fetchSensitiveWords(params?: { keyword?: string; page?: number; size?: number }) {
  return request.get<PageResult<SensitiveWord>>({ url: '/system/sensitive/words/page', params });
}

export function createSensitiveWord(word: string) {
  return request.post<SensitiveWord>({ url: '/system/sensitive/words', data: { word } });
}

export function deleteSensitiveWord(id: number) {
  return request.delete<boolean>({ url: `/system/sensitive/words/${id}` });
}

export function fetchSensitiveSettings() {
  return request.get<SensitiveSettings>({ url: '/system/sensitive/settings' });
}

export function saveSensitiveSettings(data: Partial<SensitiveSettings>) {
  return request.post<SensitiveSettings>({ url: '/system/sensitive/settings', data });
}

export function downloadSensitiveWordsTemplate() {
  return request.get(
    {
      url: '/system/sensitive/words/template',
      responseType: 'blob',
    },
    { isTransformResponse: false, isReturnNativeResponse: true },
  );
}
