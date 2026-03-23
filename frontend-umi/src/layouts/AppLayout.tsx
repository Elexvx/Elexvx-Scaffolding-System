/**
 * Elexvx Admin - Layout Shell
 * 首次编写时间：2026-03-24
 * Copyright (c) Elexvx. All rights reserved.
 */
import { ProLayout } from '@ant-design/pro-components';
import { Outlet, useModel, useNavigate } from '@umijs/max';
import { Dropdown, Grid } from 'antd';
import type { MenuProps } from 'antd';

import { brandConfig } from '@/config/brand';
import type { AppMenuItem } from '@/types/menu';
import { logoutAndRedirect } from '@/utils/auth';

export default function AppLayout() {
  const screens = Grid.useBreakpoint();
  const { initialState } = useModel('@@initialState');
  const navigate = useNavigate();
  const isMobile = !screens.lg;
  const menuItems: MenuProps['items'] = [
    {
      key: 'profile',
      label: '个人中心',
      onClick: () => navigate('/account/center'),
    },
    {
      key: 'logout',
      label: '退出登录',
      onClick: () => void logoutAndRedirect(),
    },
  ];

  return (
    <ProLayout
      title={brandConfig.brandName}
      logo={false}
      layout={isMobile ? 'mix' : 'side'}
      collapsedButtonRender={isMobile ? false : undefined}
      fixSiderbar={!isMobile}
      breakpoint="lg"
      navTheme="light"
      menuDataRender={() =>
        (initialState?.menus ?? []).map((item: AppMenuItem) => ({
          name: item.name,
          path: item.path,
          children: item.children?.map((child: AppMenuItem) => ({ name: child.name, path: child.path })),
        }))
      }
      actionsRender={() => [
        <Dropdown key="user" menu={{ items: menuItems }} placement="bottomRight">
          <a>{initialState?.currentUser?.name || '用户菜单'}</a>
        </Dropdown>,
      ]}
      menuItemRender={(item, dom) => <a onClick={() => navigate(item.path || '/')}>{dom}</a>}
    >
      <Outlet />
    </ProLayout>
  );
}
