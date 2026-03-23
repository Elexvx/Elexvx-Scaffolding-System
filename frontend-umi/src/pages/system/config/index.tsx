import { Alert, Descriptions, Space } from 'antd';
import { useEffect, useState } from 'react';

import { PageScaffold } from '@/components/core/PageScaffold';
import { queryUiSetting } from '@/services/system/core';

export default function SystemConfigPage() {
  const [data, setData] = useState<Record<string, unknown>>({});

  useEffect(() => {
    queryUiSetting().then(setData).catch(() => setData({}));
  }, []);

  return (
    <PageScaffold title="系统配置">
      <Space direction="vertical" style={{ width: '100%', padding: 16 }} size={16}>
        <Alert type="info" showIcon message="统一承接个性化、验证、安全、存储等系统配置入口；后续可按后端权限拆分子页。" />
        <Descriptions column={1} bordered>
          <Descriptions.Item label="系统名称">{String(data?.websiteName || '-')}</Descriptions.Item>
          <Descriptions.Item label="应用版本">{String(data?.appVersion || '-')}</Descriptions.Item>
          <Descriptions.Item label="主题模式">{String(data?.mode || '-')}</Descriptions.Item>
          <Descriptions.Item label="允许多端登录">{String(data?.allowMultiDeviceLogin ?? '-')}</Descriptions.Item>
          <Descriptions.Item label="日志保留天数">{String(data?.logRetentionDays ?? '-')}</Descriptions.Item>
        </Descriptions>
      </Space>
    </PageScaffold>
  );
}
