import type { PrimaryTableCol } from 'tdesign-vue-next';
import { reactive } from 'vue';

export const createUserColumns = (): PrimaryTableCol[] => [
  { colKey: 'name', title: '用户名称', width: 140 },
  { colKey: 'orgUnitNames', title: '所属机构', width: 180, ellipsis: true },
  { colKey: 'departmentNames', title: '所属部门', width: 180, ellipsis: true },
  { colKey: 'account', title: '系统账号', width: 160, ellipsis: true },
  { colKey: 'guid', title: '系统编号', width: 260, ellipsis: true },
  { colKey: 'mobile', title: '手机号', width: 140, ellipsis: true },
  { colKey: 'status', title: '状态', width: 120 },
  { colKey: 'createdAt', title: '创建时间', width: 180 },
  { colKey: 'op', title: '操作', width: 200, fixed: 'right' },
];

export const createUserFilters = () =>
  reactive({
    keyword: '',
    mobile: '',
    status: null as null | number,
    createdRange: [] as string[],
  });

export const createUserPagination = () =>
  reactive({
    current: 1,
    pageSize: 10,
    total: 0,
  });
