import type { ProColumns } from '@ant-design/pro-components';
import { Space, Tag } from 'antd';

import { PageScaffold } from '@/components/core/PageScaffold';
import { PermissionButton } from '@/components/core/PermissionButton';
import { ResponsiveListTable } from '@/components/mobile/ResponsiveListTable';
import { queryAnnouncements } from '@/services/system/core';
import type { AnnouncementRow } from '@/types/system';

const columns: ProColumns<AnnouncementRow>[] = [
  { title: '标题', dataIndex: 'title' },
  { title: '发布人', dataIndex: 'publisher' },
  { title: '发布时间', dataIndex: 'createdAt', valueType: 'dateTime' },
  {
    title: '状态',
    dataIndex: 'status',
    render: (_, row) => <Tag color={row.status === 'PUBLISHED' ? 'green' : 'default'}>{row.status || 'DRAFT'}</Tag>,
  },
];

export default function AnnouncementPage() {
  return (
    <PageScaffold
      title="公告管理"
      actions={
        <Space>
          <PermissionButton permission="announcement:create" type="primary">
            新建公告
          </PermissionButton>
        </Space>
      }
    >
      <ResponsiveListTable<AnnouncementRow>
        rowKey="id"
        columns={columns}
        request={async (params) => {
          const result = await queryAnnouncements({ ...params });
          return { ...result, success: true };
        }}
        cardTitle={(item) => item.title}
        cardDescription={(item) => `${item.publisher || '-'} ｜ ${item.status || 'DRAFT'}`}
      />
    </PageScaffold>
  );
}
