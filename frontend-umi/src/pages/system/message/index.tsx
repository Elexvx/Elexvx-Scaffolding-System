import type { ProColumns } from '@ant-design/pro-components';
import { Tag } from 'antd';

import { PageScaffold } from '@/components/core/PageScaffold';
import { ResponsiveListTable } from '@/components/mobile/ResponsiveListTable';
import { queryMessages } from '@/services/system/core';
import type { MessageRow } from '@/types/system';

const columns: ProColumns<MessageRow>[] = [
  { title: '标题', dataIndex: 'title' },
  { title: '内容', dataIndex: 'content', ellipsis: true },
  { title: '创建时间', dataIndex: 'createdAt', valueType: 'dateTime' },
  { title: '已读', dataIndex: 'read', render: (_, row) => <Tag color={row.read ? 'green' : 'orange'}>{row.read ? '是' : '否'}</Tag> },
];

export default function MessagePage() {
  return (
    <PageScaffold title="消息管理">
      <ResponsiveListTable<MessageRow>
        rowKey="id"
        columns={columns}
        request={async (params) => {
          const result = await queryMessages({ ...params });
          return { ...result, success: true };
        }}
        cardTitle={(item) => item.title}
        cardDescription={(item) => `${item.read ? '已读' : '未读'} ｜ ${item.createdAt || '-'}`}
      />
    </PageScaffold>
  );
}
