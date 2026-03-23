import type { ProColumns } from '@ant-design/pro-components';

import { PageScaffold } from '@/components/core/PageScaffold';
import { ResponsiveListTable } from '@/components/mobile/ResponsiveListTable';
import { queryLogs } from '@/services/system/core';
import type { OperationLogRow } from '@/types/system';

const columns: ProColumns<OperationLogRow>[] = [
  { title: '模块', dataIndex: 'module' },
  { title: '操作人', dataIndex: 'operator' },
  { title: '动作', dataIndex: 'action' },
  { title: '时间', dataIndex: 'createdAt', valueType: 'dateTime' },
];

export default function LogMonitorPage() {
  return (
    <PageScaffold title="操作日志">
      <ResponsiveListTable<OperationLogRow>
        rowKey="id"
        columns={columns}
        request={async (params) => {
          const result = await queryLogs({ ...params });
          return { ...result, success: true };
        }}
        cardTitle={(item) => `${item.module || '系统日志'} #${item.id}`}
        cardDescription={(item) => `${item.operator || '-'} ｜ ${item.createdAt || '-'}`}
      />
    </PageScaffold>
  );
}
