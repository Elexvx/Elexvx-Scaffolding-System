import type { FormRule, PageInfo, PrimaryTableCol } from 'tdesign-vue-next';

export interface OrgUnitNode {
  id: number;
  parentId?: number;
  name: string;
  shortName?: string;
  type: string;
  typeLabel?: string;
  sortOrder?: number;
  status?: number;
  userCount?: number;
  phone?: string;
  email?: string;
  leaderIds?: number[];
  leaderNames?: string[];
  createdAt?: string;
  children?: OrgUnitNode[];
  disabled?: boolean;
}

export interface UserRow {
  id: number;
  name: string;
  account: string;
  orgUnitNames?: string[];
}

export interface PageResult<T> {
  list: T[];
  total: number;
}

export interface OrgSearchForm {
  keyword: string;
  status: null | number;
}

export interface OrgFormModel {
  parentId: number | undefined;
  name: string;
  shortName: string;
  type: string;
  status: number;
  leaderIds: number[];
  phone: string;
  email: string;
}

export interface OrgUserSelectFilters {
  keyword: string;
  orgKeyword: string;
  orgUnitId: number | null;
  departmentId: number | null;
}

export interface OrgDialogState {
  dialogVisible: boolean;
  leaderDialogVisible: boolean;
  memberDialogVisible: boolean;
}

export interface OrgUserSelectorState {
  rows: UserRow[];
  selection: UserRow[];
  pagination: {
    current: number;
    pageSize: number;
    total: number;
  };
}

export interface OrgUserSelectorHandlers {
  onPageChange: (pageInfo: PageInfo) => void;
  onSelectChange: (selectedKeys: Array<string | number>, context: { selectedRowData?: UserRow[] }) => void;
}

export interface OrgFormSchema {
  rules: Record<string, FormRule[]>;
}

export interface OrgTableColumnHandlers {
  onCreate: (parent?: OrgUnitNode) => void;
  onEdit: (row: OrgUnitNode) => void;
  onDelete: (row: OrgUnitNode) => void;
}

export type OrgTableColumns = PrimaryTableCol<OrgUnitNode>[];
