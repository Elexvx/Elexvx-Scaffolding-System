import type { PrimaryTableCol, TreeProps } from 'tdesign-vue-next';

export const orgTreeKeys: TreeProps['keys'] = { value: 'id', label: 'name', children: 'children' };

export const fallbackStatusOptions = [
  { label: '正常', value: 1 },
  { label: '停用', value: 0 },
];

export const fallbackTypeOptions = [
  { label: '单位', value: 'UNIT' },
  { label: '部门', value: 'DEPARTMENT' },
  { label: '科室', value: 'SECTION' },
  { label: '班组', value: 'TEAM' },
  { label: '用户', value: 'USER' },
];

export const orgTypeValues = ['UNIT', 'DEPARTMENT', 'SECTION', 'TEAM', 'USER'];

export const orgTableTreeConfig = {
  childrenKey: 'children',
  treeNodeColumnIndex: 1,
  indent: 24,
  expandTreeNodeOnClick: true,
};

export const userSelectColumns: PrimaryTableCol[] = [
  { colKey: 'row-select', type: 'multiple', width: 48, fixed: 'left' },
  { colKey: 'name', title: '用户名', width: 140 },
  { colKey: 'account', title: '账号', width: 160 },
  { colKey: 'orgUnitNames', title: '所属部门', width: 180 },
];
