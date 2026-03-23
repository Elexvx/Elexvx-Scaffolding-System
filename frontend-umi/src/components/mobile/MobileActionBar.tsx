import { MoreOutlined } from '@ant-design/icons';
import { Button, Dropdown, Grid, Space } from 'antd';
import type { ReactNode } from 'react';

interface MobileActionBarProps {
  primaryAction?: ReactNode;
  secondaryActions?: ReactNode[];
  moreItems?: { key: string; label: string; onClick: () => void }[];
}

export function MobileActionBar({ primaryAction, secondaryActions = [], moreItems = [] }: MobileActionBarProps) {
  const screens = Grid.useBreakpoint();
  const isMobile = !screens.md;
  if (!isMobile) {
    return (
      <Space wrap>
        {primaryAction}
        {secondaryActions}
      </Space>
    );
  }
  return (
    <Space wrap>
      {primaryAction}
      {moreItems.length > 0 ? (
        <Dropdown menu={{ items: moreItems.map((item) => ({ key: item.key, label: item.label, onClick: item.onClick })) }}>
          <Button icon={<MoreOutlined />}>更多</Button>
        </Dropdown>
      ) : null}
    </Space>
  );
}
