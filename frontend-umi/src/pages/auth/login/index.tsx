import { history, useModel } from 'umi';
import { LoginFormPage, ProFormText } from '@ant-design/pro-components';
import { LockOutlined, UserOutlined } from '@ant-design/icons';
import { Alert, message } from 'antd';

import { brandConfig } from '@/config/brand';
import { loginByPassword } from '@/services/auth/auth';
import { clearAuthStorage, setRefreshToken, setToken, setTokenExpiresAt } from '@/utils/token';

export default function LoginPage() {
  const { refresh } = useModel('@@initialState');

  return (
    <LoginFormPage
      title={brandConfig.brandName}
      subTitle={brandConfig.productName}
      submitter={{ searchConfig: { submitText: '登录' } }}
      initialValues={{ account: 'admin' }}
      actions={<Alert type="info" showIcon message="Elexvx 轻量品牌保留：仅在登录与基础框架中体现。" />}
      onFinish={async (values) => {
        clearAuthStorage();
        const result = await loginByPassword({
          account: values.account,
          password: values.password,
        });
        if (result.status && result.status !== 'ok') {
          throw new Error('当前登录流程需要额外确认，frontend-umi 仅完成标准账号密码登录链路。');
        }
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
