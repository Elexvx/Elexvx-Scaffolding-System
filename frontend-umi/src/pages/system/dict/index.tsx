import type { ProColumns } from '@ant-design/pro-components';
import { Tag } from 'antd';

import { PageScaffold } from '@/components/core/PageScaffold';
import { ResponsiveListTable } from '@/components/mobile/ResponsiveListTable';
import { queryDicts } from '@/services/system/core';
import type { DictRow } from '@/types/system';

const columns: ProColumns<DictRow>[] = [
  { title: '字典名称', dataIndex: 'dictName', render: (_, row) => row.dictName || row.name || '-' },
  { title: '字典编码', dataIndex: 'dictCode', render: (_, row) => row.dictCode || row.code || '-' },
  { title: '备注', dataIndex: 'remark', ellipsis: true },
  {
    title: '状态',
    dataIndex: 'status',
    render: (_, row) => <Tag color={Number(row.status ?? 1) === 1 ? 'green' : 'default'}>{Number(row.status ?? 1) === 1 ? '启用' : '停用'}</Tag>,
  },
  { title: '创建时间', dataIndex: 'createdAt', render: (_, row) => row.createdAt || row.createTime || '-' },
];

export default function DictPage() {
  return (
    <PageScaffold title="字典管理">
      <ResponsiveListTable<DictRow>
        rowKey="id"
        columns={columns}
        request={async (params) => {
          const result = await queryDicts(params);
          return { data: result.data, total: result.total, success: true };
        }}
        cardTitle={(item) => item.dictName || item.name || '-'}
        cardDescription={(item) => `${item.dictCode || item.code || '-'} ｜ ${item.remark || '无备注'}`}
      />
    </PageScaffold>
  );
}
