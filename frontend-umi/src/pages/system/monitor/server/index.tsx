import { Alert, Card, Col, Descriptions, Row } from 'antd';
import { useEffect, useState } from 'react';

import { PageScaffold } from '@/components/core/PageScaffold';
import { queryServerMonitor } from '@/services/system/core';
import type { ServerMonitorData } from '@/types/system';

export default function ServerMonitorPage() {
  const [data, setData] = useState<ServerMonitorData>({});

  useEffect(() => {
    queryServerMonitor().then(setData).catch(() => setData({}));
  }, []);

  return (
    <PageScaffold title="服务器监控">
      <Alert type="info" showIcon style={{ marginBottom: 16 }} message="旧版服务器监控页已迁入，当前提供核心运行指标总览。" />
      <Row gutter={[16, 16]}>
        <Col xs={24} lg={8}>
          <Card title="主机信息">
            <Descriptions column={1} size="small">
              <Descriptions.Item label="主机名">{String(data.host?.hostname ?? '-')}</Descriptions.Item>
              <Descriptions.Item label="系统">{String(data.host?.osName ?? '-')}</Descriptions.Item>
              <Descriptions.Item label="IP">{String(data.host?.ip ?? '-')}</Descriptions.Item>
            </Descriptions>
          </Card>
        </Col>
        <Col xs={24} lg={8}>
          <Card title="CPU / 内存">
            <Descriptions column={1} size="small">
              <Descriptions.Item label="CPU 使用率">{String(data.cpu?.usage ?? '-')}</Descriptions.Item>
              <Descriptions.Item label="总内存">{String(data.memory?.total ?? '-')}</Descriptions.Item>
              <Descriptions.Item label="已用内存">{String(data.memory?.used ?? '-')}</Descriptions.Item>
            </Descriptions>
          </Card>
        </Col>
        <Col xs={24} lg={8}>
          <Card title="JVM 信息">
            <Descriptions column={1} size="small">
              <Descriptions.Item label="JVM 名称">{String(data.jvm?.name ?? '-')}</Descriptions.Item>
              <Descriptions.Item label="JVM 版本">{String(data.jvm?.version ?? '-')}</Descriptions.Item>
              <Descriptions.Item label="运行时长">{String(data.jvm?.uptime ?? '-')}</Descriptions.Item>
            </Descriptions>
          </Card>
        </Col>
      </Row>
    </PageScaffold>
  );
}
