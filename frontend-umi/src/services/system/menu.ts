import { request } from 'umi';

import type { AppMenuItem, BackendMenuItem, BackendMenuTitleMap } from '@/types/menu';
import { unwrapApiEnvelope } from '@/utils/request';

interface MenuListResult {
  list?: BackendMenuItem[];
}

function resolveMenuTitle(title?: string | BackendMenuTitleMap, fallback?: string) {
  if (typeof title === 'string' && title.trim()) {
    return title.trim();
  }
  if (title && typeof title === 'object') {
    return title.zh_CN || title.en_US || fallback || '未命名菜单';
  }
  return fallback || '未命名菜单';
}

function normalizeComponentKey(item: BackendMenuItem) {
  if (item.componentKey) {
    return item.componentKey;
  }
  const raw = String(item.component || '')
    .replace(/^\/?src\//, '')
    .replace(/\.vue$/i, '')
    .replace(/^views\//, '')
    .replace(/^pages\//, '')
    .replace(/^\//, '');
  if (!raw || raw === 'LAYOUT') {
    return undefined;
  }
  return raw;
}

export async function queryBackendMenus() {
  const payload = await request('/get-menu-list-i18n');
  const data = unwrapApiEnvelope<MenuListResult | BackendMenuItem[]>(payload);
  if (Array.isArray(data)) {
    return data;
  }
  return data?.list ?? [];
}

export function adaptBackendMenu(items: BackendMenuItem[]): AppMenuItem[] {
  return items
    .map((item) => {
      const children = item.children ? adaptBackendMenu(item.children) : undefined;
      const permissions = item.meta?.requiredPermissions ?? [];
      return {
        id: String(item.path || item.name || item.id),
        name: resolveMenuTitle(item.meta?.title, item.name),
        path: item.path,
        icon: item.meta?.icon,
        permCode: permissions[0],
        permissionCodes: permissions,
        resource: item.meta?.resource,
        actions: item.meta?.actions,
        componentKey: normalizeComponentKey(item),
        routeName: item.name,
        redirect: item.redirect,
        hidden: item.meta?.hidden,
        orderNo: item.meta?.orderNo ?? 0,
        children,
      };
    })
    .sort((a, b) => (a.orderNo ?? 0) - (b.orderNo ?? 0));
}
