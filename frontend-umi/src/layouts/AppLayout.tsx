/**
 * Elexvx Admin - Layout Helper
 * 首次编写时间：2026-03-24
 * Copyright (c) Elexvx. All rights reserved.
 */
import { useModel } from 'umi';

import type { AppMenuItem } from '@/types/menu';

export function useAppMenus(): AppMenuItem[] {
  const { initialState } = useModel('@@initialState');
  return initialState?.menus ?? [];
}
