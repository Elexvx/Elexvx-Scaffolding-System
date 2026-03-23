export interface BackendMenuMeta {
  title?: string;
  icon?: string;
  hidden?: boolean;
  orderNo?: number;
  requiredPermissions?: string[];
}

export interface BackendMenuItem {
  id?: number | string;
  name: string;
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
  componentKey?: string;
  hidden?: boolean;
  orderNo?: number;
  children?: AppMenuItem[];
}
