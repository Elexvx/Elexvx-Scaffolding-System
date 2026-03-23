/**
 * Elexvx Admin - App Runtime
 * 首次编写时间：2026-03-24
 * Copyright (c) Elexvx. All rights reserved.
 */
import type { RequestConfig, RunTimeLayoutConfig } from '@umijs/max';
import { history } from '@umijs/max';
import { App, ConfigProvider, message } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import type { ReactNode } from 'react';

import { brandConfig } from '@/config/brand';
import { getCurrentUser } from '@/services/auth/auth';
import { adaptBackendMenu, queryBackendMenus } from '@/services/system/menu';
import type { CurrentUser } from '@/types/auth';
import type { AppMenuItem } from '@/types/menu';
import { clearAuthStorage, getToken } from '@/utils/token';

export interface InitialStateType {
  currentUser?: CurrentUser;
  permissions: string[];
  menus: AppMenuItem[];
}

let hasSessionExpiredNotified = false;

function toLayoutMenus(items: AppMenuItem[]): Record<string, unknown>[] {
  return items
    .filter((item) => !item.hidden)
    .map((item) => ({
      name: item.name,
      path: item.path,
      icon: item.icon,
      children: item.children ? toLayoutMenus(item.children) : undefined,
    }));
}

export async function getInitialState(): Promise<InitialStateType> {
  if (history.location.pathname === '/login') {
    return { permissions: [], menus: [] };
  }
  const token = getToken();
  if (!token) {
    return { permissions: [], menus: [] };
  }
  try {
    const [currentUser, backendMenus] = await Promise.all([getCurrentUser(), queryBackendMenus()]);
    return {
      currentUser,
      permissions: currentUser.permissions ?? [],
      menus: adaptBackendMenu(backendMenus ?? []),
    };
  } catch {
    clearAuthStorage();
    history.push('/login');
    return { permissions: [], menus: [] };
  }
}

export const layout: RunTimeLayoutConfig = ({ initialState }: { initialState?: InitialStateType }) => {
  return {
    title: brandConfig.productName,
    menu: {
      locale: false,
      request: async () => toLayoutMenus(initialState?.menus ?? []),
    },
    onPageChange: () => {
      const { pathname } = history.location;
      if (!getToken() && pathname !== '/login') {
        history.push(`/login?redirect=${encodeURIComponent(pathname)}`);
      }
    },
    childrenRender: (children: ReactNode) => (
      <ConfigProvider locale={zhCN}>
        <App>
          {children}
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
    errorHandler: (error: unknown) => {
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
        if (!hasSessionExpiredNotified) {
          hasSessionExpiredNotified = true;
          message.warning('登录状态已失效，请重新登录');
        }
        history.push('/login');
        return response;
      }
      if (response.status === 403) {
        history.push('/403');
      }
      return response;
    },
  ],
};
