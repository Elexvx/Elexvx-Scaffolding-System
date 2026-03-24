import { DownloadOutlined, FileTextOutlined, LinkOutlined } from '@ant-design/icons';
import { ProForm, ProFormText } from '@ant-design/pro-components';
import { App, Button, List, Space, Typography } from 'antd';

import { PageScaffold } from '@/components/core/PageScaffold';

const builtInDownloads = [
  { name: '系统初始化 SQL', url: '/api/database/tdesign_init.sql' },
  { name: 'OpenAPI 文档', url: '/api/v3/api-docs' },
  { name: '健康检查', url: '/api/actuator/health' },
];

const builtInDownloadUrlSet = new Set(builtInDownloads.map((item) => item.url));

const isValidDownloadUrl = (url: string) => {
  return builtInDownloadUrlSet.has(url) || url.startsWith('/api/');
};

export default function ConsoleDownloadPage() {
  const { message } = App.useApp();

  const triggerDownload = (url: string) => {
    const normalizedUrl = url.trim();

    if (!normalizedUrl) {
      message.warning('请输入下载地址');
      return;
    }

    if (!isValidDownloadUrl(normalizedUrl)) {
      message.error('无效的下载地址');
      return;
    }

    window.open(normalizedUrl, '_blank', 'noopener,noreferrer');
  };

  return (
    <PageScaffold title="下载中心">
      <Space direction="vertical" size={16} style={{ width: '100%', padding: 16 }}>
        <Typography.Paragraph type="secondary">
          提供系统常用资源下载入口，支持移动端和桌面端直接跳转下载。
        </Typography.Paragraph>
        <ProForm
          submitter={{
            searchConfig: { submitText: '下载链接' },
          }}
          onFinish={async (values) => {
            triggerDownload(String(values.url || ''));
          }}
        >
          <ProFormText
            name="url"
            label="下载 URL"
            placeholder="例如 /api/files/export/template"
            rules={[{ required: true, message: '请输入下载地址' }]}
            fieldProps={{ prefix: <LinkOutlined /> }}
          />
        </ProForm>
        <List
          size="small"
          bordered
          header="常用下载"
          dataSource={builtInDownloads}
          renderItem={(item) => (
            <List.Item
              actions={[
                <Button
                  key={item.url}
                  type="link"
                  icon={<DownloadOutlined />}
                  onClick={() => triggerDownload(item.url)}
                >
                  下载
                </Button>,
              ]}
            >
              <Space>
                <FileTextOutlined />
                <Typography.Text>{item.name}</Typography.Text>
              </Space>
            </List.Item>
          )}
        />
      </Space>
    </PageScaffold>
  );
}
