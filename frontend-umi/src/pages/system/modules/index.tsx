import type { ProColumns } from '@ant-design/pro-components';
import { Tag } from 'antd';

import { PageScaffold } from '@/components/core/PageScaffold';
import { ResponsiveListTable } from '@/components/mobile/ResponsiveListTable';
import { queryModules } from '@/services/system/core';
import type { ModuleRow } from '@/types/system';

const columns: ProColumns<ModuleRow>[] = [
  { title: '模块名称', dataIndex: 'name' },
  { title: '模块标识', dataIndex: 'moduleKey', render: (_, row) => row.moduleKey || row.key || '-' },
  { title: '版本', dataIndex: 'version' },
  {
    title: '状态',
    dataIndex: 'status',
    render: (_, row) => {
      const enabled = row.enabled ?? row.status === 'enabled';
      return <Tag color={enabled ? 'green' : 'default'}>{enabled ? '已启用' : '未启用'}</Tag>;
    },
  },
  { title: '描述', dataIndex: 'description', ellipsis: true },
];

export default function ModulesPage() {
  return (
    <PageScaffold title="模块管理">
      <ResponsiveListTable<ModuleRow>
        rowKey="moduleKey"
        columns={columns}
        request={async (params) => {
          const result = await queryModules(params);
          return { data: result.data, total: result.total, success: true };
        }}
        cardTitle={(item) => item.name || item.moduleKey || item.key || '-'}
        cardDescription={(item) => `${item.version || '-'} ｜ ${item.description || '无描述'}`}
      />
    </PageScaffold>
  );
}
