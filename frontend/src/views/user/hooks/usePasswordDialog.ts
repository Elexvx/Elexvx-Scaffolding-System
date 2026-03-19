import type { FormInstanceFunctions, SubmitContext } from 'tdesign-vue-next';
import { MessagePlugin } from 'tdesign-vue-next';
import { reactive, ref } from 'vue';

import { changePassword } from '@/api/user';

import { createPasswordFormModel, createPasswordRules } from '../schema/passwordSchema';
import type { PasswordFormModel } from '../types';

export const usePasswordDialog = () => {
  const changingPassword = ref(false);
  const passwordFormRef = ref<FormInstanceFunctions>();
  const passwordForm = reactive<PasswordFormModel>(createPasswordFormModel());
  const passwordRules = createPasswordRules(passwordForm);

  const resetPasswordForm = () => {
    passwordForm.oldPassword = '';
    passwordForm.newPassword = '';
    passwordForm.confirmPassword = '';
    passwordFormRef.value?.reset();
  };

  const submitPassword = async (context: SubmitContext) => {
    if (context.validateResult !== true) return;
    changingPassword.value = true;
    try {
      await changePassword(passwordForm);
      MessagePlugin.success('密码修改成功');
      resetPasswordForm();
    } catch (error: unknown) {
      const raw = String((error as { message?: string })?.message || '密码修改失败');
      const humanMsg = raw.replace(/\s*\[\d{3}\]\s*$/, '').trim();
      MessagePlugin.error(humanMsg || '密码修改失败');
    } finally {
      changingPassword.value = false;
    }
  };

  return { changingPassword, passwordFormRef, passwordForm, passwordRules, resetPasswordForm, submitPassword };
};
