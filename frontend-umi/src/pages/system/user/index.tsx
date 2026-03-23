import type { ProColumns } from '@ant-design/pro-components';
import { Form, Input, Space, Tag } from 'antd';
import { useRef } from 'react';

import { PageScaffold } from '@/components/core/PageScaffold';
import { PermissionButton } from '@/components/core/PermissionButton';
import { FilterDrawer } from '@/components/mobile/FilterDrawer';
import { MobileActionBar } from '@/components/mobile/MobileActionBar';
import { ResponsiveListTable } from '@/components/mobile/ResponsiveListTable';
import { queryUsers } from '@/services/system/core';
import type { UserRow } from '@/types/system';

const columns: ProColumns<UserRow>[] = [
  { title: '姓名', dataIndex: 'name', render: (_, row) => row.name || row.nickName || '-' },
  { title: '账号', dataIndex: 'account', render: (_, row) => row.account || row.username || '-' },
  { title: '手机号', dataIndex: 'mobile' },
  { title: '邮箱', dataIndex: 'email' },
  {
    title: '状态',
    dataIndex: 'status',
    render: (_, row) => (row.status === 1 ? <Tag color="green">启用</Tag> : <Tag>停用</Tag>),
  },
  { title: '创建时间', dataIndex: 'createdAt', valueType: 'dateTime' },
];

export default function UserPage() {
  const keywordRef = useRef<string>('');
  return (
    <PageScaffold
      title="用户管理"
      query={
        <FilterDrawer>
          <Form layout="vertical">
            <Form.Item label="关键字">
              <Input placeholder="姓名/账号/手机号" onChange={(event) => { keywordRef.current = event.target.value; }} />
            </Form.Item>
          </Form>
        </FilterDrawer>
      }
      actions={
        <MobileActionBar
          primaryAction={<PermissionButton permission="system:SystemUser:create" type="primary">新建用户</PermissionButton>}
          moreItems={[{ key: 'export', label: '导出（预留）', onClick: () => undefined }]}
        />
      }
    >
      <ResponsiveListTable<UserRow>
        rowKey="id"
        columns={columns}
        request={async (params) => {
          const result = await queryUsers({ ...params, keyword: keywordRef.current });
          return { data: result.data, total: result.total, success: true };
        }}
        cardTitle={(item) => item.name || item.nickName || item.account || '-'}
        cardDescription={(item) => `${item.account || item.username || '-'} ｜ ${(item.orgUnitNames || []).join(' / ') || '未分配组织'}`}
      />
    </PageScaffold>
  );
}
