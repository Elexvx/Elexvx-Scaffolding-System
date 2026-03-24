import { request } from '@/utils/request';

/**
 * 系统-字典 API。
 *
 * 典型用法：
 * - 页面：通过 fetchDictPage/fetchDictItems 管理字典与字典项
 * - 业务：通过 fetchDictItemsByCode 按 code 读取字典项（配合 hooks/useDictionary 与 dictionary store 缓存）
 * - 导入导出：exportDictItems/downloadDictTemplate 使用 blob 响应（返回原始 response）
 */
export interface PageResult<T> {
  list: T[];
  total: number;
}

export interface SysDict {
  id: number;
  name: string;
  code: string;
  status: number;
  sort: number;
  remark?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface SysDictItem {
  id: number;
  dictId: number;
  label: string;
  value: string;
  valueType: string;
  status: number;
  sort: number;
  tagColor?: string;
  province?: string;
  city?: string;
  district?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface DictionaryImportResult {
  total: number;
  imported: number;
  updated: number;
  skipped: number;
  failed: number;
  errors: string[];
}

export const fetchDictPage = (params: { keyword?: string; status?: number | null; page?: number; size?: number }) =>
  request.get<PageResult<SysDict>>({ url: '/system/dict/page', params });

export const fetchDictDetail = (id: number) => request.get<SysDict>({ url: `/system/dict/${id}` });

export const createDict = (data: Partial<SysDict>) => request.post<SysDict>({ url: '/system/dict', data });

export const updateDict = (id: number, data: Partial<SysDict>) =>
  request.put<SysDict>({ url: `/system/dict/${id}`, data });

export const deleteDict = (id: number) => request.delete<boolean>({ url: `/system/dict/${id}` });

export const fetchDictItems = (
  dictId: number,
  params: { keyword?: string; status?: number | null; page?: number; size?: number },
) => request.get<PageResult<SysDictItem>>({ url: `/system/dict/${dictId}/items`, params });

export const fetchDictItemsByCode = (code: string) =>
  request.get<SysDictItem[]>({ url: `/system/dict/code/${code}/items` });

export const createDictItem = (dictId: number, data: Partial<SysDictItem>) =>
  request.post<SysDictItem>({ url: `/system/dict/${dictId}/items`, data });

export const updateDictItem = (id: number, data: Partial<SysDictItem>) =>
  request.put<SysDictItem>({ url: `/system/dict/items/${id}`, data });

export const deleteDictItem = (id: number) => request.delete<boolean>({ url: `/system/dict/items/${id}` });

export const importDictItems = (dictId: number, file: File) => {
  const formData = new FormData();
  formData.append('file', file);
  return request.post<DictionaryImportResult>({
    url: `/system/dict/${dictId}/items/import`,
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' },
  });
};

export const exportDictItems = async (dictId: number) =>
  request.get(
    {
      url: `/system/dict/${dictId}/items/export`,
      responseType: 'blob',
    } as any,
    { isReturnNativeResponse: true },
  );

export const downloadDictTemplate = async () =>
  request.get({ url: '/system/dict/items/template', responseType: 'blob' } as any, { isReturnNativeResponse: true });
