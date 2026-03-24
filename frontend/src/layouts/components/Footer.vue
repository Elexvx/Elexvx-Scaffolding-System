<template>
  <div :class="`${prefix}-footer`">
    <div v-if="copyrightText">{{ copyrightText }}</div>
    <div v-if="icp">
      <a href="https://beian.miit.gov.cn/" target="_blank">{{ icp }}</a>
    </div>
  </div>
</template>
<script setup lang="ts">
import { computed } from 'vue';

import { prefix } from '@/config/global';
import { useSettingStore } from '@/store';

const setting = useSettingStore();
const websiteName = computed(() => setting.websiteName?.trim() || '');
const company = computed(() => setting.footerCompany?.trim() || websiteName.value || '管理后台');
const icp = computed(() => setting.footerIcp || '');
const startYear = computed(() => setting.copyrightStartYear?.trim() || '');
const copyrightText = computed(() => {
  if (!company.value) return '';
  const currentYear = new Date().getFullYear();
  const normalizedStart = startYear.value || `${currentYear}`;
  const yearText = Number(normalizedStart) === currentYear ? `${currentYear}` : `${normalizedStart}-${currentYear}`;
  return `Copyright © ${yearText} ${company.value}. All Rights Reserved`;
});
</script>
<style lang="less" scoped>
.@{starter-prefix}-footer {
  color: var(--td-text-color-placeholder);
  line-height: 20px;
  text-align: center;

  a {
    color: var(--td-text-color-placeholder);
    text-decoration: none;
    cursor: auto;
  }
}
</style>
