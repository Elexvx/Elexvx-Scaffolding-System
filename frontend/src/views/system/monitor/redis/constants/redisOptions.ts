import type { PrimaryTableCol } from 'tdesign-vue-next';

export const REFRESH_MS = 3000;

export const commandColumns: PrimaryTableCol[] = [
  { colKey: 'command', title: '命令', width: 200 },
  { colKey: 'calls', title: '调用次数', width: 200 },
  { colKey: 'usec', title: '耗时(ms)', width: 200 },
  { colKey: 'usecPerCall', title: '平均耗时(ms)', width: 200 },
];

export const keyspaceColumns: PrimaryTableCol[] = [
  { colKey: 'db', title: '数据库', width: 150 },
  { colKey: 'keys', title: '键数量', width: 150 },
  { colKey: 'expires', title: '过期键数量', width: 150 },
  { colKey: 'avgTtl', title: '平均TTL', width: 150 },
];
