<template>
  <div class="verification-container">
    <t-card title="验证设置" :bordered="false">
      <verification-panel />
    </t-card>
  </div>
</template>
<script setup lang="ts">
import VerificationPanel from '@/pages/system/personalize/components/VerificationPanel.vue';
</script>
<style lang="less" scoped>
.verification-container {
  :deep(.t-card__body) {
    padding: 8px 24px 24px;
  }

  :deep(.t-drawer__body) {
    padding: var(--td-comp-paddingTB-xxl) var(--td-comp-paddingLR-xxl);
  }

  :deep(.t-drawer__footer) {
    padding: var(--td-comp-paddingTB-l) var(--td-comp-paddingLR-xxl);
  }
}
</style>
