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
