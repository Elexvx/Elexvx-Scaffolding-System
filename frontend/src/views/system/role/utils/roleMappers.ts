import type { MenuNode, PermissionCatalogItem } from '../types';

export const normalizeActions = (actions?: string[]) => {
  if (!actions?.length) return [] as string[];
  const set = new Set<string>();
  actions.forEach((action) => {
    const value = String(action || '').trim();
    if (value) set.add(value);
  });
  return Array.from(set);
};

export const getAllMenuIds = (nodes: MenuNode[]): number[] => {
  let ids: number[] = [];
  nodes.forEach((n) => {
    ids.push(n.id);
    if (n.children?.length) {
      ids = ids.concat(getAllMenuIds(n.children));
    }
  });
  return ids;
};

export const filterHiddenMenus = (nodes: MenuNode[]): MenuNode[] => {
  return nodes
    .filter((node) => !node.hidden)
    .map((node) => ({
      ...node,
      children: node.children ? filterHiddenMenus(node.children) : [],
    }));
};

export const toActionMapFromPermissions = (
  permissions: string[],
  menuIds: number[],
  permissionCatalog: PermissionCatalogItem[],
) => {
  const selectedMenuSet = new Set(menuIds.map((id) => String(id)));
  const permissionSet = new Set((permissions || []).map((item) => String(item || '').trim()).filter(Boolean));
  const map: Record<string, string[]> = {};
  permissionCatalog.forEach((catalog) => {
    const key = String(catalog.menuId);
    if (!selectedMenuSet.has(key)) return;
    const actions = catalog.actions.filter((action) => {
      const code = catalog.permissionCodes[action];
      return !!code && permissionSet.has(code);
    });
    if (actions.length > 0) {
      map[key] = actions;
    }
  });
  return map;
};

export const toActionMapForAdmin = (menuIds: number[], permissionCatalog: PermissionCatalogItem[]) => {
  const selectedMenuSet = new Set(menuIds.map((id) => String(id)));
  const map: Record<string, string[]> = {};
  permissionCatalog.forEach((catalog) => {
    const key = String(catalog.menuId);
    if (!selectedMenuSet.has(key)) return;
    if (catalog.actions.length > 0) {
      map[key] = [...catalog.actions];
    }
  });
  return map;
};

export const buildSubmitMenuActionMap = (
  selectedMenuIds: number[],
  menuActionMap: Record<string, string[]>,
  catalogByMenuId: Map<number, PermissionCatalogItem>,
) => {
  const selectedMenuSet = new Set(selectedMenuIds.map((id) => String(id)));
  const map: Record<number, string[]> = {};
  Object.entries(menuActionMap).forEach(([menuId, actions]) => {
    if (!selectedMenuSet.has(menuId)) return;
    const menuIdValue = Number(menuId);
    if (!Number.isFinite(menuIdValue)) return;
    const catalog = catalogByMenuId.get(menuIdValue);
    if (!catalog) return;
    const validActions = new Set(catalog.actions);
    const selected = normalizeActions(actions).filter((action) => validActions.has(action));
    if (selected.length > 0) {
      map[menuIdValue] = selected;
    }
  });
  return map;
};
