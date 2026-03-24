import { Navigate, useAccess, useLocation, useModel } from '@umijs/max';
import { Result } from 'antd';

import type { AppMenuItem } from '@/types/menu';

import { componentRegistry } from './componentRegistry';

function flattenMenus(items: AppMenuItem[]): AppMenuItem[] {
  const list: AppMenuItem[] = [];
  const stack = [...items];
  while (stack.length > 0) {
    const current = stack.pop()!;
    list.push(current);
    if (current.children?.length) {
      stack.push(...current.children);
    }
  }
  return list;
}

export default function DynamicPage() {
  const { pathname } = useLocation();
  const access = useAccess();
  const { initialState } = useModel('@@initialState');
  const allMenus = flattenMenus(initialState?.menus ?? []);
  const menu = allMenus.find((item) => item.path === pathname || `${item.path}/index` === pathname || item.redirect === pathname);
  if (!menu) {
    return <Navigate to="/404" />;
  }
  if (menu.permissionCodes?.length && !access.hasPermission(menu.permissionCodes)) {
    return <Navigate to="/403" />;
  }
  const candidates = [
    menu.routeName || '',
    menu.componentKey || '',
    `${pathname.replace(/^\//, '')}/index`,
    pathname.replace(/^\//, ''),
  ].filter(Boolean);
  const hitKey = candidates.find((key) => componentRegistry[key]);
  const Component = hitKey ? componentRegistry[hitKey] : undefined;
  if (!Component) {
    return <Result status="404" title="未注册页面组件" subTitle={`componentKey: ${candidates.join(' | ')}`} />;
  }
  return <Component />;
}
