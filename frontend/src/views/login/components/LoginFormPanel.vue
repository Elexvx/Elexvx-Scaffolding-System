<template>
  <t-form
    ref="form"
    class="item-container"
    :class="[`login-${method}`]"
    :data="formData"
    :rules="formRules"
    :show-error-message="false"
    label-width="0"
    @submit="onSubmit"
  >
    <login-tabs v-model="method" :sms-enabled="settingStore.smsEnabled" :email-enabled="settingStore.emailEnabled" />

    <template v-if="method === 'password'">
      <t-form-item name="account">
        <t-input v-model="accountValue" size="large" :placeholder="t('pages.login.input.account')">
          <template #prefix-icon><t-icon name="user" /></template>
        </t-input>
      </t-form-item>
      <t-form-item name="password">
        <t-input
          v-model="passwordValue"
          size="large"
          :type="showPassword ? 'text' : 'password'"
          clearable
          :placeholder="t('pages.login.input.password')"
        >
          <template #prefix-icon><t-icon name="lock-on" /></template>
          <template #suffix-icon><t-icon :name="showPassword ? 'browse' : 'browse-off'" @click="showPassword = !showPassword" /></template>
        </t-input>
      </t-form-item>
      <t-form-item v-if="captcha.captchaEnabled" name="captcha">
        <div v-if="captcha.captchaType === 'image'" class="captcha-container">
          <t-input v-model="captchaValue" size="large" :placeholder="t('pages.login.required.verification')">
            <template #prefix-icon><t-icon name="verify" /></template>
          </t-input>
          <img :src="captcha.captchaImage" class="captcha-image" @click="() => loadCaptcha()" />
        </div>
        <div v-else class="captcha-placeholder">
          <t-icon name="verify" />
          <span>点击登录后完成验证</span>
          <t-tag v-if="formData.captcha" theme="success" size="small">已完成验证</t-tag>
        </div>
      </t-form-item>
    </template>

    <template v-else-if="method === 'phone'">
      <t-form-item name="phone">
        <t-input v-model="phoneValue" size="large" :placeholder="t('pages.login.input.phone')">
          <template #prefix-icon><t-icon name="mobile" /></template>
        </t-input>
      </t-form-item>
      <verification-code-input
        v-model="verifyCodeValue"
        name="verifyCode"
        :count-down="countDown"
        :placeholder="t('pages.login.input.verification')"
        :send-label="t('pages.login.sendVerification')"
        :resend-label="`${countDown}秒后可重发`"
        @send="sendCode"
      />
    </template>

    <template v-else>
      <t-form-item name="email">
        <t-input v-model="emailValue" size="large" :placeholder="t('pages.login.input.email')">
          <template #prefix-icon><t-icon name="mail" /></template>
        </t-input>
      </t-form-item>
      <verification-code-input
        v-model="verifyCodeValue"
        name="verifyCode"
        :count-down="countDown"
        :placeholder="t('pages.login.input.verification')"
        :send-label="t('pages.login.sendVerification')"
        :resend-label="t('pages.login.resendAfter', [countDown])"
        @send="sendCode"
      />
    </template>

    <t-form-item class="check-container" name="agreed">
      <agreement-checkbox v-model="formData.agreed" />
    </t-form-item>
    <t-form-item v-if="method === 'password'" class="check-container" name="checked">
      <t-checkbox v-model="formData.checked">{{ t('pages.login.remember') }}</t-checkbox>
    </t-form-item>
    <t-form-item class="btn-container">
      <t-button block size="large" type="submit">{{ t('pages.login.signIn') }}</t-button>
    </t-form-item>
    <div class="switch-container">
      <span class="tip" @click="emit('register')">{{ t('pages.login.createAccount') }}</span>
      <span class="tip" @click="emit('forgot')">{{ t('pages.login.forget') }}</span>
    </div>
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
import { t } from '@/locales';
import { useSettingStore } from '@/store';

import { useLoginForm } from '../hooks/useLoginForm';
import AgreementCheckbox from './AgreementCheckbox.vue';
import LoginTabs from './LoginTabs.vue';
import VerificationCodeInput from './VerificationCodeInput.vue';

const emit = defineEmits<{ register: []; forgot: [] }>();
const settingStore = useSettingStore();
const {
  accountValue,
  captcha,
  captchaValue,
  completeDragCaptcha,
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
} = useLoginForm();
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

.captcha-image {
  width: 120px;
  height: 40px;
  cursor: pointer;
  border-radius: var(--td-radius-default);
  flex-shrink: 0;
}
</style>
