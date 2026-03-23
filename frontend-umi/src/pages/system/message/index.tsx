import type { ProColumns } from '@ant-design/pro-components';
import { Tag } from 'antd';

import { PageScaffold } from '@/components/core/PageScaffold';
import { ResponsiveListTable } from '@/components/mobile/ResponsiveListTable';
import { queryMessages } from '@/services/system/core';
import type { MessageRow } from '@/types/system';

const columns: ProColumns<MessageRow>[] = [
  { title: '消息内容', dataIndex: 'content', ellipsis: true },
  { title: '类型', dataIndex: 'type' },
  { title: '优先级', dataIndex: 'quality' },
  { title: '时间', dataIndex: 'date' },
  { title: '已读', dataIndex: 'status', render: (_, row) => <Tag color={row.status ? 'green' : 'orange'}>{row.status ? '已读' : '未读'}</Tag> },
];

export default function MessagePage() {
  return (
    <PageScaffold title="消息管理">
      <ResponsiveListTable<MessageRow>
        rowKey="id"
        columns={columns}
        request={async (params) => {
          const result = await queryMessages(params);
          return { data: result.data, total: result.total, success: true };
        }}
        cardTitle={(item) => item.title || item.content || '系统消息'}
        cardDescription={(item) => `${item.type || '-'} ｜ ${item.date || '-'}`}
      />
    </PageScaffold>
  );
}
