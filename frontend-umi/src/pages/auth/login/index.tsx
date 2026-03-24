import { SafetyCertificateOutlined } from '@ant-design/icons';
import { LoginFormPage, ProFormText } from '@ant-design/pro-components';
import { LockOutlined, UserOutlined } from '@ant-design/icons';
import { useCallback, useEffect, useRef, useState } from 'react';
import { history, useModel } from '@umijs/max';
import { Alert, Button, Modal, Space, Tag, message } from 'antd';

import { DragCaptcha } from '@/components/core/DragCaptcha';
import type { DragCaptchaSuccessPayload } from '@/components/core/DragCaptcha';
import { brandConfig } from '@/config/brand';
import { getLoginCaptcha, loginByPassword } from '@/services/auth/auth';
import { notifyRequestError } from '@/utils/request';
import { clearAuthStorage, setRefreshToken, setToken, setTokenExpiresAt } from '@/utils/token';

const CAPTCHA_REFRESH_FALLBACK_MS = 55_000;
const CAPTCHA_REFRESH_LEEWAY_MS = 10_000;
const DEFAULT_DRAG_CAPTCHA_WIDTH = 310;
const DEFAULT_DRAG_CAPTCHA_HEIGHT = 155;

interface LoginFormValues {
  account?: string;
  password?: string;
  captchaCode?: string;
}

function resolveRequestStatus(error: unknown) {
  if (!error || typeof error !== 'object') {
    return undefined;
  }
  const maybeResponse = (
    error as {
      response?: { status?: number };
      info?: { response?: { status?: number } };
      status?: number;
      statusCode?: number;
      data?: { status?: number; statusCode?: number };
      message?: string;
    }
  );
  const status =
    maybeResponse.response?.status ??
    maybeResponse.info?.response?.status ??
    maybeResponse.status ??
    maybeResponse.statusCode ??
    maybeResponse.data?.status ??
    maybeResponse.data?.statusCode;
  if (typeof status === 'number') {
    return status;
  }
  if (typeof maybeResponse.message === 'string') {
    const matched = maybeResponse.message.match(/\b(4\d{2}|5\d{2})\b/);
    if (matched) {
      return Number(matched[1]);
    }
  }
  return undefined;
}

