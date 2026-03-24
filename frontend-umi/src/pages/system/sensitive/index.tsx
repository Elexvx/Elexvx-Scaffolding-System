import type { ProColumns } from '@ant-design/pro-components';
import { Tag } from 'antd';

import { PageScaffold } from '@/components/core/PageScaffold';
import { ResponsiveListTable } from '@/components/mobile/ResponsiveListTable';
import { querySensitiveWords } from '@/services/system/core';
import type { SensitiveWordRow } from '@/types/system';

const columns: ProColumns<SensitiveWordRow>[] = [
  { title: '敏感词', dataIndex: 'word' },
  { title: '级别', dataIndex: 'level' },
  { title: '分类', dataIndex: 'category' },
  {
    title: '状态',
    dataIndex: 'enabled',
    render: (_, row) => <Tag color={row.enabled !== false ? 'green' : 'default'}>{row.enabled !== false ? '启用' : '停用'}</Tag>,
  },
  { title: '创建时间', dataIndex: 'createdAt', valueType: 'dateTime' },
];

export default function SensitivePage() {
  return (
    <PageScaffold title="敏感词管理">
      <ResponsiveListTable<SensitiveWordRow>
        rowKey="id"
        columns={columns}
        request={async (params) => {
          const result = await querySensitiveWords(params);
          return { data: result.data, total: result.total, success: true };
        }}
        cardTitle={(item) => item.word}
        cardDescription={(item) => `${item.category || '-'} ｜ ${item.level || '-'}`}
      />
    </PageScaffold>
  );
}
