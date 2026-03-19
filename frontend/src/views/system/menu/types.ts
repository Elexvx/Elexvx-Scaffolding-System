export type MenuType = 'DIR' | 'PAGE' | 'BTN';
export type OpenType = 'internal' | 'iframe' | 'external';

export interface RoleRow {
  id: number;
  name: string;
  permissions?: string[];
}

export interface MenuNode {
  id: number;
  parentId: number | null;
  nodeType: MenuType;
  path: string;
  routeName: string;
  component?: string;
  redirect?: string;
  titleZhCn: string;
  titleEnUs?: string;
  icon?: string;
  hidden?: boolean;
  frameSrc?: string;
  frameBlank?: boolean;
  enabled?: boolean;
  orderNo?: number;
  version?: number;
  actions?: string;
  children?: MenuNode[];
}

export interface MenuFormModel {
  id: number | null;
  version: number | null;
  parentId: number | null;
  nodeType: MenuType;
  path: string;
  routeName: string;
  component: string;
  redirect: string;
  titleZhCn: string;
  titleEnUs: string;
  icon: string;
  hidden: boolean;
  frameSrc: string;
  frameBlank: boolean;
  enabled: boolean;
  orderNo: number;
  actions: string[];
  openType: OpenType;
}

export interface MenuReorderItem {
  id: number;
  parentId: number | null;
  orderNo: number;
  version?: number;
}

export interface MenuSubmitPayload {
  parentId: number | null;
  nodeType: MenuType;
  path: string;
  routeName: string;
  component?: string;
  redirect?: string;
  titleZhCn: string;
  titleEnUs?: string;
  icon?: string;
  enabled: boolean;
  hidden: boolean;
  frameSrc?: string;
  frameBlank: boolean;
  orderNo: number;
  actions: string;
}

export interface MenuComponentOption {
  label: string;
  value: string;
}
