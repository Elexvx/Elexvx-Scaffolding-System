/**
 * Elexvx Admin - Standard Page Scaffold
 * 首次编写时间：2026-03-24
 * Copyright (c) Elexvx. All rights reserved.
 */
import { PageContainer } from '@ant-design/pro-components';
import { Card, Grid, Space } from 'antd';
import type { CSSProperties, PropsWithChildren, ReactNode } from 'react';

interface PageScaffoldProps extends PropsWithChildren {
  title: string;
  extra?: ReactNode;
  query?: ReactNode;
  actions?: ReactNode;
}

const stickyStyle: CSSProperties = {
  position: 'sticky',
  top: 0,
  zIndex: 2,
  background: 'var(--ant-color-bg-layout, #f5f5f5)',
};

export function QuerySection({ children }: PropsWithChildren) {
  return <Card size="small">{children}</Card>;
}

export function ActionSection({ children }: PropsWithChildren) {
  return <Card size="small">{children}</Card>;
}

export function ContentSection({ children }: PropsWithChildren) {
  return (
    <Card size="small" bodyStyle={{ padding: 0, overflow: 'hidden' }}>
      {children}
    </Card>
  );
}

export function PageScaffold({ title, extra, query, actions, children }: PageScaffoldProps) {
  const screens = Grid.useBreakpoint();
  const desktop = !!screens.lg;
  return (
    <PageContainer title={title} extra={extra} style={{ height: '100%' }}>
      <Space direction="vertical" style={{ width: '100%', height: '100%' }} size={16}>
        {(query || actions) && (
          <div style={desktop ? stickyStyle : undefined}>
            <Space direction="vertical" style={{ width: '100%' }} size={16}>
              {query ? <QuerySection>{query}</QuerySection> : null}
              {actions ? <ActionSection>{actions}</ActionSection> : null}
            </Space>
          </div>
        )}
        <div style={{ minHeight: 0, flex: 1 }}>
          <ContentSection>{children}</ContentSection>
        </div>
      </Space>
    </PageContainer>
  );
}
