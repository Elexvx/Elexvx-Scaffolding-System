<template>
  <t-tabs v-model="model" class="login-tabs">
    <t-tab-panel v-if="smsEnabled" value="phone" :label="t('pages.login.phoneLogin')" :destroy-on-hide="false" />
    <t-tab-panel v-if="emailEnabled" value="email" :label="t('pages.login.emailLogin')" :destroy-on-hide="false" />
    <t-tab-panel value="password" :label="t('pages.login.accountLogin')" :destroy-on-hide="false" />
  </t-tabs>
</template>

<script setup lang="ts">
import { t } from '@/locales';

import type { LoginMethod } from '../types';

const model = defineModel<LoginMethod>({ default: 'password' });
defineProps<{ smsEnabled: boolean; emailEnabled: boolean }>();
</script>

<style scoped lang="less">
.login-tabs {
  margin-bottom: 12px;
}
</style>
