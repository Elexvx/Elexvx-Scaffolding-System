import { Result } from 'antd';

export default function Forbidden() {
  return <Result status="403" title="403" subTitle="抱歉，您暂无权限访问该页面" />;
}
