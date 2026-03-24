import { history } from 'umi';
import { Button, Result } from 'antd';

export default function SuccessResultPage() {
  return <Result status="success" title="操作成功" subTitle="变更已生效。" extra={<Button onClick={() => history.push('/dashboard')}>回到工作台</Button>} />;
}
