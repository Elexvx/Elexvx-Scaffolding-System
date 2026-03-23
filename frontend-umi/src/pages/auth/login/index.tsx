import { history, useModel } from '@umijs/max';
import { LoginFormPage, ProFormText } from '@ant-design/pro-components';
import { LockOutlined, UserOutlined } from '@ant-design/icons';
import { message } from 'antd';

import { brandConfig } from '@/config/brand';
import { loginByPassword } from '@/services/auth/auth';
import { setRefreshToken, setToken, setTokenExpiresAt } from '@/utils/token';

export default function LoginPage() {
  const { refresh } = useModel('@@initialState');

  return (
    <LoginFormPage
      title={brandConfig.brandName}
      subTitle={brandConfig.productName}
      submitter={{ searchConfig: { submitText: '登录' } }}
      onFinish={async (values) => {
        const result = await loginByPassword({
          account: values.account,
          password: values.password,
        });
        setToken(result.token || result.accessToken || '');
        if (result.refreshToken) {
          setRefreshToken(result.refreshToken);
        }
        if (result.expiresIn) {
          setTokenExpiresAt(Date.now() + result.expiresIn * 1000);
        }
        await refresh();
        message.success('登录成功');
        const redirect = new URLSearchParams(window.location.search).get('redirect');
        history.push(redirect || '/dashboard');
        return true;
      }}
    >
      <ProFormText
        name="account"
        fieldProps={{ size: 'large', prefix: <UserOutlined /> }}
        placeholder="请输入账号"
        rules={[{ required: true, message: '请输入账号' }]}
      />
      <ProFormText.Password
        name="password"
        fieldProps={{ size: 'large', prefix: <LockOutlined /> }}
        placeholder="请输入密码"
        rules={[{ required: true, message: '请输入密码' }]}
      />
    </LoginFormPage>
  );
}
