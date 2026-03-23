export interface UserRow {
  id: number;
  username?: string;
  name: string;
  account?: string;
  mobile?: string;
  email?: string;
  status?: number;
  createTime?: string;
}

export interface RoleRow {
  id: number;
  name: string;
  code?: string;
  status?: number;
  createTime?: string;
}

export interface MenuRow {
  id: number;
  name: string;
  path: string;
  component?: string;
  type?: string;
  sort?: number;
}

export interface OrgRow {
  id: number;
  name: string;
  type?: string;
  leader?: string;
  createTime?: string;
}

export interface AnnouncementRow {
  id: number;
  title: string;
  status?: string;
  createdAt?: string;
  publisher?: string;
}

export interface MessageRow {
  id: number;
  title: string;
  content?: string;
  read?: boolean;
  createdAt?: string;
}

export interface NotificationRow {
  id: number;
  title: string;
  content?: string;
  status?: string;
  createdAt?: string;
}

export interface OperationLogRow {
  id: number;
  module?: string;
  operator?: string;
  action?: string;
  success?: boolean;
  createdAt?: string;
}