export default function LoginPage() {
  const { refresh } = useModel('@@initialState');
  const captchaRefreshTimerRef = useRef<number | null>(null);
  const [captchaEnabled, setCaptchaEnabled] = useState(false);
  const [captchaType, setCaptchaType] = useState<'image' | 'drag'>('image');
  const [captchaId, setCaptchaId] = useState('');
  const [captchaImage, setCaptchaImage] = useState('');
  const [dragCaptchaWidth, setDragCaptchaWidth] = useState(DEFAULT_DRAG_CAPTCHA_WIDTH);
  const [dragCaptchaHeight, setDragCaptchaHeight] = useState(DEFAULT_DRAG_CAPTCHA_HEIGHT);
  const [dragCaptchaToken, setDragCaptchaToken] = useState('');
  const [dragCaptchaVerification, setDragCaptchaVerification] = useState('');
  const [showDragCaptchaDialog, setShowDragCaptchaDialog] = useState(false);
  const [captchaLoading, setCaptchaLoading] = useState(true);
  const [captchaLoadFailed, setCaptchaLoadFailed] = useState(false);
  const pendingLoginValuesRef = useRef<LoginFormValues | null>(null);

  const clearCaptchaRefreshTimer = useCallback(() => {
    if (captchaRefreshTimerRef.current) {
      window.clearTimeout(captchaRefreshTimerRef.current);
      captchaRefreshTimerRef.current = null;
    }
  }, []);

  const resetDragCaptcha = useCallback(() => {
    setDragCaptchaToken('');
    setDragCaptchaVerification('');
  }, []);

  const loadCaptcha = useCallback(
    async (silent = false) => {
      clearCaptchaRefreshTimer();
      setCaptchaLoading(true);
      try {
        const result = await getLoginCaptcha();
        const nextEnabled = result.enabled !== false;
        const nextType = result.type === 'drag' ? 'drag' : 'image';
        setCaptchaEnabled(nextEnabled);
        setCaptchaType(nextType);
        setCaptchaLoadFailed(false);
        setShowDragCaptchaDialog(false);
        resetDragCaptcha();
        setCaptchaId(nextEnabled && nextType === 'image' ? result.id || '' : '');
        setCaptchaImage(nextEnabled && nextType === 'image' ? result.image || '' : '');
        setDragCaptchaWidth(result.width || DEFAULT_DRAG_CAPTCHA_WIDTH);
        setDragCaptchaHeight(result.height || DEFAULT_DRAG_CAPTCHA_HEIGHT);
        if (nextEnabled) {
          const expiresMs =
            (typeof result.expiresIn === 'number' && Number.isFinite(result.expiresIn)
              ? result.expiresIn * 1000
              : CAPTCHA_REFRESH_FALLBACK_MS) - CAPTCHA_REFRESH_LEEWAY_MS;
          captchaRefreshTimerRef.current = window.setTimeout(() => {
            captchaRefreshTimerRef.current = null;
            void loadCaptcha(true);
          }, Math.max(expiresMs, 10_000));
        }
      } catch (error) {
        if (resolveRequestStatus(error) === 404) {
          setCaptchaEnabled(false);
          setCaptchaLoadFailed(false);
          setCaptchaType('image');
          setCaptchaId('');
          setCaptchaImage('');
          setShowDragCaptchaDialog(false);
          resetDragCaptcha();
          return;
        }
        setCaptchaLoadFailed(true);
        if (!silent) {
          notifyRequestError(error);
        }
      } finally {
        setCaptchaLoading(false);
      }
    },
    [clearCaptchaRefreshTimer, resetDragCaptcha],
  );

  useEffect(() => {
    void loadCaptcha();
    return clearCaptchaRefreshTimer;
  }, [clearCaptchaRefreshTimer, loadCaptcha]);

  const imageCaptchaEnabled = captchaEnabled && captchaType === 'image';
  const dragCaptchaEnabled = captchaEnabled && captchaType === 'drag';

  const finishLogin = useCallback(
    async (values: LoginFormValues, dragPayload?: DragCaptchaSuccessPayload) => {
      try {
        const captchaCode = values.captchaCode?.trim();
        const resolvedDragCaptcha =
          dragPayload || (dragCaptchaVerification && dragCaptchaToken
            ? {
                captchaVerification: dragCaptchaVerification,
                token: dragCaptchaToken,
              }
            : undefined);
        if (imageCaptchaEnabled) {
          if (!captchaCode) {
            throw new Error('请输入验证码');
          }
          if (!captchaId) {
            await loadCaptcha(true);
            throw new Error('验证码已失效，请重新输入');
          }
        }
        if (dragCaptchaEnabled && (!resolvedDragCaptcha?.captchaVerification || !resolvedDragCaptcha.token)) {
          pendingLoginValuesRef.current = values;
          setShowDragCaptchaDialog(true);
          return false;
        }
        clearAuthStorage();
        const result = await loginByPassword({
          account: values.account,
          password: values.password,
          captchaId: imageCaptchaEnabled ? captchaId : resolvedDragCaptcha?.token,
          captchaCode: imageCaptchaEnabled ? captchaCode : resolvedDragCaptcha?.captchaVerification,
          force: true,
        });
        if (result.status && result.status !== 'ok') {
          throw new Error(
            result.status === 'pending'
              ? '当前账号需要额外确认后才能登录，frontend-umi 暂未接入该确认流程。'
              : '当前登录流程需要额外确认，frontend-umi 仅完成标准账号密码登录链路。',
          );
        }
        pendingLoginValuesRef.current = null;
        setToken(result.token || result.accessToken || '');
        if (result.refreshToken) {
          setRefreshToken(result.refreshToken);
        }
        if (result.expiresIn) {
          setTokenExpiresAt(Date.now() + result.expiresIn * 1000);
        }
        await refresh();
        clearCaptchaRefreshTimer();
        message.success('登录成功');
        const redirect = new URLSearchParams(window.location.search).get('redirect');
        history.push(redirect && redirect.startsWith('/') ? redirect : '/dashboard');
        return true;
      } catch (error) {
        pendingLoginValuesRef.current = null;
        await loadCaptcha(true);
        notifyRequestError(error);
        return false;
      }
    },
    [
      captchaId,
      clearCaptchaRefreshTimer,
      dragCaptchaEnabled,
      dragCaptchaToken,
      dragCaptchaVerification,
      imageCaptchaEnabled,
      loadCaptcha,
      refresh,
    ],
  );

  const handleDragCaptchaSuccess = useCallback(
    async (payload: DragCaptchaSuccessPayload) => {
      setDragCaptchaVerification(payload.captchaVerification);
      setDragCaptchaToken(payload.token);
      setShowDragCaptchaDialog(false);
      const pendingValues = pendingLoginValuesRef.current;
      if (pendingValues) {
        await finishLogin(pendingValues, payload);
      }
    },
    [finishLogin],
  );

  return (
    <>
      <LoginFormPage
        title={brandConfig.brandName}
        subTitle={brandConfig.productName}
        submitter={{
          searchConfig: { submitText: '登录' },
          submitButtonProps: {
            loading: captchaLoading,
          },
        }}
        initialValues={{ account: 'admin' }}
        actions={
          <Space direction="vertical" size={12} style={{ width: '100%' }}>
            {captchaLoadFailed ? (
              <Alert
                type="warning"
                showIcon
                message="验证码加载失败，可能会导致登录被后端拦截。"
                action={
                  <Button size="small" type="link" onClick={() => void loadCaptcha()}>
                    重新加载
                  </Button>
                }
              />
            ) : null}
          </Space>
        }
        onFinish={async (values: LoginFormValues) => {
          if (dragCaptchaEnabled && (!dragCaptchaVerification || !dragCaptchaToken)) {
            pendingLoginValuesRef.current = values;
            setShowDragCaptchaDialog(true);
            return false;
          }
          return finishLogin(values);
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
        {imageCaptchaEnabled ? (
          <div style={{ display: 'flex', gap: 12, alignItems: 'flex-start' }}>
            <div style={{ flex: 1 }}>
              <ProFormText
                name="captchaCode"
                fieldProps={{ size: 'large' }}
                placeholder="请输入验证码"
                rules={[{ required: true, message: '请输入验证码' }]}
              />
            </div>
            <Button
              type="default"
              onClick={() => void loadCaptcha()}
              loading={captchaLoading}
              style={{ width: 132, height: 40, padding: 0, overflow: 'hidden' }}
            >
              {captchaImage ? (
                <img
                  src={captchaImage}
                  alt="验证码"
                  style={{ width: '100%', height: '100%', objectFit: 'cover', display: 'block' }}
                />
              ) : (
                '刷新验证码'
              )}
            </Button>
          </div>
        ) : null}
        {dragCaptchaEnabled ? (
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: 8,
              width: '100%',
              padding: '0 12px',
              height: 40,
              border: '1px dashed rgba(5, 5, 5, 0.15)',
              borderRadius: 6,
              color: 'rgba(0, 0, 0, 0.45)',
            }}
          >
            <SafetyCertificateOutlined />
            <span>点击登录后完成验证</span>
            {dragCaptchaVerification ? <Tag color="success">已完成验证</Tag> : null}
          </div>
        ) : null}
      </LoginFormPage>
      <Modal
        open={showDragCaptchaDialog}
        title="完成安全验证"
        footer={null}
        destroyOnHidden
        maskClosable={false}
        onCancel={() => {
          pendingLoginValuesRef.current = null;
          setShowDragCaptchaDialog(false);
        }}
        width={dragCaptchaWidth + 70}
      >
        <DragCaptcha
          active={showDragCaptchaDialog}
          width={dragCaptchaWidth}
          height={dragCaptchaHeight}
          onRefresh={resetDragCaptcha}
          onSuccess={handleDragCaptchaSuccess}
        />
      </Modal>
    </>
  );
}
