import { history } from 'umi';
import { Button, Result } from 'antd';

export default function FailResultPage() {
  return <Result status="error" title="操作失败" subTitle="请求未成功，请检查参数后重试。" extra={<Button onClick={() => window.history.back()}>返回上一页</Button>} />;
}
