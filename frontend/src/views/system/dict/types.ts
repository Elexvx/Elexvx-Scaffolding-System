import type { SysDict, SysDictItem } from '@/api/system/dictionary';

export interface DictFilters {
  keyword: string;
  name: string;
  status: number | null;
}

export interface DictItemFilters {
  keyword: string;
  status: number | null;
}

export interface DictFormModel {
  name: string;
  code: string;
  status: number;
  sort: number;
  remark: string;
}

export interface DictItemFormModel {
  label: string;
  value: string;
  valueType: string;
  status: number;
  sort: number;
  tagColor: string;
  province: string;
  city: string;
  district: string;
}

export type DictRow = SysDict;
export type DictItemRow = SysDictItem;
