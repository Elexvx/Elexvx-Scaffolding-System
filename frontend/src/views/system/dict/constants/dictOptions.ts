import type { PrimaryTableCol } from 'tdesign-vue-next';

export const statusOptions = [
  { label: '正常', value: 1 },
  { label: '停用', value: 0 },
];

export const dictColumns: PrimaryTableCol[] = [
  { colKey: 'serial', title: '序号', width: 80 },
  { colKey: 'name', title: '字典名称', minWidth: 180 },
  { colKey: 'code', title: '字典编码', minWidth: 200 },
  { colKey: 'status', title: '状态', width: 120 },
  { colKey: 'op', title: '操作', width: 220, fixed: 'right' },
];

export const valueTypeOptions = [
  { label: '字符串', value: 'string' },
  { label: '数字', value: 'number' },
  { label: '布尔', value: 'boolean' },
];

export const valueTypeLabelMap: Record<string, string> = {
  string: '字符串',
  number: '数字',
  boolean: '布尔',
};

export const tagColorOptions = [
  { label: 'success', value: 'success' },
  { label: 'primary', value: 'primary' },
  { label: 'warning', value: 'warning' },
  { label: 'danger', value: 'danger' },
  { label: 'default', value: 'default' },
];
