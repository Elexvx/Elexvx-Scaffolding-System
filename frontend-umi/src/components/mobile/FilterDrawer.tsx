import { FilterOutlined } from '@ant-design/icons';
import { Button, Drawer, Grid } from 'antd';
import type { PropsWithChildren } from 'react';
import { useState } from 'react';

interface FilterDrawerProps extends PropsWithChildren {
  title?: string;
}

export function FilterDrawer({ title = '筛选条件', children }: FilterDrawerProps) {
  const [open, setOpen] = useState(false);
  const screens = Grid.useBreakpoint();
  const isMobile = !screens.md;
  if (!isMobile) {
    return <>{children}</>;
  }
  return (
    <>
      <Button icon={<FilterOutlined />} onClick={() => setOpen(true)}>
        筛选
      </Button>
      <Drawer open={open} title={title} onClose={() => setOpen(false)} placement="right" width="88vw">
        {children}
      </Drawer>
    </>
  );
}
