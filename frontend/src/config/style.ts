/**
 * UI 样式默认配置（前端静态默认值）。
 *
 * 这份配置会被 setting store 作为初始状态合入；当后端提供 UI 配置时，可被覆盖。
 * 典型场景：布局模式（side/top）、主题模式（light/dark/auto）、是否启用 tabs-router 等。
 */
export default {
  showFooter: true,
  isSidebarCompact: false,
  showBreadcrumb: true,
  menuAutoCollapsed: true,
  mode: 'light',
  layout: 'side',
  splitMenu: false,
  sideMode: 'light',
  isFooterAside: false,
  isSidebarFixed: true,
  isHeaderFixed: true,
  isUseTabsRouter: true,
  showHeader: true,
  brandTheme: '#0052D9',
  footerCompany: '',
  footerIcp: '',
  appVersion: '',
};
