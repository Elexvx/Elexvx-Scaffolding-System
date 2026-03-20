import type { FormInstanceFunctions, SubmitContext } from 'tdesign-vue-next';
import { MessagePlugin } from 'tdesign-vue-next';
import { computed, onActivated, onMounted, reactive, ref, watch } from 'vue';

import { t } from '@/locales';
import { useSettingStore, useUserStore } from '@/store';
import { setTokenExpireTimer } from '@/utils/tokenExpire';

import { CAPTCHA_REFRESH_FALLBACK_MS, LOGIN_METHOD_ORDER, REMEMBER_ACCOUNT_KEY, INITIAL_LOGIN_FORM } from '../constants/loginOptions';
import { createLoginPayload, createLoginRules } from '../schema/loginSchema';
import { ensureAgreementAccepted } from './useAuthAgreement';
import { useCaptcha } from './useCaptcha';
import { useLoginRedirect } from './useLoginRedirect';
import { useVerificationCode } from './useVerificationCode';
import { resolveAvailableLoginMethod } from '../utils/loginGuards';
import { normalizeAuthErrorMessage, sanitizeCaptcha, sanitizeNoSpace, sanitizeTrim } from '../utils/loginMappers';
import type { LoginMethod } from '../types';

export function useLoginForm() {
  const userStore = useUserStore();
  const settingStore = useSettingStore();
  const form = ref<FormInstanceFunctions>();
  const formData = reactive({ ...INITIAL_LOGIN_FORM });
  const method = ref<LoginMethod>('password');
  const showPassword = ref(false);
  const { countDown, sendLoginCode } = useVerificationCode();
  const { pushAfterLogin } = useLoginRedirect();
  const { captcha, clearCaptchaRefreshTimer, handleDragRefresh, handleDragSuccess, loadCaptcha, openDragCaptcha } =
    useCaptcha('获取验证码失败');

  const accountValue = computed({ get: () => formData.account, set: (value) => (formData.account = sanitizeTrim(String(value ?? ''))) });
  const passwordValue = computed({ get: () => formData.password, set: (value) => (formData.password = sanitizeTrim(String(value ?? ''))) });
  const phoneValue = computed({ get: () => formData.phone, set: (value) => (formData.phone = sanitizeNoSpace(String(value ?? ''))) });
  const emailValue = computed({ get: () => formData.email, set: (value) => (formData.email = sanitizeNoSpace(String(value ?? ''))) });
  const verifyCodeValue = computed({ get: () => formData.verifyCode, set: (value) => (formData.verifyCode = sanitizeNoSpace(String(value ?? ''))) });
  const captchaValue = computed({ get: () => formData.captcha, set: (value) => (formData.captcha = sanitizeCaptcha(String(value ?? ''))) });
  const formRules = createLoginRules(() => captcha.captchaEnabled && captcha.captchaType === 'image');

  const syncLoginMethod = () => {
    method.value = resolveAvailableLoginMethod(method.value, settingStore.smsEnabled, settingStore.emailEnabled);
  };
  const getDefaultLoginMethod = () => LOGIN_METHOD_ORDER.find((item) => (item === 'phone' ? settingStore.smsEnabled : item === 'email' ? settingStore.emailEnabled : true)) || 'password';

  watch(
    () => [settingStore.smsEnabled, settingStore.emailEnabled],
    () => {
      if (!method.value) method.value = getDefaultLoginMethod();
      syncLoginMethod();
    },
    { immediate: true },
  );

  watch(method, (value) => {
    if (value === 'password') void loadCaptcha();
    else clearCaptchaRefreshTimer();
  });

  onMounted(() => {
    void loadCaptcha();
    const savedAccount = localStorage.getItem(REMEMBER_ACCOUNT_KEY);
    if (savedAccount && savedAccount !== 'admin') {
      formData.account = sanitizeTrim(savedAccount);
      formData.checked = true;
    } else if (savedAccount === 'admin') {
      localStorage.removeItem(REMEMBER_ACCOUNT_KEY);
    }
  });

  onActivated(() => {
    if (method.value === 'password') void loadCaptcha();
  });

  const sendCode = async () => {
    const field = method.value === 'phone' ? 'phone' : 'email';
    const res = await form.value?.validate({ fields: [field] });
    if (res !== true) {
      MessagePlugin.error(method.value === 'phone' ? t('pages.login.invalid.phone') : t('pages.login.invalid.email'));
      return;
    }
    await sendLoginCode(method.value, method.value === 'phone' ? formData.phone : formData.email);
  };

  const completeDragCaptcha = (payload: { captchaVerification: string; token: string }) => {
    formData.captcha = payload.captchaVerification;
    handleDragSuccess(payload, () => {
      if (captcha.pendingDragSubmit) {
        captcha.pendingDragSubmit = false;
        form.value?.submit?.();
      }
    });
  };

  const onSubmit = async (ctx: SubmitContext) => {
    if (ctx.validateResult !== true) {
      MessagePlugin.error(ctx.firstError || '请完善登录信息');
      return;
    }
    if (!ensureAgreementAccepted(formData.agreed, t('pages.login.agreeTerms'))) return;

    try {
      if (method.value === 'phone') {
        const res = await userStore.loginBySms({ phone: formData.phone, code: formData.verifyCode });
        if (res?.status !== 'ok') throw new Error(t('pages.login.loginFailed'));
        if (res.token && res.expiresIn) setTokenExpireTimer(res.token, res.expiresIn);
        await settingStore.loadUiSetting();
        MessagePlugin.success(t('pages.login.loginSuccess'));
        await pushAfterLogin();
        return;
      }
      if (method.value === 'email') {
        const res = await userStore.loginByEmail({ email: formData.email, code: formData.verifyCode });
        if (res?.status !== 'ok') throw new Error(t('pages.login.loginFailed'));
        if (res.token && res.expiresIn) setTokenExpireTimer(res.token, res.expiresIn);
        await settingStore.loadUiSetting();
        MessagePlugin.success(t('pages.login.loginSuccess'));
        await pushAfterLogin();
        return;
      }
      if (captcha.captchaEnabled && captcha.captchaType === 'drag' && !formData.captcha) {
        captcha.pendingDragSubmit = true;
        openDragCaptcha();
        return;
      }
      const res = await userStore.login(createLoginPayload(formData, captcha.captchaId));
      if (res?.status !== 'ok') throw new Error(t('pages.login.loginFailed'));
      if (res.token && res.expiresIn) setTokenExpireTimer(res.token, res.expiresIn);
      await settingStore.loadUiSetting();
      MessagePlugin.success(t('pages.login.loginSuccess'));
      if (formData.checked) localStorage.setItem(REMEMBER_ACCOUNT_KEY, formData.account);
      else localStorage.removeItem(REMEMBER_ACCOUNT_KEY);
      await pushAfterLogin();
    } catch (err: any) {
      MessagePlugin.error(normalizeAuthErrorMessage(err?.message, t('pages.login.loginFailed')));
      if (method.value === 'password') void loadCaptcha();
    }
  };

  return {
    accountValue,
    captcha,
    captchaValue,
    countDown,
    emailValue,
    form,
    formData,
    formRules,
    handleDragRefresh,
    loadCaptcha,
    method,
    onSubmit,
    passwordValue,
    phoneValue,
    sendCode,
    showPassword,
    verifyCodeValue,
    completeDragCaptcha,
  };
}
