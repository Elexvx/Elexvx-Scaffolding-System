/**
 * 路由相关常量（布局组件与 404 兜底路由）。
 *
 * - LAYOUT/BLANK_LAYOUT/...：用于动态路由转换时，根据后端 component 字段映射到前端布局组件
 * - PAGE_NOT_FOUND_ROUTE：最终兜底的 404 路由
 */
export const LAYOUT = () => import('@/layouts/index.vue');
export const BLANK_LAYOUT = () => import('@/layouts/blank.vue');
export const IFRAME = () => import('@/layouts/components/FrameBlank.vue');
export const EXCEPTION_COMPONENT = () => import('@/pages/result/500/index.vue');
export const PARENT_LAYOUT = () => import('@/layouts/ParentLayout.vue');

export const PAGE_NOT_FOUND_ROUTE = {
  path: '/:w+',
  name: '404Page',
  redirect: '/result/404',
};
