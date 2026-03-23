import { ProTable } from '@ant-design/pro-components';
import type { ProColumns } from '@ant-design/pro-components';
import { Space } from 'antd';

import { PageScaffold } from '@/components/core/PageScaffold';
import { PermissionButton } from '@/components/core/PermissionButton';
import { queryMenuTree } from '@/services/system/core';
import type { MenuRow } from '@/types/system';

const columns: ProColumns<MenuRow>[] = [
  { title: '菜单名称', dataIndex: 'name' },
  { title: '路径', dataIndex: 'path' },
  { title: '组件标识', dataIndex: 'component' },
  { title: '类型', dataIndex: 'type' },
];

export default function MenuPage() {
  return (
    <PageScaffold
      title="菜单管理"
      actions={
        <Space>
          <PermissionButton permission="system:menu:add" type="primary">
            新建菜单
          </PermissionButton>
        </Space>
      }
    >
      <ProTable<MenuRow>
        rowKey="id"
        columns={columns}
        search={false}
        request={async () => {
          const data = await queryMenuTree();
          return { data, success: true, total: data.length };
        }}
      />
    </PageScaffold>
  );
}
