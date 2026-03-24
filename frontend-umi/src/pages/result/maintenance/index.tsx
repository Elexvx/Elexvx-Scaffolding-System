import { history } from '@umijs/max';
import { Button, Result } from 'antd';

export default function MaintenanceResultPage() {
  return <Result status="info" title="系统维护中" subTitle="服务正在维护，请稍后重新访问。" extra={<Button onClick={() => history.push('/login')}>重新登录</Button>} />;
}
