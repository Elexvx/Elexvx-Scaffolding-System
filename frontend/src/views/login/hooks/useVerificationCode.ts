import { MessagePlugin } from 'tdesign-vue-next';

import { sendEmailCode, sendSmsCode } from '@/api/auth';
import { useCounter } from '@/hooks';
import { t } from '@/locales';

import type { LoginMethod } from '../types';

export function useVerificationCode() {
  const [countDown, handleCounter] = useCounter();

  const sendLoginCode = async (method: LoginMethod, value: string) => {
    try {
      if (method === 'phone') {
        await sendSmsCode({ phone: value });
      } else if (method === 'email') {
        await sendEmailCode({ email: value });
      }
      MessagePlugin.success(t('pages.login.verificationSent'));
      handleCounter();
    } catch (err: any) {
      MessagePlugin.error(String(err?.message || t('pages.login.sendingFailed')));
      throw err;
    }
  };

  const sendForgotCode = async (phone: string) => {
    try {
      await sendSmsCode({ phone });
      handleCounter();
      MessagePlugin.success('验证码已发送');
    } catch (err: any) {
      MessagePlugin.error(String(err?.message || '验证码发送失败'));
      throw err;
    }
  };

  return { countDown, sendLoginCode, sendForgotCode };
}
