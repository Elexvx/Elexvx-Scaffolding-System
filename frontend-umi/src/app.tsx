/**
 * Elexvx Admin - App Runtime
 * 首次编写时间：2026-03-24
 * Copyright (c) Elexvx. All rights reserved.
 */
import type { RequestConfig, RunTimeLayoutConfig } from '@umijs/max';
import { history } from '@umijs/max';
import { App, ConfigProvider, Skeleton, message, Result } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import type { ReactNode } from 'react';

import { brandConfig } from '@/config/brand';
import { getCurrentUser } from '@/services/auth/auth';
import { adaptBackendMenu, queryBackendMenus } from '@/services/system/menu';
import type { CurrentUser } from '@/types/auth';
import type { AppMenuItem } from '@/types/menu';
import { clearAuthStorage, getToken, isTokenExpired } from '@/utils/token';

export interface InitialStateType {
  currentUser?: CurrentUser;
  permissions: string[];
  menus: AppMenuItem[];
}

let hasSessionExpiredNotified = false;
const STATIC_ROUTES = new Set(['/login', '/403', '/404', '/500']);

function flattenMenuTree(items: AppMenuItem[]): AppMenuItem[] {
  return items.flatMap((item) => [item, ...(item.children ? flattenMenuTree(item.children) : [])]);
}

function toLayoutMenus(items: AppMenuItem[]): Record<string, unknown>[] {
  return items
    .filter((item) => !item.hidden)
    .map((item) => ({
      name: item.name,
      path: item.path,
      icon: item.icon,
      routes: item.children ? toLayoutMenus(item.children) : undefined,
    }));
}

function ensureValidSession() {
  const token = getToken();
  if (!token || isTokenExpired()) {
    clearAuthStorage();
    return '';
  }
  return token;
}

export async function getInitialState(): Promise<InitialStateType> {
  if (STATIC_ROUTES.has(history.location.pathname)) {
    return { permissions: [], menus: [] };
  }
  const token = ensureValidSession();
  if (!token) {
    return { permissions: [], menus: [] };
  }
  try {
    const [currentUser, backendMenus] = await Promise.all([getCurrentUser(), queryBackendMenus()]);
    const menus = adaptBackendMenu(backendMenus ?? []);
    return {
      currentUser,
      permissions: currentUser.permissions ?? [],
      menus,
    };
  } catch {
    clearAuthStorage();
    return { permissions: [], menus: [] };
  }
}

export const layout: RunTimeLayoutConfig = ({ initialState, loading }: { initialState?: InitialStateType; loading?: boolean }) => {
  const flatMenus = flattenMenuTree(initialState?.menus ?? []);
  return {
    title: brandConfig.productName,
    layout: 'mix',
    fixedHeader: true,
    fixSiderbar: true,
    splitMenus: false,
    menu: {
      locale: false,
      request: async () => toLayoutMenus(initialState?.menus ?? []),
    },
    avatarProps: {
      title: initialState?.currentUser?.name || '未登录用户',
    },
    unAccessible: <Result status="403" title="403" subTitle="当前账号暂无访问权限" />,
    onPageChange: () => {
      const { pathname } = history.location;
      const token = ensureValidSession();
      if (!token && pathname !== '/login') {
        history.push(`/login?redirect=${encodeURIComponent(pathname)}`);
        return;
      }
      if (token && pathname === '/login') {
        history.replace('/dashboard');
        return;
      }
      if (pathname !== '/dashboard' && pathname !== '/account/center' && pathname.startsWith('/') && !STATIC_ROUTES.has(pathname)) {
        const menu = flatMenus.find((item) => item.path === pathname || item.path === `${pathname}/index`);
        if (!menu && pathname !== '/') {
          // 动态路由页面交给 catch-all，未知路径直达 404。
        }
      }
    },
    childrenRender: (children: ReactNode) => (
      <ConfigProvider locale={zhCN}>
        <App>
          {loading ? <Skeleton active paragraph={{ rows: 6 }} style={{ padding: 24 }} /> : children}
          {brandConfig.showLiteCopyright ? (
            <div style={{ textAlign: 'center', color: 'rgba(0,0,0,0.45)', padding: '12px 0 20px' }}>
              {brandConfig.defaultFooterText}
            </div>
          ) : null}
        </App>
      </ConfigProvider>
    ),
  };
};

export const request: RequestConfig = {
  timeout: 10000,
  prefix: process.env.UMI_APP_API_PREFIX || '/api',
  errorConfig: {
    errorHandler: (error: any) => {
      const status = error?.response?.status;
      if (status === 401) {
        clearAuthStorage();
        if (!hasSessionExpiredNotified) {
          hasSessionExpiredNotified = true;
          message.warning('登录状态已失效，请重新登录');
          setTimeout(() => {
            hasSessionExpiredNotified = false;
          }, 1200);
        }
        history.replace('/login');
        return;
      }
      if (status === 403) {
        history.replace('/403');
        return;
      }
      if (status && status >= 500) {
        history.replace('/500');
        return;
      }
      throw error;
    },
  },
  requestInterceptors: [
    (config: any) => {
      const token = getToken();
      const headers = {
        ...config.headers,
        ...(token ? { Authorization: token } : {}),
      };
      return { ...config, headers };
    },
  ],
  responseInterceptors: [
    (response: Response) => {
      if (response.status === 401) {
        clearAuthStorage();
      }
      return response;
    },
  ],
};
