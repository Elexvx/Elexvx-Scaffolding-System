import type { PrimaryTableCol } from 'tdesign-vue-next';

export const sensitivePermissionCodes = {
  query: 'system:sensitive:query',
  create: 'system:sensitive:create',
  delete: 'system:sensitive:delete',
  update: 'system:sensitive:update',
};

export const sensitiveWordPaginationDefault = {
  current: 1,
  pageSize: 10,
  total: 0,
};

export const sensitiveTreeConfig = {
  childrenKey: 'children',
  treeNodeColumnIndex: 0,
  indent: 24,
  expandTreeNodeOnClick: true,
};

export const sensitiveWordColumns: PrimaryTableCol[] = [
  { colKey: 'row-select', type: 'multiple', width: 48, fixed: 'left' },
  { colKey: 'serial-number', title: '序号', width: 80, fixed: 'left' },
  { colKey: 'word', title: '敏感词', ellipsis: true },
  { colKey: 'updatedAt', title: '更新时间', width: 200, cell: 'updatedAt' },
  { colKey: 'op', title: '操作', width: 100, fixed: 'right' },
];

export const sensitivePageColumns: PrimaryTableCol[] = [
  { colKey: 'titleZhCn', title: '页面名称', ellipsis: true },
  { colKey: 'fullPath', title: '页面路径', ellipsis: true, width: 300 },
  { colKey: 'enabled', title: '启用拦截', width: 100, align: 'center', cell: 'enabled' },
];
