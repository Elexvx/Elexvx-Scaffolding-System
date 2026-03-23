import type { ProColumns } from '@ant-design/pro-components';
import { Space, Tag } from 'antd';

import { PageScaffold } from '@/components/core/PageScaffold';
import { PermissionButton } from '@/components/core/PermissionButton';
import { ResponsiveListTable } from '@/components/mobile/ResponsiveListTable';
import { queryRoles } from '@/services/system/core';
import type { RoleRow } from '@/types/system';

const columns: ProColumns<RoleRow>[] = [
  { title: '角色名称', dataIndex: 'name' },
  { title: '角色说明', dataIndex: 'description', ellipsis: true },
  { title: '权限数', dataIndex: 'permissions', render: (_, row) => row.permissions?.length ?? 0 },
  { title: '状态', dataIndex: 'status', render: (_, row) => (row.status === 1 ? <Tag color="green">启用</Tag> : <Tag>停用</Tag>) },
];

export default function RolePage() {
  return (
    <PageScaffold
      title="角色管理"
      actions={<Space><PermissionButton permission="system:SystemRole:create" type="primary">新建角色</PermissionButton></Space>}
    >
      <ResponsiveListTable<RoleRow>
        rowKey="id"
        columns={columns}
        request={async (params) => {
          const result = await queryRoles(params);
          return { data: result.data, total: result.total, success: true };
        }}
        cardTitle={(item) => item.name}
        cardDescription={(item) => `${item.description || '未填写说明'} ｜ 权限 ${item.permissions?.length ?? 0} 项`}
      />
    </PageScaffold>
  );
}
