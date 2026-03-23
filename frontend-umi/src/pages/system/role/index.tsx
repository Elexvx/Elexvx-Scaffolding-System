import type { ProColumns } from '@ant-design/pro-components';
import { ProTable } from '@ant-design/pro-components';
import { Space, Tag } from 'antd';

import { PageScaffold } from '@/components/core/PageScaffold';
import { PermissionButton } from '@/components/core/PermissionButton';
import { queryRoles } from '@/services/system/core';
import type { RoleRow } from '@/types/system';

const columns: ProColumns<RoleRow>[] = [
  { title: '角色名称', dataIndex: 'name' },
  { title: '角色编码', dataIndex: 'code' },
  {
    title: '状态',
    dataIndex: 'status',
    render: (_, row) => (row.status === 1 ? <Tag color="green">启用</Tag> : <Tag>停用</Tag>),
  },
  { title: '创建时间', dataIndex: 'createTime', valueType: 'dateTime' },
];

export default function RolePage() {
  return (
    <PageScaffold
      title="角色管理"
      actions={
        <Space>
          <PermissionButton permission="system:role:add" type="primary">
            新建角色
          </PermissionButton>
        </Space>
      }
    >
      <ProTable<RoleRow>
        rowKey="id"
        search={false}
        columns={columns}
        request={async () => {
          const data = await queryRoles();
          return { data, success: true, total: data.length };
        }}
      />
    </PageScaffold>
  );
}
