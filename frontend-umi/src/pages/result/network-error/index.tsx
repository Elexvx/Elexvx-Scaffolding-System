import { history } from '@umijs/max';
import { Button, Result } from 'antd';

export default function NetworkErrorResultPage() {
  return (
    <Result
      status="warning"
      title="网络异常"
      subTitle="无法连接到后端服务，请确认网络或稍后重试。"
      extra={<Button onClick={() => window.location.reload()}>刷新页面</Button>}
    />
  );
}
