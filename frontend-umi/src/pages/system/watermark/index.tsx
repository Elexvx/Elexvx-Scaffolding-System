import { Alert, Descriptions, Space, Switch, Typography } from 'antd';
import { useEffect, useState } from 'react';

import { PageScaffold } from '@/components/core/PageScaffold';
import { queryWatermarkSetting } from '@/services/system/core';
import type { WatermarkSetting } from '@/types/system';

export default function WatermarkPage() {
  const [setting, setSetting] = useState<WatermarkSetting>({});

  useEffect(() => {
    queryWatermarkSetting().then(setSetting).catch(() => setSetting({}));
  }, []);

  return (
    <PageScaffold title="水印配置">
      <Space direction="vertical" style={{ width: '100%', padding: 16 }} size={16}>
        <Alert showIcon type="info" message="已迁入独立水印页，后续可继续补齐编辑与预览交互。" />
        <Descriptions bordered column={1}>
          <Descriptions.Item label="启用状态">
            <Switch checked={setting.enabled === true} disabled />
          </Descriptions.Item>
          <Descriptions.Item label="水印内容">{setting.content || '-'}</Descriptions.Item>
          <Descriptions.Item label="字体大小">{String(setting.fontSize ?? '-')}</Descriptions.Item>
          <Descriptions.Item label="颜色">{setting.color || '-'}</Descriptions.Item>
          <Descriptions.Item label="透明度">{String(setting.opacity ?? '-')}</Descriptions.Item>
          <Descriptions.Item label="旋转角度">{String(setting.rotate ?? '-')}</Descriptions.Item>
          <Descriptions.Item label="水平间距">{String(setting.gapX ?? '-')}</Descriptions.Item>
          <Descriptions.Item label="垂直间距">{String(setting.gapY ?? '-')}</Descriptions.Item>
        </Descriptions>
        <Typography.Text type="secondary">当前页面先完成旧版水印正式页迁移落点，编辑提交能力后续迭代补齐。</Typography.Text>
      </Space>
    </PageScaffold>
  );
}
