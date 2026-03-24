import type { defineComponent } from 'vue';
import type { RouteMeta } from 'vue-router';

/**
 * 权限/菜单模型定义（与后端菜单接口数据结构保持一致）。
 *
 * - RouteItem.component 既支持字符串（后端下发的页面路径/布局标识），也支持运行时组件加载器
 * - 该类型会被 utils/route 转换为 vue-router RouteRecordRaw，用于动态路由与菜单渲染
 */
export interface MenuListResult {
  list: Array<RouteItem>;
}

export type Component<T = any> =
  | ReturnType<typeof defineComponent>
  | (() => Promise<typeof import('*.vue')>)
  | (() => Promise<T>);

export interface RouteItem {
  path: string;
  name: string;
  component?: Component | string;
  components?: Component;
  redirect?: string;
  meta: RouteMeta;
  children?: Array<RouteItem>;
}
