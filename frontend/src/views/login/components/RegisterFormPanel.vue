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
      <t-input v-model="accountValue" size="large" placeholder="请输入用户名">
        <template #prefix-icon><t-icon name="user" /></template>
      </t-input>
    </t-form-item>
    <t-form-item name="password" :help="passwordHelp">
      <t-input v-model="passwordValue" size="large" :type="showPassword ? 'text' : 'password'" clearable placeholder="请输入密码">
        <template #prefix-icon><t-icon name="lock-on" /></template>
        <template #suffix-icon><t-icon :name="showPassword ? 'browse' : 'browse-off'" @click="showPassword = !showPassword" /></template>
      </t-input>
    </t-form-item>
    <t-form-item name="confirmPassword">
      <t-input
        v-model="confirmPasswordValue"
        size="large"
        :type="showConfirmPassword ? 'text' : 'password'"
        clearable
        placeholder="请确认密码"
      >
        <template #prefix-icon><t-icon name="lock-on" /></template>
        <template #suffix-icon><t-icon :name="showConfirmPassword ? 'browse' : 'browse-off'" @click="showConfirmPassword = !showConfirmPassword" /></template>
      </t-input>
    </t-form-item>
    <t-form-item v-if="captcha.captchaEnabled" name="captcha">
      <div v-if="captcha.captchaType === 'image'" class="captcha-container">
        <t-input v-model="captchaValue" size="large" placeholder="请输入验证码">
          <template #prefix-icon><t-icon name="verify" /></template>
        </t-input>
        <img :src="captcha.captchaImage" class="captcha-image" @click="() => loadCaptcha()" />
      </div>
      <div v-else class="captcha-placeholder">
        <t-icon name="verify" />
        <span>点击注册后完成验证</span>
        <t-tag v-if="formData.captcha" theme="success" size="small">已完成验证</t-tag>
      </div>
    </t-form-item>
    <t-form-item class="check-container" name="checked">
      <agreement-checkbox v-model="formData.checked" />
    </t-form-item>
    <t-form-item class="btn-container split-buttons">
      <t-button size="large" theme="default" variant="text" @click="emit('back')">返回登录</t-button>
      <t-button size="large" type="submit" :loading="submitting">注册</t-button>
    </t-form-item>
  </t-form>

  <t-dialog
    v-model:visible="captcha.showDragCaptchaDialog"
    header="完成安全验证"
    :footer="false"
    :close-on-overlay-click="false"
    :width="`${captcha.dragWidth + 70}px`"
  >
    <drag-captcha
      v-if="captcha.showDragCaptchaDialog"
      :key="captcha.dragRefreshKey"
      :width="captcha.dragWidth"
      :height="captcha.dragHeight"
      @success="completeDragCaptcha"
      @refresh="handleDragRefresh"
    />
  </t-dialog>
</template>

<script setup lang="ts">
import DragCaptcha from '@/components/DragCaptcha.vue';

import { useRegisterForm } from '../hooks/useRegisterForm';
import AgreementCheckbox from './AgreementCheckbox.vue';

const emit = defineEmits<{ back: []; 'register-success': [] }>();
const registerEmit = (event: 'register-success') => emit(event);
const {
  accountValue,
  captcha,
  captchaValue,
  completeDragCaptcha,
  confirmPasswordValue,
  form,
  formData,
  formRules,
  handleDragRefresh,
  loadCaptcha,
  onSubmit,
  passwordHelp,
  passwordValue,
  showConfirmPassword,
  showPassword,
  submitting,
} = useRegisterForm(registerEmit);

</script>

<style scoped lang="less">
@import '../../../pages/login/index.less';

.captcha-container {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 12px;

  :deep(.t-input) {
    flex: 1;
    width: auto;
  }
}

.captcha-image {
  width: 120px;
  height: 40px;
  cursor: pointer;
  border-radius: var(--td-radius-default);
  flex-shrink: 0;
}

.captcha-placeholder {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  padding: 0 12px;
  height: 40px;
  border: 1px dashed var(--td-component-stroke);
  border-radius: var(--td-radius-default);
  color: var(--td-text-color-secondary);
}
</style>
