import { Descriptions } from 'antd';
import { useEffect, useState } from 'react';

import { PageScaffold } from '@/components/core/PageScaffold';
import { queryUiSetting } from '@/services/system/core';

export default function SystemConfigPage() {
  const [data, setData] = useState<Record<string, unknown>>();

  useEffect(() => {
    queryUiSetting().then(setData).catch(() => undefined);
  }, []);

  return (
    <PageScaffold title="系统配置">
      <Descriptions column={1} bordered>
        <Descriptions.Item label="系统名称">{String(data?.systemName || '-')}</Descriptions.Item>
        <Descriptions.Item label="主题模式">{String(data?.themeMode || '-')}</Descriptions.Item>
        <Descriptions.Item label="登录页标题">{String(data?.loginTitle || '-')}</Descriptions.Item>
      </Descriptions>
    </PageScaffold>
  );
}
