import type { SensitivePageSetting, SensitiveWord } from '@/api/system/sensitive';

export type SensitiveTabValue = 'words' | 'pages';
export type NodeType = 'DIR' | 'PAGE' | 'BTN';

export interface SensitiveMenuNode {
  id: number;
  nodeType: NodeType;
  path: string;
  routeName: string;
  component?: string;
  titleZhCn: string;
  titleEnUs?: string;
  hidden?: boolean;
  children?: SensitiveMenuNode[];
}

export interface SensitivePageRow {
  pageKey: string;
  pageName: string;
  enabled: boolean;
}

export interface SensitivePageTreeNode {
  id: number;
  nodeType: NodeType;
  titleZhCn: string;
  titleEnUs?: string;
  path: string;
  routeName: string;
  component?: string;
  fullPath: string;
  enabled?: boolean;
  children?: SensitivePageTreeNode[];
}

export interface SensitivePagination {
  current: number;
  pageSize: number;
  total: number;
}

export interface SensitiveImportParseResult {
  message: string;
  errors: string[];
  hasErrorCode: boolean;
}

export interface SensitiveWordSearchForm {
  keyword: string;
}

export interface SensitiveWordCreateForm {
  word: string;
}

export type SensitiveWordData = SensitiveWord;
export type SensitivePageSettingData = SensitivePageSetting;
