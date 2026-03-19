import type { PrimaryTableCol, SelectOption } from 'tdesign-vue-next';

import type { CompletionTodoConfig, LoginLogRow } from '../types';

export const mobileBreakpoint = 768;
export const desktopBreakpoint = 1200;

export const sectionTargetMap: Record<string, string> = {
  basic: 'user-block-basic',
  document: 'user-block-document',
  security: 'user-block-security',
};

export const docTypeResidentIdCard = 'resident_id_card';
export const docTypePassport = 'passport';
export const residentIdCardWeights = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2];
export const residentIdCardChecksumCodes = ['1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'];

export const documentTypeFallbackOptions: SelectOption[] = [
  { label: '居民身份证', value: docTypeResidentIdCard },
  { label: '护照', value: docTypePassport },
];

export const loginLogColumns: PrimaryTableCol[] = [
  { colKey: 'createdAt', title: '登录时间', width: 180 },
  { colKey: 'ipAddress', title: 'IP', width: 140 },
  { colKey: 'deviceInfo', title: '设备信息', minWidth: 220, ellipsis: true },
  { colKey: 'detail', title: '备注', minWidth: 200, ellipsis: true },
];

export const todoConfigs: CompletionTodoConfig[] = [
  { key: 'idCard', title: '完善证件号码', gain: '+15%', section: 'document', actionText: '去填写', priority: 100 },
  { key: 'idType', title: '选择证件类型', gain: '+12%', section: 'document', actionText: '去填写', priority: 95 },
  { key: 'idValidFrom', title: '补全证件有效期起', gain: '+10%', section: 'document', actionText: '去填写', priority: 90 },
  { key: 'idValidTo', title: '补全证件有效期止', gain: '+10%', section: 'document', actionText: '去填写', priority: 88 },
  { key: 'mobile', title: '补全手机号码', gain: '+8%', section: 'security', actionText: '去填写', priority: 80 },
  { key: 'email', title: '补全电子邮箱', gain: '+8%', section: 'security', actionText: '去填写', priority: 78 },
  { key: 'address', title: '补全联系地址', gain: '+6%', section: 'basic', actionText: '去填写', priority: 60 },
  { key: 'name', title: '补全姓名信息', gain: '+5%', section: 'basic', actionText: '去填写', priority: 55 },
  { key: 'gender', title: '补全性别信息', gain: '+4%', section: 'basic', actionText: '去填写', priority: 50 },
];

export const sectionWeights: Record<'basic' | 'document' | 'security', number> = {
  basic: 1,
  document: 3,
  security: 2,
};

export const emptyLoginLogs: LoginLogRow[] = [];
