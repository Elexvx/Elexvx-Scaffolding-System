import { request } from '@/utils/request';

import type { MenuNode, PermissionCatalogItem, RoleRow } from '../types';

export const useRoleApi = () => {
  const fetchRoleList = () => request.get<RoleRow[]>({ url: '/system/role/list' });
  const fetchRoleDetail = (id: number) => request.get<RoleRow>({ url: `/system/role/${id}` });
  const fetchMenuTree = () => request.get<MenuNode[]>({ url: '/system/menu/tree' });
  const fetchPermissionCatalog = () => request.get<PermissionCatalogItem[]>({ url: '/system/permission/catalog' });
  const createRole = (data: Record<string, unknown>) => request.post({ url: '/system/role', data });
  const updateRole = (id: number, data: Record<string, unknown>) => request.put({ url: `/system/role/${id}`, data });
  const deleteRole = (id: number) => request.delete({ url: `/system/role/${id}` });

  return {
    fetchRoleList,
    fetchRoleDetail,
    fetchMenuTree,
    fetchPermissionCatalog,
    createRole,
    updateRole,
    deleteRole,
  };
};
