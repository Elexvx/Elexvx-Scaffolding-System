<template>
  <div class="security-setting">
    <t-card title="安全设置" :bordered="false">
      <t-tabs v-model="activeTab">
        <t-tab-panel value="token" label="Token 策略" :destroy-on-hide="false"><div class="security-content"><security-policy-form :form="form" :saving="saving" :can-edit="canEdit" @save="saveSettings" /></div></t-tab-panel>
        <t-tab-panel value="captcha" label="验证码设置" :destroy-on-hide="false"><div class="security-content"><verification-policy-form :form="form" :saving="saving" :can-edit="canEdit" :disable-confirm-visible="disableConfirmVisible" :handle-captcha-enabled-change="handleCaptchaEnabledChange" :confirm-disable-captcha="confirmDisableCaptcha" :cancel-disable-captcha="cancelDisableCaptcha" @save="saveSettings" /></div></t-tab-panel>
        <t-tab-panel value="defense" label="防御阈值" :destroy-on-hide="false"><div class="security-content"><session-policy-form :form="form" :saving="saving" :can-edit="canEdit" @save="saveSettings" /></div></t-tab-panel>
        <t-tab-panel value="password" label="密码规范设置" :destroy-on-hide="false"><div class="security-content"><password-policy-form :form="form" :saving="saving" :can-edit="canEdit" @save="saveSettings" /></div></t-tab-panel>
      </t-tabs>
    </t-card>
  </div>
</template>
<script setup lang="ts">
import { onMounted, watch } from 'vue';

import PasswordPolicyForm from './PasswordPolicyForm.vue';
import SecurityPolicyForm from './SecurityPolicyForm.vue';
import SessionPolicyForm from './SessionPolicyForm.vue';
import VerificationPolicyForm from './VerificationPolicyForm.vue';
import { usePasswordPolicyForm } from '../hooks/usePasswordPolicyForm';
import { useSecurityPageState } from '../hooks/useSecurityPageState';
import { useSecurityPermissions } from '../hooks/useSecurityPermissions';
import { useSecurityPolicyForm } from '../hooks/useSecurityPolicyForm';
import { useSessionPolicyForm } from '../hooks/useSessionPolicyForm';
import { useVerificationPolicyForm } from '../hooks/useVerificationPolicyForm';

const { activeTab, saving, disableConfirmVisible, syncTab } = useSecurityPageState();
const { canEdit } = useSecurityPermissions();
const { form, loadSettings, saveSettings } = useSecurityPolicyForm(saving);
usePasswordPolicyForm(form);
useSessionPolicyForm(form);
const { handleCaptchaEnabledChange, confirmDisableCaptcha, cancelDisableCaptcha } = useVerificationPolicyForm(form, disableConfirmVisible);
watch(activeTab, (tab) => syncTab(tab));
onMounted(() => { void loadSettings(); });
</script>
<style scoped lang="less">
.security-setting { display: flex; flex-direction: column; gap: 16px; :deep(.t-card__body) { padding: 8px 24px 24px; } }
.security-content { padding-top: 24px; }
</style>
