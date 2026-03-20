import type { FormInstanceFunctions, SubmitContext } from 'tdesign-vue-next';
import { MessagePlugin } from 'tdesign-vue-next';
import { computed, reactive, ref } from 'vue';

import { resetPassword } from '@/api/auth';

import { INITIAL_FORGOT_PASSWORD_FORM } from '../constants/loginOptions';
import { createForgotPasswordRules } from '../schema/forgotPasswordSchema';
import { sanitizeNoSpace, sanitizeTrim } from '../utils/loginMappers';
import { useVerificationCode } from './useVerificationCode';

export function useForgotPasswordForm(emit: (event: 'reset-success') => void) {
  const form = ref<FormInstanceFunctions>();
  const formData = reactive({ ...INITIAL_FORGOT_PASSWORD_FORM });
  const submitting = ref(false);
  const { countDown, sendForgotCode } = useVerificationCode();

  const accountValue = computed({ get: () => formData.account, set: (value) => (formData.account = sanitizeTrim(String(value ?? ''))) });
  const phoneValue = computed({ get: () => formData.phone, set: (value) => (formData.phone = sanitizeNoSpace(String(value ?? ''))) });
  const verifyCodeValue = computed({ get: () => formData.verifyCode, set: (value) => (formData.verifyCode = sanitizeNoSpace(String(value ?? ''))) });
  const newPasswordValue = computed({ get: () => formData.newPassword, set: (value) => (formData.newPassword = sanitizeTrim(String(value ?? ''))) });
  const confirmPasswordValue = computed({ get: () => formData.confirmPassword, set: (value) => (formData.confirmPassword = sanitizeTrim(String(value ?? ''))) });

  const sendCode = async () => {
    const res = await form.value?.validate({ fields: ['phone'] });
    if (res !== true) {
      MessagePlugin.error('请输入正确手机号');
      return;
    }
    await sendForgotCode(formData.phone);
  };

  const onSubmit = async (ctx: SubmitContext) => {
    if (ctx.validateResult !== true) {
      MessagePlugin.error(ctx.firstError || '请完善找回信息');
      return;
    }
    if (!formData.checked) {
      MessagePlugin.error('请先同意相关条款');
      return;
    }
    submitting.value = true;
    try {
      await resetPassword({
        account: formData.account,
        phone: formData.phone,
        code: formData.verifyCode,
        newPassword: formData.newPassword,
        confirmPassword: formData.confirmPassword,
      });
      MessagePlugin.success('密码已重置，请使用新密码登录');
      emit('reset-success');
      form.value?.reset();
      Object.assign(formData, INITIAL_FORGOT_PASSWORD_FORM);
    } catch (err: any) {
      MessagePlugin.error(String(err?.message || '重置失败'));
    } finally {
      submitting.value = false;
    }
  };

  return {
    accountValue,
    confirmPasswordValue,
    countDown,
    form,
    formData,
    formRules: createForgotPasswordRules(formData),
    newPasswordValue,
    onSubmit,
    phoneValue,
    sendCode,
    submitting,
    verifyCodeValue,
  };
}
