import { request } from '@/utils/request';

export interface OperationLogExportParams {
  keyword?: string;
  action?: string;
  start?: string;
  end?: string;
}

export function exportOperationLogsCsv(params?: OperationLogExportParams) {
  return request.get(
    {
      url: '/system/log/export',
      params,
      responseType: 'blob',
    },
    { isTransformResponse: false, isReturnNativeResponse: true },
  );
}
