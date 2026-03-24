import { request } from '@/utils/request';

export interface StorageSetting {
  provider: 'LOCAL' | 'ALIYUN' | 'TENCENT' | string;
  bucket?: string;
  region?: string;
  endpoint?: string;
  accessKey?: string;
  secretKey?: string;
  customDomain?: string;
  pathPrefix?: string;
  publicRead?: boolean;
  reuseSecret?: boolean;
  secretConfigured?: boolean;
}

export function fetchStorageSetting() {
  return request.get<StorageSetting>({ url: '/system/storage' });
}

export function saveStorageSetting(data: StorageSetting) {
  return request.post<StorageSetting>({ url: '/system/storage', data });
}

export function testStorageSetting(data: StorageSetting) {
  return request.post<void>({ url: '/system/storage/test', data });
}
