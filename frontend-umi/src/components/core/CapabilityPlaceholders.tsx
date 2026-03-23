import { Alert, Space } from 'antd';

export function CapabilityPlaceholders() {
  return (
    <Space direction="vertical" style={{ width: '100%' }}>
      <Alert
        type="info"
        showIcon
        message="富文本能力占位"
        description="已预留迁移入口，后续接入现有 backend 内容审核与存储接口。"
      />
      <Alert
        type="info"
        showIcon
        message="水印能力占位"
        description="已预留 UI 配置与页面叠加接口，后续迁移旧版 WatermarkOverlay。"
      />
      <Alert
        type="info"
        showIcon
        message="会话监听占位"
        description="已预留 401 会话失效通知逻辑，后续补齐并发登录与踢下线推送。"
      />
    </Space>
  );
}
