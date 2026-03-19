import type { ChangePasswordRequest } from '@/api/user';

export interface AreaOption {
  label: string;
  value: number | string;
  level?: number;
  zipCode?: string | null;
  children?: AreaOption[] | boolean;
}

export interface LoginLogRow {
  id: number;
  detail?: string;
  account?: string;
  ipAddress?: string;
  deviceInfo?: string;
  createdAt?: string;
}

export interface PageResult<T> {
  list: T[];
  total: number;
}

export interface CompletionTodoItem {
  key: string;
  title: string;
  gain: string;
  section: string;
  actionText?: string;
}

export interface CompletionTodoConfig extends CompletionTodoItem {
  priority: number;
}

export interface ProfileFormModel {
  name: string;
  nickname: string;
  gender: string;
  mobile: string;
  email: string;
  idType: string;
  idCard: string;
  idValidFrom: string;
  idValidTo: string;
  province: string;
  city: string;
  district: string;
  provinceId: number | null;
  cityId: number | null;
  districtId: number | null;
  zipCode: string;
  address: string;
  tags: string;
}

export type PasswordFormModel = ChangePasswordRequest;

export interface StatusItem {
  key: string;
  label: string;
  value: string;
  done: boolean;
}
