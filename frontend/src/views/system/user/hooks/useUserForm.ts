import { computed } from 'vue';

import { buildResetPasswordRules, buildUserFormRules, createResetPasswordModel, createUserFormModel } from '../schema/formSchema';
import type { Mode, PasswordPolicy } from '../types';

const hasSequentialChars = (value: string) => {
  if (!value || value.length < 3) return false;
  let streak = 1;
  let previousCode = -1;
  for (const raw of value) {
    if (!/[a-z0-9]/i.test(raw)) {
      streak = 1;
      previousCode = -1;
      continue;
    }
    const currentCode = raw.toLowerCase().charCodeAt(0);
    if (previousCode !== -1 && Math.abs(currentCode - previousCode) === 1) {
      streak += 1;
      if (streak >= 3) return true;
    } else {
      streak = 1;
    }
    previousCode = currentCode;
  }
  return false;
};

export const useUserForm = (mode: { value: Mode }, passwordPolicy: { value: PasswordPolicy }) => {
  const form = createUserFormModel();
  const resetPasswordForm = createResetPasswordModel();

  const minPasswordLength = computed(() => {
    const value = passwordPolicy.value.minLength;
    return value && value > 0 ? value : 6;
  });

  const requiresCustomPassword = computed(
    () =>
      minPasswordLength.value > 6 ||
      passwordPolicy.value.requireUppercase ||
      passwordPolicy.value.requireLowercase ||
      passwordPolicy.value.requireSpecial ||
      passwordPolicy.value.allowSequential === false,
  );

  const passwordRequirementMessage = computed(() => {
    const parts: string[] = [];
    if (minPasswordLength.value) parts.push(`至少 ${minPasswordLength.value} 位`);
    if (passwordPolicy.value.requireUppercase) parts.push('包含大写字母');
    if (passwordPolicy.value.requireLowercase) parts.push('包含小写字母');
    if (passwordPolicy.value.requireSpecial) parts.push('包含特殊字符');
    if (passwordPolicy.value.allowSequential === false) parts.push('禁止连续字符');
    return parts.join('、') || '至少 6 位';
  });

  const validatePasswordPolicy = (value: string) => {
    if (!value) return true;
    if (value.length < minPasswordLength.value) return false;
    if (passwordPolicy.value.requireUppercase && !/[A-Z]/.test(value)) return false;
    if (passwordPolicy.value.requireLowercase && !/[a-z]/.test(value)) return false;
    if (passwordPolicy.value.requireSpecial && !/[^a-z0-9]/i.test(value)) return false;
    if (passwordPolicy.value.allowSequential === false && hasSequentialChars(value)) return false;
    return true;
  };

  const rules = computed(() =>
    buildUserFormRules({
      mode: mode.value,
      form,
      passwordRequired: requiresCustomPassword.value,
      passwordRequirementMessage: passwordRequirementMessage.value,
      validatePasswordPolicy,
    }),
  );

  const resetRules = computed(() =>
    buildResetPasswordRules({
      passwordRequired: requiresCustomPassword.value,
      passwordRequirementMessage: passwordRequirementMessage.value,
      validatePasswordPolicy,
    }),
  );

  const passwordHelp = computed(() =>
    requiresCustomPassword.value ? `需符合密码规范：${passwordRequirementMessage.value}` : '留空则默认 123456',
  );
  const passwordPlaceholder = computed(() => (requiresCustomPassword.value ? '请输入初始密码' : '默认 123456'));
  const resetPasswordPlaceholder = computed(() => (requiresCustomPassword.value ? '请输入新密码' : '留空则默认 123456'));
  const resetPasswordHelp = computed(() =>
    requiresCustomPassword.value ? `需符合密码规范：${passwordRequirementMessage.value}` : '留空则默认 123456',
  );

  return {
    form,
    resetPasswordForm,
    rules,
    resetRules,
    passwordHelp,
    passwordPlaceholder,
    resetPasswordPlaceholder,
    resetPasswordHelp,
    passwordRequirementMessage,
    requiresCustomPassword,
  };
};
