import type { ProColumns } from '@ant-design/pro-components';
import { Space, Tag } from 'antd';

import { PageScaffold } from '@/components/core/PageScaffold';
import { PermissionButton } from '@/components/core/PermissionButton';
import { ResponsiveListTable } from '@/components/mobile/ResponsiveListTable';
import { queryNotifications } from '@/services/system/core';
import type { NotificationRow } from '@/types/system';

const columns: ProColumns<NotificationRow>[] = [
  { title: '标题', dataIndex: 'title' },
  { title: '摘要', dataIndex: 'summary', ellipsis: true },
  { title: '优先级', dataIndex: 'priority' },
  { title: '发布时间', dataIndex: 'publishAt', valueType: 'dateTime' },
  { title: '状态', dataIndex: 'status', render: (_, row) => <Tag color={row.status === 'published' ? 'green' : 'default'}>{row.status || '-'}</Tag> },
];

export default function NotificationPage() {
  return (
    <PageScaffold title="通知管理" actions={<Space><PermissionButton permission="system:NotificationTable:create" type="primary">新建通知</PermissionButton></Space>}>
      <ResponsiveListTable<NotificationRow>
        rowKey="id"
        columns={columns}
        request={async (params) => {
          const result = await queryNotifications(params);
          return { data: result.data, total: result.total, success: true };
        }}
        cardTitle={(item) => item.title}
        cardDescription={(item) => `${item.priority || '-'} ｜ ${item.status || '-'}`}
      />
    </PageScaffold>
  );
}
