<template>
  <t-collapse-panel :value="value" class="info-section-card">
    <template #header>
      <div class="info-section-card__header">
        <div class="info-section-card__title-wrap">
          <span class="info-section-card__title">{{ title }}</span>
          <t-tag size="small" variant="light" theme="primary">{{ percent }}%</t-tag>
        </div>
        <t-button size="small" variant="text" theme="primary" @click.stop="$emit('action')">
          {{ actionText || '完善/编辑' }}
        </t-button>
      </div>
    </template>
    <slot />
  </t-collapse-panel>
</template>

<script setup lang="ts">
defineProps<{
  value: string;
  title: string;
  percent: number;
  actionText?: string;
}>();

defineEmits<{
  (e: 'action'): void;
}>();
</script>

<style lang="less" scoped>
.info-section-card {
  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    width: 100%;
    gap: 12px;
  }

  &__title-wrap {
    display: inline-flex;
    align-items: center;
    gap: 10px;
    min-width: 0;
  }

  &__title {
    font-size: 15px;
    font-weight: 600;
    color: var(--td-text-color-primary);
  }
}
</style>
