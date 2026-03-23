import { history } from 'umi';
import { Button, Result } from 'antd';

export default function NotFound() {
  return (
    <Result
      status="404"
      title="404"
      subTitle="页面不存在"
      extra={<Button onClick={() => history.push('/dashboard')}>返回首页</Button>}
    />
  );
}
