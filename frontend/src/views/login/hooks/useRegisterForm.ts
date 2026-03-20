import type { SubmitContext } from 'tdesign-vue-next';
import { MessagePlugin } from 'tdesign-vue-next';
import { computed, onMounted, reactive, ref } from 'vue';

import { register } from '@/api/auth';
import { useSettingStore } from '@/store';

import { INITIAL_REGISTER_FORM } from '../constants/loginOptions';
import { createRegisterPayload, createRegisterRules } from '../schema/registerSchema';
import { hasSequentialChars } from '../utils/loginGuards';
import { normalizeRegisterErrorMessage, sanitizeCaptcha, sanitizeTrim } from '../utils/loginMappers';
import { useCaptcha } from './useCaptcha';

export function useRegisterForm(emit: (event: 'register-success') => void) {
  const form = ref();
  const formData = reactive({ ...INITIAL_REGISTER_FORM });
  const submitting = ref(false);
  const showPassword = ref(false);
  const showConfirmPassword = ref(false);
  const settingStore = useSettingStore();
  const { captcha, handleDragRefresh, handleDragSuccess, loadCaptcha, openDragCaptcha } = useCaptcha('验证码加载失败');

  const accountValue = computed({ get: () => formData.account, set: (value) => (formData.account = sanitizeTrim(String(value ?? ''))) });
  const passwordValue = computed({ get: () => formData.password, set: (value) => (formData.password = sanitizeTrim(String(value ?? ''))) });
  const confirmPasswordValue = computed({ get: () => formData.confirmPassword, set: (value) => (formData.confirmPassword = sanitizeTrim(String(value ?? ''))) });
  const captchaValue = computed({ get: () => formData.captcha, set: (value) => (formData.captcha = sanitizeCaptcha(String(value ?? ''))) });

  const passwordRequirementMessage = computed(() => {
    const parts: string[] = [];
    const minLength = settingStore.passwordMinLength ?? 6;
    parts.push(`至少 ${minLength} 位`);
    if (settingStore.passwordRequireUppercase) parts.push('包含大写字母');
    if (settingStore.passwordRequireLowercase) parts.push('包含小写字母');
    if (settingStore.passwordRequireSpecial) parts.push('包含特殊字符');
    if (settingStore.passwordAllowSequential === false) parts.push('禁止连续字符');
    return parts.join('、');
  });
  const passwordHelp = computed(() =>
    passwordRequirementMessage.value ? `需符合密码规范：${passwordRequirementMessage.value}` : '',
  );

  const validatePasswordPolicy = (value: string) => {
    if (!value) return false;
    const minLength = settingStore.passwordMinLength ?? 6;
    if (value.length < minLength) return false;
    if (settingStore.passwordRequireUppercase && !/[A-Z]/.test(value)) return false;
    if (settingStore.passwordRequireLowercase && !/[a-z]/.test(value)) return false;
    if (settingStore.passwordRequireSpecial && !/[^a-z0-9]/i.test(value)) return false;
    if (settingStore.passwordAllowSequential === false && hasSequentialChars(value)) return false;
    return true;
  };

  const formRules = createRegisterRules(
    formData,
    () => captcha.captchaEnabled && captcha.captchaType === 'image',
    validatePasswordPolicy,
    () => passwordRequirementMessage.value,
  );

  onMounted(() => {
    void loadCaptcha();
  });

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
      MessagePlugin.error(ctx.firstError || '请完善注册信息');
      return;
    }
    if (!formData.checked) {
      MessagePlugin.error('注册前请同意用户协议和隐私协议');
      return;
    }
    const payload = createRegisterPayload(formData, captcha.captchaId);
    if (!payload.account) {
      MessagePlugin.error('请输入用户名');
      return;
    }
    if (captcha.captchaEnabled && captcha.captchaType === 'drag' && !formData.captcha) {
      captcha.pendingDragSubmit = true;
      openDragCaptcha();
      return;
    }
    submitting.value = true;
    try {
      await register(payload);
      MessagePlugin.success('注册成功，请使用账号登录');
      emit('register-success');
      form.value?.reset();
      Object.assign(formData, INITIAL_REGISTER_FORM);
      void loadCaptcha();
    } catch (err: any) {
      MessagePlugin.error(normalizeRegisterErrorMessage(err?.message) || '注册失败');
      void loadCaptcha();
    } finally {
      submitting.value = false;
    }
  };

  return {
    accountValue,
    captcha,
    captchaValue,
    completeDragCaptcha,
    loadCaptcha,
    confirmPasswordValue,
    form,
    formData,
    formRules,
    handleDragRefresh,
    onSubmit,
    passwordHelp,
    passwordValue,
    showConfirmPassword,
    showPassword,
    submitting,
  };
}
