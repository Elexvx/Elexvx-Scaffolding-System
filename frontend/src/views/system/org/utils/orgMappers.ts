import dayjs from 'dayjs';

import type { OrgFormModel, OrgUnitNode, UserRow } from '../types';

export const applyOrgRowToForm = (form: OrgFormModel, row: OrgUnitNode) => {
  form.parentId = row.parentId;
  form.name = row.name;
  form.shortName = row.shortName || '';
  form.type = row.type || 'DEPARTMENT';
  form.status = row.status ?? 1;
  form.phone = row.phone || '';
  form.email = row.email || '';
  form.leaderIds = [...(row.leaderIds || [])];
};

export const createOrgSubmitPayload = (form: OrgFormModel) => ({
  parentId: form.parentId ?? null,
  name: form.name,
  shortName: form.shortName || undefined,
  type: form.type,
  status: form.status,
  leaderIds: form.leaderIds,
  phone: form.phone || undefined,
  email: form.email || undefined,
});

export const formatNameList = (names?: string[]) => {
  if (!names || names.length === 0) return '-';
  return names.join(' / ');
};

export const formatOrgTime = (value?: string) => {
  if (!value) return '-';
  return dayjs(value).format('YYYY-MM-DD HH:mm');
};

export const mapSelectedUsers = (ids: number[], names: string[]): UserRow[] =>
  ids.map((id, index) => ({
    id,
    name: names[index] || `用户${id}`,
    account: '',
  }));
