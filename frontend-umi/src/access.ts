/**
 * Elexvx Admin - Access Runtime
 * 首次编写时间：2026-03-24
 * Copyright (c) Elexvx. All rights reserved.
 */
import type { AppMenuItem } from '@/types/menu';

type InitialState = {
  permissions: string[];
  menus: AppMenuItem[];
};

function flattenPermissions(menus: AppMenuItem[]) {
  const set = new Set<string>();
  const stack = [...menus];
  while (stack.length > 0) {
    const current = stack.pop()!;
    if (current.permCode) {
      set.add(current.permCode);
    }
    if (current.children?.length) {
      stack.push(...current.children);
    }
  }
  return set;
}

export default function access(initialState: InitialState) {
  const menuPermissionSet = flattenPermissions(initialState?.menus ?? []);
  const userPermissionSet = new Set(initialState?.permissions ?? []);
  const hasPermission = (perm: string) => {
    if (!perm) {
      return true;
    }
    return userPermissionSet.has(perm) || menuPermissionSet.has(perm);
  };
  return {
    hasPermission,
    canViewDashboard: true,
    canAccessAccountCenter: true,
  };
}
