import type { PrimaryTableCol } from 'tdesign-vue-next';

export const officeExtensions = new Set(['doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx']);
export const uploadChunkSize = 5 * 1024 * 1024;
export const uploadAccept =
  '.pdf,.doc,.docx,.xls,.xlsx,.csv,.ppt,.pptx,.png,.jpg,.jpeg,.gif,.webp,.bmp';

export const downloadColumns: PrimaryTableCol[] = [
  { title: '序号', colKey: 'index', width: 90, align: 'center' },
  { title: '内容', colKey: 'content', minWidth: 220 },
  { title: '文件', colKey: 'file', minWidth: 280 },
  { title: '创建时间', colKey: 'createdAt', width: 180 },
  { title: '操作', colKey: 'actions', width: 160, align: 'center' },
];

export const downloadPermissionCodes = {
  query: 'console:download:query',
  create: 'console:download:create',
  update: 'console:download:update',
  delete: 'console:download:delete',
};
