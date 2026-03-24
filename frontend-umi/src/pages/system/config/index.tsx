import { Alert, Descriptions, Segmented, Space } from 'antd';
import { useEffect, useMemo, useState } from 'react';

import { PageScaffold } from '@/components/core/PageScaffold';
import { queryUiSetting } from '@/services/system/core';

type ConfigSection = 'personalize' | 'verification' | 'security' | 'storage';

const sectionOptions: { label: string; value: ConfigSection }[] = [
  { label: '个性化', value: 'personalize' },
  { label: '验证配置', value: 'verification' },
  { label: '安全策略', value: 'security' },
  { label: '存储设置', value: 'storage' },
];

const sectionMeta: Record<ConfigSection, { title: string; description: string }> = {
  personalize: { title: '个性化配置', description: '管理品牌信息、主题样式及界面展示偏好。' },
  verification: { title: '验证配置', description: '管理短信、邮件、图形验证码与验证策略参数。' },
  security: { title: '安全配置', description: '管理密码规则、登录限制与安全审计相关开关。' },
  storage: { title: '存储配置', description: '管理文件存储、上传策略与资源访问地址。' },
};

interface SystemConfigPageProps {
  section?: ConfigSection;
}

export default function SystemConfigPage({ section = 'personalize' }: SystemConfigPageProps) {
  const [data, setData] = useState<Record<string, unknown>>({});
  const [activeSection, setActiveSection] = useState<ConfigSection>(section);

  useEffect(() => {
    setActiveSection(section);
  }, [section]);

  useEffect(() => {
    queryUiSetting().then(setData).catch(() => setData({}));
  }, []);

  const metadata = useMemo(() => sectionMeta[activeSection], [activeSection]);

  return (
    <PageScaffold title={metadata.title}>
      <Space direction="vertical" style={{ width: '100%', padding: 16 }} size={16}>
        <Segmented block options={sectionOptions} value={activeSection} onChange={(value) => setActiveSection(value as ConfigSection)} />
        <Alert type="info" showIcon message={metadata.description} />
        <Descriptions column={1} bordered>
          <Descriptions.Item label="系统名称">{String(data?.websiteName || '-')}</Descriptions.Item>
          <Descriptions.Item label="应用版本">{String(data?.appVersion || '-')}</Descriptions.Item>
          <Descriptions.Item label="主题模式">{String(data?.mode || '-')}</Descriptions.Item>
          <Descriptions.Item label="允许多端登录">{String(data?.allowMultiDeviceLogin ?? '-')}</Descriptions.Item>
          <Descriptions.Item label="日志保留天数">{String(data?.logRetentionDays ?? '-')}</Descriptions.Item>
          <Descriptions.Item label="对象存储类型">{String(data?.storageType ?? '-')}</Descriptions.Item>
        </Descriptions>
      </Space>
    </PageScaffold>
  );
}
