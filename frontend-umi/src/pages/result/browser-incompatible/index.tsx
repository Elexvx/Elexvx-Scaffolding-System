import { history } from 'umi';
import { Button, Result } from 'antd';

export default function BrowserIncompatiblePage() {
  return (
    <Result
      status="warning"
      title="浏览器版本不受支持"
      subTitle="请升级浏览器或切换到 Chrome / Edge 最新版本后重试。"
      extra={<Button onClick={() => history.push('/dashboard')}>返回工作台</Button>}
    />
  );
}
