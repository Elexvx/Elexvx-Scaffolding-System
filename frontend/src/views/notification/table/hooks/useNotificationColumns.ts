import type { PrimaryTableCol } from 'tdesign-vue-next';

export const useNotificationColumns = () => {
  const columns: PrimaryTableCol[] = [
    { colKey: 'title', title: '标题', width: 260 },
    { colKey: 'priority', title: '优先级', cell: 'priority', width: 120 },
    { colKey: 'status', title: '状态', cell: 'status', width: 140 },
    { colKey: 'publishAt', title: '发布时间', width: 180 },
    { colKey: 'updatedAt', title: '最近更新', width: 180 },
    { colKey: 'op', title: '操作', cell: 'op', width: 240 },
  ];
  return { columns };
};
