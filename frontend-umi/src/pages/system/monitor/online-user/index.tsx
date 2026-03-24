import type { ProColumns } from '@ant-design/pro-components';

import { PageScaffold } from '@/components/core/PageScaffold';
import { ResponsiveListTable } from '@/components/mobile/ResponsiveListTable';
import { queryOnlineUsers } from '@/services/system/core';
import type { OnlineUserRow } from '@/types/system';

const columns: ProColumns<OnlineUserRow>[] = [
  { title: '账号', dataIndex: 'username' },
  { title: '昵称', dataIndex: 'nickname' },
  { title: 'IP', dataIndex: 'ip', render: (_, row) => row.ip || row.loginIp || '-' },
  { title: '设备', dataIndex: 'device', render: (_, row) => row.device || `${row.browser || '-'} / ${row.os || '-'}` },
  { title: '登录时间', dataIndex: 'loginTime', valueType: 'dateTime' },
  { title: '最后活跃', dataIndex: 'lastActiveAt', valueType: 'dateTime' },
];

export default function OnlineUserPage() {
  return (
    <PageScaffold title="在线用户">
      <ResponsiveListTable<OnlineUserRow>
        rowKey="userId"
        columns={columns}
        request={async (params) => {
          const result = await queryOnlineUsers(params);
          return { data: result.data, total: result.total, success: true };
        }}
        cardTitle={(item) => item.nickname || item.username || '-'}
        cardDescription={(item) => `${item.ip || item.loginIp || '-'} ｜ ${item.loginTime || '-'}`}
      />
    </PageScaffold>
  );
}
