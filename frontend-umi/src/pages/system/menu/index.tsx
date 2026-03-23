import type { ProColumns } from '@ant-design/pro-components';
import { Space, Tag } from 'antd';

import { PageScaffold } from '@/components/core/PageScaffold';
import { PermissionButton } from '@/components/core/PermissionButton';
import { ResponsiveListTable } from '@/components/mobile/ResponsiveListTable';
import { queryMenuTree } from '@/services/system/core';
import type { MenuRow } from '@/types/system';

const columns: ProColumns<MenuRow>[] = [
  { title: '菜单名称', dataIndex: 'titleZhCn', render: (_, row) => row.titleZhCn || row.name || '-' },
  { title: '路由标识', dataIndex: 'routeName' },
  { title: '路径', dataIndex: 'path' },
  { title: '组件标识', dataIndex: 'component', render: (_, row) => row.component || 'LAYOUT' },
  { title: '类型', dataIndex: 'nodeType', render: (_, row) => <Tag>{row.nodeType || row.type || '-'}</Tag> },
];

export default function MenuPage() {
  return (
    <PageScaffold title="菜单管理" actions={<Space><PermissionButton permission="system:SystemMenu:create" type="primary">新建菜单</PermissionButton></Space>}>
      <ResponsiveListTable<MenuRow>
        rowKey="id"
        columns={columns}
        request={async (params) => {
          const result = await queryMenuTree(params);
          return { data: result.data, total: result.total, success: true };
        }}
        cardTitle={(item) => item.titleZhCn || item.name || '-'}
        cardDescription={(item) => `${item.routeName || '-'} ｜ ${item.path}`}
      />
    </PageScaffold>
  );
}
