import loginBgUrl from '@/assets/logo/background.svg?url';
import faviconUrl from '@/assets/logo/favicon.ico';
import logoCollapsedUrl from '@/assets/logo/fold.svg?url';
import logoExpandedUrl from '@/assets/logo/logo.svg?url';

// 前端离线模式默认 UI 配置：未连接后端时用于展示站点标题、Logo、favicon 等。
const OFFLINE_UI_CONFIG = {
  websiteName: 'Elexvx 脚手架系统',
  // 版权起始年份（用于登录页、页脚等位置展示）。
  copyrightStartYear: '2024',
  // 侧边栏/头部 Logo 的自定义地址，未配置时使用内置图标。
  logoExpandedUrl,
  logoCollapsedUrl,
  // 浏览器标签页 favicon 地址，未配置则沿用默认图标。
  faviconUrl,
  // 登录页背景图地址，未配置则沿用默认图。
  loginBgUrl,
};

export default OFFLINE_UI_CONFIG;
