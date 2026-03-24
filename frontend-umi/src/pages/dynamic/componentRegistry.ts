/**
 * Elexvx Admin - Menu Component Adapter
 * 首次编写时间：2026-03-24
 * Copyright (c) Elexvx. All rights reserved.
 */
import type { ComponentType } from 'react';

import AccountCenterPage from '@/pages/account/center';
import AnnouncementCardsPage from '@/pages/announcement/cards';
import AnnouncementTablePage from '@/pages/announcement/table';
import ConsoleDownloadPage from '@/pages/console/download';
import DashboardPage from '@/pages/dashboard';
import ExampleGoodsPage from '@/pages/example/goods';
import ExampleOrderPage from '@/pages/example/order';
import MessageSendPage from '@/pages/message/send';
import NotificationTablePage from '@/pages/notification/table';
import BrowserIncompatiblePage from '@/pages/result/browser-incompatible';
import FailResultPage from '@/pages/result/fail';
import MaintenanceResultPage from '@/pages/result/maintenance';
import NetworkErrorResultPage from '@/pages/result/network-error';
import SuccessResultPage from '@/pages/result/success';
import DictPage from '@/pages/system/dict';
import SystemLogPage from '@/pages/system/log';
import MenuPage from '@/pages/system/menu';
import ModulesPage from '@/pages/system/modules';
import OnlineUserPage from '@/pages/system/monitor/online-user';
import RedisMonitorPage from '@/pages/system/monitor/redis';
import ServerMonitorPage from '@/pages/system/monitor/server';
import OrgPage from '@/pages/system/org';
import SystemPersonalizePage from '@/pages/system/personalize';
import RolePage from '@/pages/system/role';
import SystemSecurityPage from '@/pages/system/security';
import SensitivePage from '@/pages/system/sensitive';
import SystemStoragePage from '@/pages/system/storage';
import UserPage from '@/pages/system/user';
import SystemVerificationPage from '@/pages/system/verification';
import WatermarkPage from '@/pages/system/watermark';
import UserIndexPage from '@/pages/user';

export const componentRegistry: Record<string, ComponentType> = {
  DashboardPage,
  'dashboard/index': DashboardPage,
  'dashboard/workbench': DashboardPage,

  UserIndex: UserIndexPage,
  'user/index': UserIndexPage,
  UserProfile: AccountCenterPage,
  user: AccountCenterPage,
  'account/center': AccountCenterPage,

  SystemUser: UserPage,
  'system/user/index': UserPage,
  SystemRole: RolePage,
  'system/role/index': RolePage,
  SystemOrg: OrgPage,
  'system/org/index': OrgPage,
  SystemMenu: MenuPage,
  'system/menu/index': MenuPage,
  SystemLog: SystemLogPage,
  'system/log/index': SystemLogPage,
  'system/monitor/log/index': SystemLogPage,
  SystemDict: DictPage,
  'system/dict/index': DictPage,
  SystemModules: ModulesPage,
  'system/modules/index': ModulesPage,
  SystemSensitive: SensitivePage,
  'system/sensitive/index': SensitivePage,
  SystemWatermark: WatermarkPage,
  'system/watermark/index': WatermarkPage,

  SystemMonitorOnlineUser: OnlineUserPage,
  SystemOnlineUser: OnlineUserPage,
  'system/monitor/online-user/index': OnlineUserPage,
  SystemMonitorRedis: RedisMonitorPage,
  SystemRedis: RedisMonitorPage,
  'system/monitor/redis/index': RedisMonitorPage,
  SystemMonitorServer: ServerMonitorPage,
  SystemServer: ServerMonitorPage,
  'system/monitor/server/index': ServerMonitorPage,

  SystemPersonalize: SystemPersonalizePage,
  'system/personalize/index': SystemPersonalizePage,
  SystemVerification: SystemVerificationPage,
  'system/verification/index': SystemVerificationPage,
  SystemSecurity: SystemSecurityPage,
  'system/security/index': SystemSecurityPage,
  SystemStorage: SystemStoragePage,
  'system/storage/index': SystemStoragePage,

  AnnouncementCards: AnnouncementCardsPage,
  'announcement/cards/index': AnnouncementCardsPage,
  AnnouncementTable: AnnouncementTablePage,
  'announcement/table/index': AnnouncementTablePage,

  NotificationTable: NotificationTablePage,
  'notification/table/index': NotificationTablePage,
  'system/notification/index': NotificationTablePage,

  Message: MessageSendPage,
  MessageSend: MessageSendPage,
  'message/send/index': MessageSendPage,
  'system/message/index': MessageSendPage,

  ConsoleDownload: ConsoleDownloadPage,
  'console/download/index': ConsoleDownloadPage,

  ExampleGoods: ExampleGoodsPage,
  'example/goods/index': ExampleGoodsPage,
  ExampleOrder: ExampleOrderPage,
  'example/order/index': ExampleOrderPage,

  ResultBrowserIncompatible: BrowserIncompatiblePage,
  ResultFail: FailResultPage,
  ResultMaintenance: MaintenanceResultPage,
  ResultNetworkError: NetworkErrorResultPage,
  ResultSuccess: SuccessResultPage,
  'result/browser-incompatible/index': BrowserIncompatiblePage,
  'result/fail/index': FailResultPage,
  'result/maintenance/index': MaintenanceResultPage,
  'result/network-error/index': NetworkErrorResultPage,
  'result/success/index': SuccessResultPage,
};
