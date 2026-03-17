import cloneDeep from 'lodash/cloneDeep';
import { defineStore } from 'pinia';
import type { RouteRecordRaw } from 'vue-router';

import type { RouteItem } from '@/api/model/permissionModel';
import { getMenuList } from '@/api/permission';
import router from '@/router';
import { store } from '@/store';
import type { UserInfo } from '@/types/interface';
import { transformObjectToRoute } from '@/utils/route';
import { PAGE_NOT_FOUND_ROUTE } from '@/utils/route/constant';

import { useSettingStore } from './setting';
import { getTabsRouterStore } from './tabs-router';

/**
 * 权限与动态路由 Store。
 *
 * 数据来源：
 * - 后端菜单接口 getMenuList() 返回 RouteItem 列表（通常包含 title/icon/component 等 meta 信息）
 *
 * 处理流程：
 * - buildAsyncRoutes：拉取菜单 -> transformObjectToRoute 转换为 vue-router RouteRecordRaw -> initRoutes 生成侧边菜单数据
 * - refreshAsyncRoutes：刷新菜单时先移除旧路由再注入新路由，并同步校正 tabs 与默认首页
 *
 * 注意：真正的路由注入发生在 src/permission.ts（router.addRoute），这里负责构建与维护可展示的 routers 数据。
 */
const normalizePath = (path?: string) => String(path || '').split(/[?#]/)[0];

const isPathInRoutes = (target: string, routes: RouteRecordRaw[]) => {
  const normalized = normalizePath(target);
  if (!normalized) return false;
  let found = false;
  const walk = (list: RouteRecordRaw[]) => {
    list.forEach((route) => {
      if (found || route.name === PAGE_NOT_FOUND_ROUTE.name) return;
      const current = normalizePath(route.path);
      if (current && current === normalized) {
        found = true;
        return;
      }
      if (route.children?.length) walk(route.children);
    });
  };
  walk(routes);
  return found;
};

const findFirstRoutePath = (routes: RouteRecordRaw[]) => {
  let fallback = '';
  const walk = (list: RouteRecordRaw[]): string => {
    for (const route of list) {
      if (route.name === PAGE_NOT_FOUND_ROUTE.name) continue;
      const current = normalizePath(route.path);
      const hasChildren = !!route.children?.length;
      if (hasChildren) {
        const child = walk(route.children || []);
        if (child) return child;
      }
      if (!hasChildren && current) return current;
      if (!fallback && current) fallback = current;
    }
    return '';
  };
  const leaf = walk(routes);
  return leaf || fallback;
};

const syncDefaultHome = (routes: RouteRecordRaw[]) => {
  const settingStore = useSettingStore();
  const target = normalizePath(settingStore.defaultHome);
  if (target && isPathInRoutes(target, routes)) return;
  const fallback = findFirstRoutePath(routes) || '/result/403';
  if (fallback && fallback !== settingStore.defaultHome) {
    settingStore.updateConfig({ defaultHome: fallback });
    getTabsRouterStore().updateHomeTab(fallback);
  }
};

export const usePermissionStore = defineStore('permission', {
  state: () => ({
    whiteListRouters: ['/login', '/register', '/forgot'],
    routers: [],
    removeRoutes: [],
    asyncRoutes: [],
  }),
  actions: {
    async initRoutes() {
      const accessedRouters = this.asyncRoutes;

      // 在菜单展示全部路由（去重，避免数据库菜单与本地静态菜单重复）
      // const merged = [...accessedRouters, ...homepageRouterList, ...fixedRouterList];
      // 完全使用后端返回的路由，不再合并本地路由文件
      const merged = [...accessedRouters];
      const byPath = new Map<string, RouteRecordRaw>();
      merged.forEach((r) => {
        const key = String(r.path || '');
        if (!key) return;
        if (!byPath.has(key)) byPath.set(key, r);
      });
      this.routers = cloneDeep(Array.from(byPath.values()));
      // 在菜单只展示动态路由和首页
      // this.routers = [...homepageRouterList, ...accessedRouters];
      // 在菜单只展示动态路由
      // this.routers = [...accessedRouters];
    },
    async buildAsyncRoutes(_userInfo?: UserInfo) {
      try {
        const menuListData = await getMenuList();
        const asyncRoutes: Array<RouteItem> = menuListData.list || [];
        this.asyncRoutes = transformObjectToRoute(asyncRoutes);
        await this.initRoutes();
        syncDefaultHome(this.asyncRoutes);
        return this.asyncRoutes;
      } catch (error) {
        console.error('Build routes error:', error);
        throw new Error("Can't build routes", error);
      }
    },
    async refreshAsyncRoutes(_userInfo?: UserInfo) {
      try {
        const menuListData = await getMenuList();
        const nextAsyncRoutes: Array<RouteItem> = menuListData.list || [];
        const nextRoutes = transformObjectToRoute(nextAsyncRoutes);

        const currentRoute = router.currentRoute.value;
        const currentRouteName = currentRoute.name;

        // 先拿到新配置，再替换/移除旧路由，避免刷新期间出现短暂无匹配
        this.asyncRoutes.forEach((item: RouteRecordRaw) => {
          if (item.name && router.hasRoute(item.name)) {
            router.removeRoute(item.name);
          }
        });

        this.asyncRoutes = nextRoutes as unknown as RouteRecordRaw[];
        await this.initRoutes();
        syncDefaultHome(this.asyncRoutes);

        this.asyncRoutes.forEach((item: RouteRecordRaw) => {
          router.addRoute(item);
        });

        const tabsRouterStore = getTabsRouterStore();
        tabsRouterStore.reconcileTabs(
          (tab) => {
            if (!tab.name || !router.hasRoute(tab.name)) return null;
            try {
              const resolved = router.resolve({ name: tab.name, query: tab.query });
              return resolved.path;
            } catch (e) {
              console.warn('Error resolving tab route:', e);
              return null;
            }
          },
          (tab) => tab.isHome || (!!tab.name && router.hasRoute(tab.name)),
        );

        // 如果页面被移动（path 变了但 name 不变），通过 name 重新定位到新 path，避免命中 404
        if (currentRouteName && router.hasRoute(currentRouteName)) {
          const resolved = router.resolve({
            name: currentRouteName,
            params: currentRoute.params,
            query: currentRoute.query,
            hash: currentRoute.hash,
          });
          if (resolved.fullPath !== currentRoute.fullPath) {
            await router.replace({
              name: currentRouteName,
              params: currentRoute.params,
              query: currentRoute.query,
              hash: currentRoute.hash,
            });
          }
        } else {
          const resolved = router.resolve(currentRoute.fullPath);
          if (resolved.name === PAGE_NOT_FOUND_ROUTE.name) {
            const { useSettingStore } = await import('./setting');
            const settingStore = useSettingStore();
            await router.replace(settingStore.defaultHome || '/user/index');
          }
        }

        return this.asyncRoutes;
      } catch (error) {
        throw new Error("Can't refresh routes", error);
      }
    },
    async restoreRoutes() {
      // 不需要在此额外调用initRoutes更新侧边导肮内容，在登录后asyncRoutes为空会调用
      this.asyncRoutes.forEach((item: RouteRecordRaw) => {
        if (item.name) {
          router.removeRoute(item.name);
        }
      });
      this.asyncRoutes = [];
    },
  },
});

export function getPermissionStore() {
  return usePermissionStore(store);
}
