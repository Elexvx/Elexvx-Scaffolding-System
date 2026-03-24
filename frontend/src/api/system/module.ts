import type { AxiosResponse } from 'axios';

import { request } from '@/utils/request';

export interface ModuleDescriptor {
  key: string;
  name: string;
  source: string;
  license: string;
  version: string;
  enabled: boolean;
}

export interface ModuleRegistryItem {
  moduleKey: string;
  name: string;
  version: string;
  enabled: boolean;
  installState?: string;
  installedAt?: string | null;
}

export function fetchModuleList() {
  return request.get<ModuleDescriptor[]>({ url: '/system/modules/descriptor' });
}

export function fetchModules() {
  return request.get<ModuleRegistryItem[]>({ url: '/system/modules' });
}

export function enableModule(moduleKey: string) {
  return request.post<ModuleRegistryItem>({ url: `/system/modules/${moduleKey}/enable` });
}

export function disableModule(moduleKey: string) {
  return request.post<ModuleRegistryItem>({ url: `/system/modules/${moduleKey}/disable` });
}

export function installModule(moduleKey: string) {
  return request.post<ModuleRegistryItem>({ url: `/system/modules/${moduleKey}/install` });
}

export function uninstallModule(moduleKey: string) {
  return request.post<ModuleRegistryItem>({ url: `/system/modules/${moduleKey}/uninstall` });
}

export function downloadModulePackage(moduleKey: string) {
  return request.get<AxiosResponse<Blob>>(
    { url: `/system/modules/${moduleKey}/package`, responseType: 'blob' },
    { isReturnNativeResponse: true, isTransformResponse: false },
  );
}
