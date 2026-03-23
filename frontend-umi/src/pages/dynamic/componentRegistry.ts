/**
 * Elexvx Admin - Menu Component Adapter
 * 首次编写时间：2026-03-24
 * Copyright (c) Elexvx. All rights reserved.
 */
import type { ComponentType } from 'react';

import AccountCenterPage from '@/pages/account/center';
import DashboardPage from '@/pages/dashboard';
import AnnouncementPage from '@/pages/system/announcement';
import SystemConfigPage from '@/pages/system/config';
import LogMonitorPage from '@/pages/system/monitor/log';
import MenuPage from '@/pages/system/menu';
import MessagePage from '@/pages/system/message';
import NotificationPage from '@/pages/system/notification';
import OrgPage from '@/pages/system/org';
import RolePage from '@/pages/system/role';
import UserPage from '@/pages/system/user';

export const componentRegistry: Record<string, ComponentType> = {
  'dashboard/workbench': DashboardPage,
  'account/center': AccountCenterPage,
  'system/user/index': UserPage,
  'system/role/index': RolePage,
  'system/menu/index': MenuPage,
  'system/org/index': OrgPage,
  'system/announcement/index': AnnouncementPage,
  'system/message/index': MessagePage,
  'system/notification/index': NotificationPage,
  'system/config/index': SystemConfigPage,
  'system/monitor/log/index': LogMonitorPage,
};
