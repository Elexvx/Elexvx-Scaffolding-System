import { FilterOutlined } from '@ant-design/icons';
import { Button, Drawer, Grid, Space } from 'antd';
import type { PropsWithChildren, ReactNode } from 'react';
import { useState } from 'react';

interface FilterDrawerProps extends PropsWithChildren {
  title?: string;
  extra?: ReactNode;
}

export function FilterDrawer({ title = '筛选条件', extra, children }: FilterDrawerProps) {
  const [open, setOpen] = useState(false);
  const screens = Grid.useBreakpoint();
  const isMobile = !screens.md;
  if (!isMobile) {
    return <Space direction="vertical" style={{ width: '100%' }}>{children}</Space>;
  }
  return (
    <>
      <Button icon={<FilterOutlined />} onClick={() => setOpen(true)}>
        筛选
      </Button>
      <Drawer
        open={open}
        title={title}
        onClose={() => setOpen(false)}
        placement="right"
        width="88vw"
        extra={extra}
      >
        {children}
      </Drawer>
    </>
  );
}
