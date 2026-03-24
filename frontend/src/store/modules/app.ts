import { defineStore } from 'pinia';

/**
 * 应用运行态 Store（与具体业务无关的全局状态）。
 *
 * - routeLoading：路由切换加载态（由 src/permission.ts 控制）
 * - backendReady：后端可用性探测结果（由 App.vue 启动探测并定时刷新）
 */
export const useAppStore = defineStore('app', {
  state: () => ({
    routeLoading: false,
    backendReady: false,
  }),
  actions: {
    setRouteLoading(loading: boolean) {
      this.routeLoading = loading;
    },
    setBackendReady(ready: boolean) {
      this.backendReady = ready;
    },
  },
});
