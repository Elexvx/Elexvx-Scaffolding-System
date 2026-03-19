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

export interface PageResult<T> {
  list: T[];
  total: number;
}
