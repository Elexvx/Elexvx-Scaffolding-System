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
  DashboardPage,
  'dashboard/index': DashboardPage,
  'dashboard/workbench': DashboardPage,
  UserIndex: AccountCenterPage,
  'user/index': AccountCenterPage,
  'account/center': AccountCenterPage,
  SystemUser: UserPage,
  'system/user/index': UserPage,
  SystemRole: RolePage,
  'system/role/index': RolePage,
  SystemOrg: OrgPage,
  'system/org/index': OrgPage,
  SystemMenu: MenuPage,
  'system/menu/index': MenuPage,
  SystemLog: LogMonitorPage,
  'system/log/index': LogMonitorPage,
  'system/monitor/log/index': LogMonitorPage,
  SystemPersonalize: SystemConfigPage,
  SystemVerification: SystemConfigPage,
  SystemSecurity: SystemConfigPage,
  SystemStorage: SystemConfigPage,
  'system/personalize/index': SystemConfigPage,
  'system/verification/index': SystemConfigPage,
  'system/security/index': SystemConfigPage,
  'system/storage/index': SystemConfigPage,
  AnnouncementTable: AnnouncementPage,
  AnnouncementCards: AnnouncementPage,
  'announcement/table/index': AnnouncementPage,
  'announcement/cards/index': AnnouncementPage,
  NotificationTable: NotificationPage,
  'system/notification/index': NotificationPage,
  Message: MessagePage,
  'system/message/index': MessagePage,
};
