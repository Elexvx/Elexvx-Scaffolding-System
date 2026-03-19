import { MoveIcon } from 'tdesign-icons-vue-next';
import type { PrimaryTableCol } from 'tdesign-vue-next';
import { h } from 'vue';

export const useOrgColumns = (): PrimaryTableCol[] => [
  {
    colKey: 'drag',
    title: '排序',
    width: 46,
    className: 't-table__drag-col',
    cell: () => h(MoveIcon, { class: 't-table__handle-draggable org-table__drag-handle' }),
  },
  { colKey: 'name', title: '机构名称', width: 220 },
  { colKey: 'typeLabel', title: '机构类型', width: 120 },
  { colKey: 'leaderNames', title: '负责人', width: 200 },
  { colKey: 'userCount', title: '人数', width: 90 },
  { colKey: 'status', title: '状态', width: 100 },
  { colKey: 'createdAt', title: '创建时间', width: 180 },
  { colKey: 'op', title: '操作', width: 180, fixed: 'right' },
];
