import type { ProColumns } from '@ant-design/pro-components';
import { Tag } from 'antd';

import { PageScaffold } from '@/components/core/PageScaffold';
import { ResponsiveListTable } from '@/components/mobile/ResponsiveListTable';
import { queryNotifications } from '@/services/system/core';
import type { NotificationRow } from '@/types/system';

const columns: ProColumns<NotificationRow>[] = [
  { title: '标题', dataIndex: 'title' },
  { title: '内容', dataIndex: 'content', ellipsis: true },
  { title: '发布时间', dataIndex: 'createdAt', valueType: 'dateTime' },
  {
    title: '状态',
    dataIndex: 'status',
    render: (_, row) => <Tag color={row.status === 'PUBLISHED' ? 'green' : 'default'}>{row.status || '-'}</Tag>,
  },
];

export default function NotificationPage() {
  return (
    <PageScaffold title="通知管理">
      <ResponsiveListTable<NotificationRow>
        rowKey="id"
        columns={columns}
        request={async (params) => {
          const result = await queryNotifications({ ...params });
          return { ...result, success: true };
        }}
        cardTitle={(item) => item.title}
        cardDescription={(item) => `${item.status || '-'} ｜ ${item.createdAt || '-'}`}
      />
    </PageScaffold>
  );
}
