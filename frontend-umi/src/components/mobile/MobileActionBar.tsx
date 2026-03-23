import { MoreOutlined } from '@ant-design/icons';
import { Button, Dropdown, Grid } from 'antd';
import type { ReactNode } from 'react';

interface MobileActionBarProps {
  primaryAction?: ReactNode;
  moreItems?: { key: string; label: string; onClick: () => void }[];
}

export function MobileActionBar({ primaryAction, moreItems = [] }: MobileActionBarProps) {
  const screens = Grid.useBreakpoint();
  const isMobile = !screens.md;
  if (!isMobile) {
    return <>{primaryAction}</>;
  }
  return (
    <>
      {primaryAction}
      {moreItems.length > 0 ? (
        <Dropdown menu={{ items: moreItems.map((item) => ({ key: item.key, label: item.label, onClick: item.onClick })) }}>
          <Button icon={<MoreOutlined />}>更多</Button>
        </Dropdown>
      ) : null}
    </>
  );
}
