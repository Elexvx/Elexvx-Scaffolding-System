import { request } from '@umijs/max';

import type { PageQuery } from '@/types/api';
import type {
  AnnouncementRow,
  DictRow,
  MenuRow,
  MessageRow,
  ModuleRow,
  NotificationRow,
  OnlineUserRow,
  OperationLogRow,
  OrgRow,
  RedisMonitorData,
  RoleRow,
  SensitiveWordRow,
  ServerMonitorData,
  UserRow,
  WatermarkSetting,
} from '@/types/system';
import { normalizePageParams, normalizePageResult, unwrapApiEnvelope } from '@/utils/request';

export async function queryUsers(params: PageQuery) {
  const payload = await request('/system/user/page', { params: normalizePageParams(params) });
  return normalizePageResult<UserRow>(unwrapApiEnvelope(payload), params);
}

export async function queryRoles(params: PageQuery = {}) {
  const payload = await request('/system/role/list');
  return normalizePageResult<RoleRow>(unwrapApiEnvelope(payload), params);
}

export async function queryMenuTree(params: PageQuery = {}) {
  const payload = await request('/system/menu/tree');
  return normalizePageResult<MenuRow>(unwrapApiEnvelope(payload), params);
}

export async function queryOrgTree(params: PageQuery = {}) {
  const payload = await request('/system/org/tree');
  return normalizePageResult<OrgRow>(unwrapApiEnvelope(payload), params);
}

export async function queryAnnouncements(params: PageQuery) {
  const payload = await request('/announcement', { params: normalizePageParams(params) });
  return normalizePageResult<AnnouncementRow>(unwrapApiEnvelope(payload), params);
}

export async function queryMessages(params: PageQuery) {
  const payload = await request('/message/list');
  return normalizePageResult<MessageRow>(unwrapApiEnvelope(payload), params);
}

export async function queryNotifications(params: PageQuery) {
  const payload = await request('/notification', { params: normalizePageParams(params) });
  return normalizePageResult<NotificationRow>(unwrapApiEnvelope(payload), params);
}

export async function queryUiSetting() {
  const payload = await request('/system/ui');
  return unwrapApiEnvelope<Record<string, unknown>>(payload);
}

export async function queryLogs(params: PageQuery) {
  const payload = await request('/system/log/page', { params: normalizePageParams(params) });
  return normalizePageResult<OperationLogRow>(unwrapApiEnvelope(payload), params);
}

export async function queryDicts(params: PageQuery) {
  const payload = await request('/system/dict/page', { params: normalizePageParams(params) });
  return normalizePageResult<DictRow>(unwrapApiEnvelope(payload), params);
}

export async function queryModules(params: PageQuery = {}) {
  const payload = await request('/system/modules');
  return normalizePageResult<ModuleRow>(unwrapApiEnvelope(payload), params);
}

export async function querySensitiveWords(params: PageQuery) {
  const payload = await request('/system/sensitive/words/page', { params: normalizePageParams(params) });
  return normalizePageResult<SensitiveWordRow>(unwrapApiEnvelope(payload), params);
}

export async function queryWatermarkSetting() {
  const payload = await request('/system/watermark');
  return unwrapApiEnvelope<WatermarkSetting>(payload);
}

export async function queryOnlineUsers(params: PageQuery) {
  const payload = await request('/system/monitor/online-user', { params: normalizePageParams(params) });
  return normalizePageResult<OnlineUserRow>(unwrapApiEnvelope(payload), params);
}

export async function queryRedisMonitor() {
  const payload = await request('/system/monitor/redis');
  return unwrapApiEnvelope<RedisMonitorData>(payload);
}

export async function queryServerMonitor() {
  const payload = await request('/system/monitor/server');
  return unwrapApiEnvelope<ServerMonitorData>(payload);
}
