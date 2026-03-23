/**
 * Elexvx Admin - Standard Page Scaffold
 * 首次编写时间：2026-03-24
 * Copyright (c) Elexvx. All rights reserved.
 */
import { PageContainer } from '@ant-design/pro-components';
import { Card, Space } from 'antd';
import type { PropsWithChildren, ReactNode } from 'react';

interface PageScaffoldProps extends PropsWithChildren {
  title: string;
  extra?: ReactNode;
  query?: ReactNode;
  actions?: ReactNode;
}

export function PageScaffold({ title, extra, query, actions, children }: PageScaffoldProps) {
  return (
    <PageContainer title={title} extra={extra}>
      <Space direction="vertical" style={{ width: '100%' }} size={16}>
        {query ? <Card>{query}</Card> : null}
        {actions ? <Card>{actions}</Card> : null}
        <Card>{children}</Card>
      </Space>
    </PageContainer>
  );
}
