import { request } from '@umijs/max';

import type { AppMenuItem, BackendMenuItem } from '@/types/menu';
import { unwrapApiEnvelope } from '@/utils/request';

export async function queryBackendMenus() {
  const payload = await request('/api/get-menu-list-i18n');
  return unwrapApiEnvelope<BackendMenuItem[]>(payload);
}

function resolveComponentKey(item: BackendMenuItem) {
  if (item.componentKey) {
    return item.componentKey;
  }
  const raw = String(item.component || '').replace(/^\/?src\//, '').replace(/\.vue$/i, '');
  return raw.replace(/^views\//, '').replace(/^pages\//, '');
}

export function adaptBackendMenu(items: BackendMenuItem[]): AppMenuItem[] {
  return items
    .map((item) => {
      const children = item.children ? adaptBackendMenu(item.children) : undefined;
      return {
        id: String(item.id ?? item.path),
        name: item.meta?.title || item.name,
        path: item.path,
        icon: item.meta?.icon,
        permCode: item.meta?.requiredPermissions?.[0],
        componentKey: resolveComponentKey(item),
        hidden: item.meta?.hidden,
        orderNo: item.meta?.orderNo ?? 0,
        children,
      };
    })
    .sort((a, b) => (a.orderNo ?? 0) - (b.orderNo ?? 0));
}
