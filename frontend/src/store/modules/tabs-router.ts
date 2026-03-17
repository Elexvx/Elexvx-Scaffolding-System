import { defineStore } from 'pinia';

import { store } from '@/store';
import type { TRouterInfo, TTabRouterType } from '@/types/interface';

export const normalizeTabPath = (path: string) => String(path || '').split(/[?#]/)[0];

type TabsRouterState = TTabRouterType & { isSyncing: boolean; isLocked: boolean };

const state: TabsRouterState = {
  tabRouterList: [
    {
      path: '/user/index',
      routeIdx: 0,
      title: '个人中心',
      name: 'UserIndex',
      isHome: true,
    },
  ],
  isRefreshing: false,
  isSyncing: false,
  isLocked: false,
};

// 不需要做多标签tabs页缓存的列表 值为每个页面对应的name 如 DashboardDetail
// const ignoreCacheRoutes = ['DashboardDetail'];
const ignoreCacheRoutes = ['LoginPage'];

export const useTabsRouterStore = defineStore('tabsRouter', {
  state: () => state,
  getters: {
    tabRouters: (state: TTabRouterType) => state.tabRouterList,
    refreshing: (state: TTabRouterType) => state.isRefreshing,
    homeTab: (state: TTabRouterType) => state.tabRouterList.find((t) => t.isHome) as TRouterInfo,
  },
  actions: {
    setTabsLock(locked: boolean) {
      this.isLocked = locked;
    },
    beginTabRefresh(path: string) {
      const normalizedPath = normalizeTabPath(path);
      this.isRefreshing = true;
      const idx = this.tabRouterList.findIndex((tab) => normalizeTabPath(tab.path) === normalizedPath);
      if (idx === -1) return;
      const tab = this.tabRouterList[idx];
      this.tabRouterList[idx] = { ...tab, isAlive: false };
    },
    endTabRefresh(path: string) {
      const normalizedPath = normalizeTabPath(path);
      this.isRefreshing = false;
      const idx = this.tabRouterList.findIndex((tab) => normalizeTabPath(tab.path) === normalizedPath);
      if (idx === -1) return;
      const tab = this.tabRouterList[idx];
      this.tabRouterList[idx] = { ...tab, isAlive: true };
    },
    updateHomeTab(path: string, name?: string, title?: string) {
      const homeIdx = this.tabRouterList.findIndex((t) => t.isHome);
      if (homeIdx !== -1) {
        this.tabRouterList[homeIdx] = {
          ...this.tabRouterList[homeIdx],
          path,
          name: name || this.tabRouterList[homeIdx].name,
          title: title || this.tabRouterList[homeIdx].title,
        };
      }
    },
    normalizeTabRouterList() {
      const normalized: TRouterInfo[] = [];
      const seen = new Set<string>();

      this.tabRouterList.forEach((tab) => {
        const nextPath = normalizeTabPath(tab.path);
        if (!nextPath) return;
        if (seen.has(nextPath)) return;
        seen.add(nextPath);
        normalized.push(nextPath !== tab.path ? { ...tab, path: nextPath } : tab);
      });

      this.tabRouterList = normalized;
    },
    reorderTabRouters(currentIndex: number, targetIndex: number) {
      if (currentIndex === targetIndex) return;
      const list = [...this.tabRouterList];
      const [moved] = list.splice(currentIndex, 1);
      if (!moved) return;
      list.splice(targetIndex, 0, moved);
      this.tabRouterList = list;
    },
    // 刷新当前 Tab
    refreshCurrentTab() {
      this.isRefreshing = true;
      setTimeout(() => {
        this.isRefreshing = false;
      }, 200);
    },
    // 处理新增
    appendTabRouterList(newRoute: TRouterInfo) {
      // 不要将判断条件newRoute.meta.keepAlive !== false修改为newRoute.meta.keepAlive，starter默认开启保活，所以meta.keepAlive未定义时也需要进行保活，只有显式说明false才禁用保活。
      const routeName = typeof newRoute.name === 'string' ? newRoute.name : '';
      const needAlive = !!routeName && !ignoreCacheRoutes.includes(routeName) && newRoute.meta?.keepAlive !== false;
      const nextPath = normalizeTabPath(newRoute.path);
      const existingIndex = this.tabRouterList.findIndex(
        (route: TRouterInfo) => normalizeTabPath(route.path) === nextPath,
      );
      if (existingIndex === -1) {
        this.tabRouterList = this.tabRouterList.concat({ ...newRoute, path: nextPath, isAlive: needAlive });
        return;
      }

      const existing = this.tabRouterList[existingIndex];
      this.tabRouterList[existingIndex] = {
        ...existing,
        ...newRoute,
        path: nextPath,
        isAlive: needAlive,
        query: newRoute.query ?? existing?.query,
      };
    },
    // 处理关闭当前
    subtractCurrentTabRouter(newRoute: TRouterInfo) {
      const { routeIdx } = newRoute;
      this.tabRouterList = this.tabRouterList.slice(0, routeIdx).concat(this.tabRouterList.slice(routeIdx + 1));
    },
    // 处理关闭右侧
    subtractTabRouterBehind(newRoute: TRouterInfo) {
      const { routeIdx } = newRoute;
      const homeIdx: number = this.tabRouters.findIndex((route: TRouterInfo) => route.isHome);
      let tabRouterList: Array<TRouterInfo> = this.tabRouterList.slice(0, routeIdx + 1);
      if (routeIdx < homeIdx) {
        tabRouterList = tabRouterList.concat([this.homeTab]);
      }
      this.tabRouterList = tabRouterList;
    },
    // 处理关闭左侧
    subtractTabRouterAhead(newRoute: TRouterInfo) {
      const { routeIdx } = newRoute;
      const homeIdx: number = this.tabRouters.findIndex((route: TRouterInfo) => route.isHome);
      let tabRouterList: Array<TRouterInfo> = this.tabRouterList.slice(routeIdx);
      if (routeIdx > homeIdx) {
        tabRouterList = [this.homeTab].concat(tabRouterList);
      }
      this.tabRouterList = tabRouterList;
    },
    // 处理关闭其他
    subtractTabRouterOther(newRoute: TRouterInfo) {
      const { routeIdx } = newRoute;
      const homeIdx: number = this.tabRouters.findIndex((route: TRouterInfo) => route.isHome);
      this.tabRouterList =
        routeIdx === homeIdx ? [this.homeTab] : [this.homeTab].concat([this.tabRouterList?.[routeIdx]]);
    },
    removeTabRouterList() {
      this.tabRouterList = [];
    },
    resetTabRouterList(nextHome: TRouterInfo) {
      const normalizedPath = normalizeTabPath(nextHome.path);
      this.tabRouterList = [
        {
          ...nextHome,
          path: normalizedPath,
          routeIdx: 0,
          isHome: true,
        },
      ];
    },
    reconcileTabs(resolver: (tab: TRouterInfo) => string | null, isValid?: (tab: TRouterInfo) => boolean) {
      if (this.isSyncing) return;
      this.isSyncing = true;
      try {
        const updated: TRouterInfo[] = [];
        this.tabRouterList.forEach((tab) => {
          const valid = isValid ? isValid(tab) : true;
          if (!valid) return;
          const nextPath = resolver(tab);
          if (nextPath && nextPath !== tab.path) {
            updated.push({ ...tab, path: normalizeTabPath(nextPath) });
            return;
          }
          updated.push(tab);
        });
        if (updated.length === 0 && this.homeTab) {
          updated.push(this.homeTab);
        }
        const hasChanges =
          updated.length !== this.tabRouterList.length ||
          updated.some(
            (tab, idx) => tab.path !== this.tabRouterList[idx]?.path || tab.name !== this.tabRouterList[idx]?.name,
          );
        if (hasChanges) {
          this.tabRouterList = updated;
        }
      } finally {
        this.isSyncing = false;
      }
    },
    syncTabPathByRouteName(resolver: (tab: TRouterInfo) => string | null) {
      // 防止在同步过程中产生递归更新
      if (this.isSyncing) return;

      this.isSyncing = true;
      try {
        const updatedList = this.tabRouterList.map((tab) => {
          const nextPath = resolver(tab);
          if (nextPath && nextPath !== tab.path) {
            return { ...tab, path: normalizeTabPath(nextPath) };
          }
          return tab;
        });
        // 只在有实际变化时才更新
        const hasChanges = updatedList.some((tab, idx) => tab.path !== this.tabRouterList[idx].path);
        if (hasChanges) {
          this.tabRouterList = updatedList;
        }
      } finally {
        this.isSyncing = false;
      }
    },
    initTabRouterList(newRoutes: TRouterInfo[]) {
      newRoutes?.forEach((route: TRouterInfo) => this.appendTabRouterList(route));
    },
  },
  persist: {
    paths: ['tabRouterList'],
  },
});

export function getTabsRouterStore() {
  return useTabsRouterStore(store);
}
