import type { MenuType } from '../types';

export const menuTreeKeys = { value: 'id', label: 'titleZhCn', children: 'children' };

export const fallbackMenuTypeOptions = [
  { label: '目录', value: 'DIR' },
  { label: '页面', value: 'PAGE' },
  { label: '按钮', value: 'BTN' },
];

export const menuTypeValues: MenuType[] = ['DIR', 'PAGE', 'BTN'];

export const menuTypeLabelMap: Record<MenuType, string> = {
  DIR: '目录',
  PAGE: '页面',
  BTN: '按钮',
};

export const fallbackActionOptions = [
  { label: '查询 (Query)', value: 'query' },
  { label: '新增 (Create)', value: 'create' },
  { label: '修改 (Update)', value: 'update' },
  { label: '删除 (Delete)', value: 'delete' },
];

export const defaultMenuActions = ['query', 'create', 'update', 'delete'];

export const actionValues = ['query', 'create', 'update', 'delete'];

