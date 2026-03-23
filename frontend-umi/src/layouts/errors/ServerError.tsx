import { Result } from 'antd';

export default function ServerError() {
  return <Result status="500" title="500" subTitle="服务器异常，请稍后再试" />;
}
