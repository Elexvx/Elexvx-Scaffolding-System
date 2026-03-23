import { StatisticCard } from '@ant-design/pro-components';
import { useModel } from '@umijs/max';
import { Card, Col, Row } from 'antd';

import { CapabilityPlaceholders } from '@/components/core/CapabilityPlaceholders';
import { PageScaffold } from '@/components/core/PageScaffold';

export default function DashboardPage() {
  const { initialState } = useModel('@@initialState');

  return (
    <PageScaffold title="工作台">
      <Row gutter={[16, 16]}>
        <Col xs={24} md={8}>
          <StatisticCard title="当前用户" statistic={{ value: initialState?.currentUser?.name || '-' }} />
        </Col>
        <Col xs={24} md={8}>
          <StatisticCard title="角色数" statistic={{ value: initialState?.currentUser?.roles?.length ?? 0 }} />
        </Col>
        <Col xs={24} md={8}>
          <StatisticCard title="权限点数" statistic={{ value: initialState?.permissions?.length ?? 0 }} />
        </Col>
      </Row>
      <Card style={{ marginTop: 16 }}>
        <CapabilityPlaceholders />
      </Card>
    </PageScaffold>
  );
}
