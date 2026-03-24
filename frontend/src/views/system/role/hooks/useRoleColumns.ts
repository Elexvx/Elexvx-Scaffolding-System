import type { PrimaryTableCol } from 'tdesign-vue-next';

export const useRoleColumns = () => {
  const columns: PrimaryTableCol[] = [
    {
      colKey: 'serial-number',
      title: '序号',
      width: 80,
      fixed: 'left',
      cell: (_h, { rowIndex }) => String(rowIndex + 1),
    },
    { colKey: 'name', title: '角色名', width: 160, ellipsis: true },
    { colKey: 'description', title: '描述', width: 220, ellipsis: true },
    { colKey: 'op', title: '操作', width: 160, fixed: 'right' },
  ];
  return { columns };
};
