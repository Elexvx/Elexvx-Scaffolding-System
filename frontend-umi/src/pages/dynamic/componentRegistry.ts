/**
 * Elexvx Admin - Menu Component Adapter
 * 首次编写时间：2026-03-24
 * Copyright (c) Elexvx. All rights reserved.
 */
import type { ComponentType } from 'react';

import AccountCenterPage from '@/pages/account/center';
import DashboardPage from '@/pages/dashboard';
import AnnouncementPage from '@/pages/system/announcement';
import DictPage from '@/pages/system/dict';
import SystemConfigPage from '@/pages/system/config';
import ModulesPage from '@/pages/system/modules';
import OnlineUserPage from '@/pages/system/monitor/online-user';
import LogMonitorPage from '@/pages/system/monitor/log';
import RedisMonitorPage from '@/pages/system/monitor/redis';
import ServerMonitorPage from '@/pages/system/monitor/server';
import MenuPage from '@/pages/system/menu';
import MessagePage from '@/pages/system/message';
import NotificationPage from '@/pages/system/notification';
import OrgPage from '@/pages/system/org';
import RolePage from '@/pages/system/role';
import SensitivePage from '@/pages/system/sensitive';
import UserPage from '@/pages/system/user';
import WatermarkPage from '@/pages/system/watermark';

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
  SystemDict: DictPage,
  'system/dict/index': DictPage,
  SystemModules: ModulesPage,
  'system/modules/index': ModulesPage,
  SystemSensitive: SensitivePage,
  'system/sensitive/index': SensitivePage,
  SystemWatermark: WatermarkPage,
  'system/watermark/index': WatermarkPage,
  SystemOnlineUser: OnlineUserPage,
  'system/monitor/online-user/index': OnlineUserPage,
  SystemRedis: RedisMonitorPage,
  'system/monitor/redis/index': RedisMonitorPage,
  SystemServer: ServerMonitorPage,
  'system/monitor/server/index': ServerMonitorPage,
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
  'notification/table/index': NotificationPage,
  'system/notification/index': NotificationPage,
  Message: MessagePage,
  MessageSend: MessagePage,
  'message/send/index': MessagePage,
  'system/message/index': MessagePage,
  UserProfile: AccountCenterPage,
  user: AccountCenterPage,
};
