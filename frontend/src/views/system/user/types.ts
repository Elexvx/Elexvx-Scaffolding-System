import type { FormRule } from 'tdesign-vue-next';

export type Mode = 'create' | 'edit';

export interface RoleRow {
  id: number;
  name: string;
  description?: string;
  permissions?: string[];
}

export interface UserRow {
  id: number;
  guid: string;
  account: string;
  name: string;
  nickname?: string;
  gender?: string;
  mobile?: string;
  email?: string;
  idType?: string;
  idCard?: string;
  idValidFrom?: string;
  idValidTo?: string;
  joinDay?: string;
  team?: string;
  provinceId?: number | null;
  province?: string;
  cityId?: number | null;
  city?: string;
  districtId?: number | null;
  district?: string;
  zipCode?: string;
  address?: string;
  roles?: string[];
  orgUnitIds?: number[];
  orgUnitNames?: string[];
  departmentIds?: number[];
  departmentNames?: string[];
  status?: number;
  createdAt?: string;
}

export interface UserSearchModel {
  keyword: string;
  mobile: string;
  status: number | null;
  createdRange: string[];
}

export interface UserFormModel {
  account: string;
  name: string;
  nickname: string;
  gender: string;
  password: string;
  roles: string[];
  mobile: string;
  email: string;
  idType: string;
  idCard: string;
  idValidFrom: string;
  idValidTo: string;
  joinDay: string;
  team: string;
  provinceId: number | null;
  province: string;
  cityId: number | null;
  city: string;
  districtId: number | null;
  district: string;
  zipCode: string;
  address: string;
  orgUnitIds: number[];
  departmentIds: number[];
  status: number;
}

export interface ResetPasswordModel {
  password: string;
}

export interface PasswordPolicy {
  minLength: number;
  requireUppercase: boolean;
  requireLowercase: boolean;
  requireSpecial: boolean;
  allowSequential: boolean;
}

export interface UserSubmitPayload {
  account: string;
  name: string;
  nickname: string;
  gender: string;
  password?: string;
  roles: string[];
  mobile?: string;
  email?: string;
  idType: string;
  idCard: string;
  idValidFrom?: string;
  idValidTo?: string;
  joinDay?: string;
  team?: string;
  provinceId: number | null;
  province: string;
  cityId: number | null;
  city: string;
  districtId: number | null;
  district: string;
  zipCode: string;
  address: string;
  orgUnitIds: number[];
  departmentIds: number[];
  status: number;
}

export interface RoleOption {
  label: string;
  value: string;
}

export interface PageResult<T> {
  list: T[];
  total: number;
}

export interface OrgUnitNode {
  id: number;
  parentId?: number | null;
  name: string;
  type?: string;
  typeLabel?: string;
  disabled?: boolean;
  children?: OrgUnitNode[];
}

export interface OrgOption {
  label: string;
  value: number | string;
  children?: OrgOption[];
  disabled?: boolean;
}

export interface AreaOption {
  label: string;
  value: number | string;
  level?: number;
  zipCode?: string | null;
  children?: AreaOption[] | boolean;
}

export type UserFormRules = Record<string, FormRule[]>;
