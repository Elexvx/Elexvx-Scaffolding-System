import { Alert, Card, Col, Descriptions, Row } from 'antd';
import { useEffect, useState } from 'react';

import { PageScaffold } from '@/components/core/PageScaffold';
import { queryRedisMonitor } from '@/services/system/core';
import type { RedisMonitorData } from '@/types/system';

export default function RedisMonitorPage() {
  const [data, setData] = useState<RedisMonitorData>({});

  useEffect(() => {
    queryRedisMonitor().then(setData).catch(() => setData({}));
  }, []);

  return (
    <PageScaffold title="Redis 监控">
      <Alert type="info" showIcon style={{ marginBottom: 16 }} message="旧版 Redis 正式页已迁入，兼容总览指标展示。" />
      <Row gutter={[16, 16]}>
        <Col xs={24} lg={12}>
          <Card title="基础信息">
            <Descriptions column={1} size="small">
              <Descriptions.Item label="Redis 版本">{String(data.version ?? data.info?.redis_version ?? '-')}</Descriptions.Item>
              <Descriptions.Item label="内存占用">{String(data.memory ?? data.info?.used_memory_human ?? '-')}</Descriptions.Item>
              <Descriptions.Item label="键总量">{String(data.dbSize ?? data.stats?.dbSize ?? '-')}</Descriptions.Item>
            </Descriptions>
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card title="运行指标">
            <Descriptions column={1} size="small">
              <Descriptions.Item label="连接客户端">{String(data.info?.connected_clients ?? '-')}</Descriptions.Item>
              <Descriptions.Item label="命中率">{String(data.stats?.hitRate ?? '-')}</Descriptions.Item>
              <Descriptions.Item label="拒绝连接数">{String(data.info?.rejected_connections ?? '-')}</Descriptions.Item>
            </Descriptions>
          </Card>
        </Col>
      </Row>
    </PageScaffold>
  );
}
