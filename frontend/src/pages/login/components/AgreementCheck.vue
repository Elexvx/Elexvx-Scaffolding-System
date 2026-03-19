<template>
  <div v-if="agreementEnabled" class="agreement-check">
    <t-checkbox :model-value="modelValue" @change="handleChange" />
    <div class="agreement-text">
      <span class="prefix-text" @click="toggle">我已阅读并同意</span>
      <t-link theme="primary" class="agreement-link" @click.stop="open('user')">《用户协议》</t-link>
      和
      <t-link theme="primary" class="agreement-link" @click.stop="open('privacy')">《隐私协议》</t-link>
    </div>

    <t-dialog
      v-model:visible="visible"
      :header="agreementTitle"
      :footer="false"
      width="720px"
      placement="center"
      attach="body"
    >
      <div ref="contentRef" class="agreement-content" v-html="agreementContent"></div>
    </t-dialog>
  </div>
</template>
<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue';

import { useSettingStore } from '@/store';
import { sanitizeHtml } from '@/utils/sanitizeHtml';
import { migrateLocalStorageKey } from '@/utils/storage/compat';

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(['update:modelValue']);
const settingStore = useSettingStore();

const AGREEMENT_ACCEPTED_KEY = 'elexvx.login.agreement.accepted.v2';
const AGREEMENT_ACCEPTED_LEGACY_KEY = 'tdesign.login.agreement.accepted';
const AGREEMENT_ACCEPTED_V2_LEGACY_KEY = 'tdesign.login.agreement.accepted.v2';

const agreementEnabled = computed(() => {
  const user = String(settingStore.userAgreement || '').trim();
  const privacy = String(settingStore.privacyAgreement || '').trim();
  return Boolean(user || privacy);
});

const hashString = (input: string) => {
  let hash = 5381;
  for (let i = 0; i < input.length; i += 1) {
    hash = (hash * 33) ^ input.charCodeAt(i);
  }
  return (hash >>> 0).toString(36);
};

const agreementSignature = computed(() => {
  if (!agreementEnabled.value) return '';
  const user = String(settingStore.userAgreement || '').trim();
  const privacy = String(settingStore.privacyAgreement || '').trim();
  return `${hashString(user)}.${hashString(privacy)}`;
});

const hasAcceptedAgreement = () => {
  if (!agreementEnabled.value) return true;
  if (typeof window === 'undefined') return false;
  try {
    migrateLocalStorageKey(AGREEMENT_ACCEPTED_KEY, [AGREEMENT_ACCEPTED_V2_LEGACY_KEY]);
    const accepted = window.localStorage.getItem(AGREEMENT_ACCEPTED_KEY) === agreementSignature.value;
    if (accepted) return true;
    const legacyAccepted = window.localStorage.getItem(AGREEMENT_ACCEPTED_LEGACY_KEY) === '1';
    if (legacyAccepted) {
      window.localStorage.setItem(AGREEMENT_ACCEPTED_KEY, agreementSignature.value);
      return true;
    }
    return false;
  } catch {
    return false;
  }
};

const persistAgreementAccepted = () => {
  if (!agreementEnabled.value) return;
  if (typeof window === 'undefined') return;
  try {
    window.localStorage.setItem(AGREEMENT_ACCEPTED_KEY, agreementSignature.value);
    window.localStorage.removeItem(AGREEMENT_ACCEPTED_LEGACY_KEY);
  } catch {}
};

const clearAgreementAccepted = () => {
  if (typeof window === 'undefined') return;
  try {
    window.localStorage.removeItem(AGREEMENT_ACCEPTED_KEY);
    window.localStorage.removeItem(AGREEMENT_ACCEPTED_LEGACY_KEY);
  } catch {}
};

const visible = ref(false);
const agreementType = ref<'user' | 'privacy'>('user');
const contentRef = ref<HTMLElement>();
const agreementTitle = computed(() => (agreementType.value === 'user' ? '用户协议' : '隐私协议'));
const agreementContent = computed(() => {
  const html = agreementType.value === 'user' ? settingStore.userAgreement : settingStore.privacyAgreement;
  return sanitizeHtml(html || '<p class="agreement-empty">暂无内容</p>');
});

const open = (type: 'user' | 'privacy') => {
  agreementType.value = type;
  visible.value = true;
  nextTick(() => {
    if (contentRef.value) {
      contentRef.value.scrollTop = 0;
    }
  });
};

const handleChange = (val: boolean) => {
  emit('update:modelValue', val);
  if (val) persistAgreementAccepted();
  else clearAgreementAccepted();
};

const toggle = () => {
  const next = !props.modelValue;
  emit('update:modelValue', next);
  if (next) persistAgreementAccepted();
  else clearAgreementAccepted();
};

const syncAgreementState = () => {
  if (!agreementEnabled.value) {
    emit('update:modelValue', true);
    return;
  }
  if (!props.modelValue && hasAcceptedAgreement()) {
    emit('update:modelValue', true);
  }
  if (props.modelValue && !hasAcceptedAgreement()) {
    emit('update:modelValue', false);
  }
};

onMounted(syncAgreementState);
watch(agreementSignature, syncAgreementState);
watch(agreementEnabled, syncAgreementState);
</script>
<style scoped lang="less">
.agreement-check {
  display: flex;
  align-items: center;

  :deep(.t-checkbox__label) {
    display: none;
  }

  .agreement-text {
    margin-left: 8px;
    font: var(--td-font-body-medium);
    color: var(--td-text-color-secondary);
    line-height: var(--td-line-height-body-medium);
  }

  .prefix-text {
    cursor: pointer;
  }
}

.agreement-link {
  margin: 0 4px;
}

.agreement-content {
  max-height: 60vh;
  overflow: auto;
  padding: 4px 8px;
  color: var(--td-text-color-primary);
  line-height: 1.6;

  :deep(.agreement-empty) {
    color: var(--td-text-color-placeholder);
  }
}
</style>
