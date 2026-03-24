import type { MenuListResult } from '@/api/model/permissionModel';
import { request } from '@/utils/request';

/**
 * 菜单/权限相关 API。
 *
 * - getMenuList：获取后端返回的菜单路由树（i18n 标题）
 * - 返回值会被 permission store 转换为 vue-router 动态路由（见 utils/route 与 store/modules/permission.ts）
 */
const Api = {
  MenuList: '/get-menu-list-i18n',
};

export async function getMenuList(): Promise<MenuListResult> {
  const data = await request.get<MenuListResult>({ url: Api.MenuList });
  return data || { list: [] };
}
