export interface RoleRow {
  id: number;
  name: string;
  description?: string;
  menuIds?: number[];
  permissions?: string[];
}

export interface MenuNode {
  id: number;
  titleZhCn: string;
  path?: string | null;
  routeName?: string | null;
  nodeType?: string | null;
  actions?: string;
  hidden?: boolean;
  children?: MenuNode[];
}

export interface PermissionCatalogItem {
  menuId: number;
  routeName: string;
  title: string;
  actions: string[];
  permissionCodes: Record<string, string>;
}

export interface ActionMenuOption {
  label: string;
  value: number;
}
