<template>
  <div class="field-status-item" :class="{ 'field-status-item--done': done }">
    <div class="field-status-item__main">
      <t-icon :name="done ? 'check-circle-filled' : 'error-circle-filled'" />
      <div class="field-status-item__meta">
        <div class="field-status-item__label">{{ label }}</div>
        <div v-if="value" class="field-status-item__value">{{ value }}</div>
      </div>
    </div>
    <t-button v-if="actionText" size="small" variant="text" theme="primary" @click="$emit('action')">
      {{ actionText }}
    </t-button>
  </div>
</template>
<script setup lang="ts">
defineProps<{
  label: string;
  value?: string;
  done: boolean;
  actionText?: string;
}>();

defineEmits<{
  (e: 'action'): void;
}>();
</script>
<style lang="less" scoped>
.field-status-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 0;

  &__main {
    display: flex;
    align-items: flex-start;
    gap: 8px;
    min-width: 0;
  }

  &__meta {
    min-width: 0;
  }

  &__label {
    color: var(--td-text-color-primary);
    font-size: 14px;
    line-height: 22px;
  }

  &__value {
    color: var(--td-text-color-secondary);
    font-size: 13px;
    line-height: 20px;
    word-break: break-all;
  }

  .t-icon {
    margin-top: 3px;
    color: var(--td-warning-color);
  }

  &--done .t-icon {
    color: var(--td-success-color);
  }
}
</style>
