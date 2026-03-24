<template>
  <t-form
    ref="form"
    class="item-container"
    :data="formData"
    :rules="formRules"
    :show-error-message="false"
    label-width="0"
    @submit="onSubmit"
  >
    <t-form-item name="account">
      <t-input v-model="accountValue" size="large" placeholder="请输入账号">
        <template #prefix-icon><t-icon name="user" /></template>
      </t-input>
    </t-form-item>
    <t-form-item name="phone">
      <t-input v-model="phoneValue" size="large" placeholder="请输入手机号（不支持空格）">
        <template #prefix-icon><t-icon name="mobile" /></template>
      </t-input>
    </t-form-item>
    <verification-code-input
      v-model="verifyCodeValue"
      name="verifyCode"
      :count-down="countDown"
      placeholder="请输入验证码"
      send-label="发送验证码"
      :resend-label="`${countDown}秒后可重发`"
      @send="sendCode"
    />
    <t-form-item name="newPassword">
      <t-input v-model="newPasswordValue" size="large" type="password" clearable placeholder="请输入新密码">
        <template #prefix-icon><t-icon name="lock-on" /></template>
      </t-input>
    </t-form-item>
    <t-form-item name="confirmPassword">
      <t-input v-model="confirmPasswordValue" size="large" type="password" clearable placeholder="请确认新密码">
        <template #prefix-icon><t-icon name="lock-on" /></template>
      </t-input>
    </t-form-item>
    <t-form-item class="check-container" name="checked">
      <agreement-checkbox v-model="formData.checked" />
    </t-form-item>
    <t-form-item class="btn-container split-buttons">
      <t-button size="large" theme="default" variant="text" @click="emit('back')">返回登录</t-button>
      <t-button size="large" type="submit" :loading="submitting">找回密码</t-button>
    </t-form-item>
  </t-form>
</template>

<script setup lang="ts">
import { useForgotPasswordForm } from '../hooks/useForgotPasswordForm';
import AgreementCheckbox from './AgreementCheckbox.vue';
import VerificationCodeInput from './VerificationCodeInput.vue';

const emit = defineEmits<{ back: []; 'reset-success': [] }>();
const forgotEmit = (event: 'reset-success') => emit(event);
const {
  accountValue,
  confirmPasswordValue,
  countDown,
  form,
  formData,
  formRules,
  newPasswordValue,
  onSubmit,
  phoneValue,
  sendCode,
  submitting,
  verifyCodeValue,
} = useForgotPasswordForm(forgotEmit);
</script>

<style scoped lang="less">
@import '../../../pages/login/index.less';
</style>
