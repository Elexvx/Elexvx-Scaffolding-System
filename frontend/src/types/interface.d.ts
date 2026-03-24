/**
 * 全局类型声明（业务模型 + 路由/标签页等前端结构体）。
 *
 * 说明：
 * - 这里的类型会被 store/router/components 等多处复用
 * - 若后端返回结构有变化，优先在这里收敛调整，避免散落各处的 any
 */
import type { TabValue } from 'tdesign-vue-next';
import type { Component, DefineComponent, FunctionalComponent } from 'vue';
import type { LocationQueryRaw, RouteMeta, RouteRecordName } from 'vue-router';

export interface MenuRoute {
  // TODO: MenuItem 组件实际支持 string，但其类型声明不兼容；先用 any 规避类型/打包错误。
  // 建议后续：收敛为 string，并在组件库升级或类型修复后移除 any。
  path: any;
  title?: string | Record<string, string>;
  name?: string;
  icon?: string | Component | FunctionalComponent | DefineComponent;
  redirect?: string;
  children: MenuRoute[];
  meta: RouteMeta;
}

export type ModeType = 'dark' | 'light';

export interface UserInfo {
  id?: number;
  guid?: string;
  name: string;
  avatar?: string;
  roles: string[];
  permissions?: string[];
  assignedRoles?: string[];
  roleSimulated?: boolean;
  orgUnitNames?: string[];
}

export interface LoginResponse {
  status: 'ok';
  token?: string;
  accessToken?: string;
  refreshToken?: string;
  expiresIn?: number;
}

export interface NotificationItem {
  id: string;
  content: string;
  type: string;
  status: boolean;
  collected: boolean;
  date: string;
  quality: string;
}

export interface TRouterInfo {
  path: string;
  query?: LocationQueryRaw;
  routeIdx?: number;
  title?: string | Record<string, string>;
  name?: RouteRecordName;
  isAlive?: boolean;
  isHome?: boolean;
  meta?: any;
}

export interface TTabRouterType {
  isRefreshing: boolean;
  tabRouterList: Array<TRouterInfo>;
}

export interface TTabRemoveOptions {
  value: TabValue;
  index: number;
  e: MouseEvent;
}
