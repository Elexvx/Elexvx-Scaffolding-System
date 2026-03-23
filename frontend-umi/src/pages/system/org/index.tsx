import type { ProColumns } from '@ant-design/pro-components';
import { Space } from 'antd';

import { PageScaffold } from '@/components/core/PageScaffold';
import { PermissionButton } from '@/components/core/PermissionButton';
import { ResponsiveListTable } from '@/components/mobile/ResponsiveListTable';
import { queryOrgTree } from '@/services/system/core';
import type { OrgRow } from '@/types/system';

const columns: ProColumns<OrgRow>[] = [
  { title: '组织名称', dataIndex: 'name' },
  { title: '组织类型', dataIndex: 'type' },
  { title: '负责人', dataIndex: 'leaders', render: (_, row) => row.leaders?.join('、') || '-' },
  { title: '人数', dataIndex: 'userCount' },
];

export default function OrgPage() {
  return (
    <PageScaffold title="组织管理" actions={<Space><PermissionButton permission="system:SystemOrg:create" type="primary">新建组织</PermissionButton></Space>}>
      <ResponsiveListTable<OrgRow>
        rowKey="id"
        columns={columns}
        request={async (params) => {
          const result = await queryOrgTree(params);
          return { data: result.data, total: result.total, success: true };
        }}
        cardTitle={(item) => item.name}
        cardDescription={(item) => `${item.type || '未设置类型'} ｜ 人数 ${item.userCount ?? 0}`}
      />
    </PageScaffold>
  );
}
