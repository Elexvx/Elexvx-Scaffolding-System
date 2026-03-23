import { request } from '@umijs/max';

import type { PageQuery } from '@/types/api';
import type {
  AnnouncementRow,
  MenuRow,
  MessageRow,
  NotificationRow,
  OperationLogRow,
  OrgRow,
  RoleRow,
  UserRow,
} from '@/types/system';
import { normalizePageResult, unwrapApiEnvelope } from '@/utils/request';

export async function queryUsers(params: PageQuery) {
  const payload = await request('/api/system/user/page', { params });
  return normalizePageResult<UserRow>(unwrapApiEnvelope(payload));
}

export async function queryRoles() {
  const payload = await request('/api/system/role/list');
  const data = unwrapApiEnvelope<RoleRow[]>(payload);
  return data ?? [];
}

export async function queryMenuTree() {
  const payload = await request('/api/system/menu/tree');
  return (unwrapApiEnvelope<MenuRow[]>(payload) ?? []) as MenuRow[];
}

export async function queryOrgTree() {
  const payload = await request('/api/system/org/tree');
  return (unwrapApiEnvelope<OrgRow[]>(payload) ?? []) as OrgRow[];
}

export async function queryAnnouncements(params: PageQuery) {
  const payload = await request('/api/announcement', { params });
  return normalizePageResult<AnnouncementRow>(unwrapApiEnvelope(payload));
}

export async function queryMessages(params: PageQuery) {
  const payload = await request('/api/message/list', { params });
  return normalizePageResult<MessageRow>(unwrapApiEnvelope(payload));
}

export async function queryNotifications(params: PageQuery) {
  const payload = await request('/api/notification', { params });
  return normalizePageResult<NotificationRow>(unwrapApiEnvelope(payload));
}

export async function queryUiSetting() {
  const payload = await request('/api/system/ui');
  return unwrapApiEnvelope<Record<string, unknown>>(payload);
}

export async function queryLogs(params: PageQuery) {
  const payload = await request('/api/system/log/page', { params });
  return normalizePageResult<OperationLogRow>(unwrapApiEnvelope(payload));
}
