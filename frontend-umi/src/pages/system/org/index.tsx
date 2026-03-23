import { ProTable } from '@ant-design/pro-components';
import type { ProColumns } from '@ant-design/pro-components';
import { Space } from 'antd';

import { PageScaffold } from '@/components/core/PageScaffold';
import { PermissionButton } from '@/components/core/PermissionButton';
import { queryOrgTree } from '@/services/system/core';
import type { OrgRow } from '@/types/system';

const columns: ProColumns<OrgRow>[] = [
  { title: '组织名称', dataIndex: 'name' },
  { title: '组织类型', dataIndex: 'type' },
  { title: '负责人', dataIndex: 'leader' },
  { title: '创建时间', dataIndex: 'createTime', valueType: 'dateTime' },
];

export default function OrgPage() {
  return (
    <PageScaffold
      title="组织管理"
      actions={
        <Space>
          <PermissionButton permission="system:org:add" type="primary">
            新建组织
          </PermissionButton>
        </Space>
      }
    >
      <ProTable<OrgRow>
        rowKey="id"
        columns={columns}
        search={false}
        request={async () => {
          const data = await queryOrgTree();
          return { data, success: true, total: data.length };
        }}
      />
    </PageScaffold>
  );
}
