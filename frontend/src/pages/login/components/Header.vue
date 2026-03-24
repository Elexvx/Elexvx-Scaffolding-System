<template>
  <header class="login-header">
    <img v-if="logoUrl" :src="logoUrl" class="logo" />
    <span v-else class="logo-text">{{ displayName }}</span>
    <div class="operations-container"></div>
  </header>
</template>
<script setup lang="ts">
import { computed } from 'vue';

import { useSettingStore } from '@/store';

const settingStore = useSettingStore();
const logoUrl = computed(() => settingStore.logoExpandedUrl);
const displayName = computed(() => settingStore.websiteName?.trim() || '管理后台');
</script>
<style lang="less" scoped>
.login-header {
  padding: 0 var(--td-comp-paddingLR-xl);
  display: flex;
  justify-content: space-between;
  align-items: center;
  backdrop-filter: blur(10px);
  color: var(--td-text-color-primary);
  height: var(--td-comp-size-xxxl);
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  z-index: 2;

  .logo {
    width: 178px;
    height: var(--td-comp-size-s);
  }

  .logo-text {
    font-size: 20px;
    font-weight: 600;
  }

  .operations-container {
    display: flex;
    align-items: center;

    .t-button {
      margin-left: var(--td-comp-margin-l);
    }
  }
}
</style>
