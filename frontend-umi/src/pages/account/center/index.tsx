import { useModel } from 'umi';
import { Descriptions } from 'antd';

import { PageScaffold } from '@/components/core/PageScaffold';

export default function AccountCenterPage() {
  const { initialState } = useModel('@@initialState');
  const user = initialState?.currentUser;
  return (
    <PageScaffold title="个人中心">
      <Descriptions column={1} bordered>
        <Descriptions.Item label="姓名">{user?.name || '-'}</Descriptions.Item>
        <Descriptions.Item label="用户ID">{user?.id || '-'}</Descriptions.Item>
        <Descriptions.Item label="角色">{(user?.roles || []).join(', ') || '-'}</Descriptions.Item>
        <Descriptions.Item label="组织">{(user?.orgUnitNames || []).join(', ') || '-'}</Descriptions.Item>
      </Descriptions>
    </PageScaffold>
  );
}
