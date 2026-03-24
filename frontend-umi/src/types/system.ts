export interface UserRow {
  id: number;
  username?: string;
  name?: string;
  nickName?: string;
  account?: string;
  mobile?: string;
  email?: string;
  status?: number;
  createTime?: string;
  createdAt?: string;
  orgUnitNames?: string[];
}

export interface RoleRow {
  id: number;
  name: string;
  code?: string;
  description?: string;
  status?: number;
  createTime?: string;
  permissions?: string[];
}

export interface MenuRow {
  id: number;
  parentId?: number;
  name?: string;
  titleZhCn?: string;
  routeName?: string;
  path: string;
  component?: string;
  nodeType?: string;
  type?: string;
  sort?: number;
  orderNo?: number;
  actions?: string;
  children?: MenuRow[];
}

export interface OrgRow {
  id: number;
  name: string;
  type?: string;
  leaders?: string[];
  userCount?: number;
  sortOrder?: number;
  createTime?: string;
  children?: OrgRow[];
}

export interface AnnouncementRow {
  id: number;
  title: string;
  summary?: string;
  status?: string;
  priority?: string;
  createdAt?: string;
  publishAt?: string;
  createdByName?: string;
}

export interface MessageRow {
  id: string | number;
  title?: string;
  content?: string;
  type?: string;
  read?: boolean;
  status?: boolean;
  collected?: boolean;
  createdAt?: string;
  date?: string;
  quality?: string;
}

export interface NotificationRow {
  id: number;
  title: string;
  summary?: string;
  content?: string;
  status?: string;
  priority?: string;
  createdAt?: string;
  publishAt?: string;
  createdByName?: string;
}

export interface OperationLogRow {
  id: number;
  module?: string;
  operator?: string;
  action?: string;
  detail?: string;
  account?: string;
  ipAddress?: string;
  deviceInfo?: string;
  createdAt?: string;
}

export interface DictRow {
  id: number;
  name?: string;
  dictName?: string;
  code?: string;
  dictCode?: string;
  status?: number;
  remark?: string;
  createdAt?: string;
  createTime?: string;
}

export interface ModuleRow {
  id?: number;
  key?: string;
  moduleKey?: string;
  name?: string;
  version?: string;
  status?: string;
  enabled?: boolean;
  description?: string;
}

export interface SensitiveWordRow {
  id: number;
  word: string;
  level?: string;
  category?: string;
  enabled?: boolean;
  createdAt?: string;
}

export interface WatermarkSetting {
  enabled?: boolean;
  content?: string;
  fontSize?: number;
  color?: string;
  gapX?: number;
  gapY?: number;
  rotate?: number;
  opacity?: number;
}

export interface OnlineUserRow {
  id?: number | string;
  userId?: number | string;
  username?: string;
  nickname?: string;
  ip?: string;
  loginIp?: string;
  loginTime?: string;
  lastActiveAt?: string;
  device?: string;
  browser?: string;
  os?: string;
}

export interface RedisMonitorData {
  info?: Record<string, unknown>;
  stats?: Record<string, unknown>;
  dbSize?: number;
  version?: string;
  memory?: string;
}

export interface ServerMonitorData {
  host?: Record<string, unknown>;
  cpu?: Record<string, unknown>;
  memory?: Record<string, unknown>;
  disk?: unknown[];
  jvm?: Record<string, unknown>;
}
