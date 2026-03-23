export interface BackendMenuTitleMap {
  zh_CN?: string;
  en_US?: string;
}

export interface BackendMenuMeta {
  title?: string | BackendMenuTitleMap;
  icon?: string;
  hidden?: boolean;
  orderNo?: number;
  resource?: string;
  actions?: string[];
  requiredPermissions?: string[];
}

export interface BackendMenuItem {
  id?: number | string;
  name?: string;
  path: string;
  component?: string;
  componentKey?: string;
  redirect?: string;
  meta?: BackendMenuMeta;
  children?: BackendMenuItem[];
}

export interface AppMenuItem {
  id: string;
  name: string;
  path: string;
  icon?: string;
  permCode?: string;
  permissionCodes?: string[];
  resource?: string;
  actions?: string[];
  componentKey?: string;
  routeName?: string;
  redirect?: string;
  hidden?: boolean;
  orderNo?: number;
  children?: AppMenuItem[];
}
