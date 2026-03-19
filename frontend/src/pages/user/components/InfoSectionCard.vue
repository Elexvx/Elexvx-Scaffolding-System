<template>
  <t-collapse-panel :value="value" class="info-section-card">
    <template #header>
      <div class="info-section-card__header">
        <div class="info-section-card__title-wrap">
          <span class="info-section-card__title">{{ title }}</span>
          <t-tag size="small" variant="light" theme="primary">{{ percent }}%</t-tag>
        </div>
        <div v-if="sensitiveToggle || showAction" class="info-section-card__operation-wrap">
          <t-tooltip v-if="sensitiveToggle" :content="sensitiveVisible ? '隐藏敏感信息' : '显示敏感信息'">
            <span
              class="info-section-card__sensitive-toggle"
              role="button"
              tabindex="0"
              @click.stop="$emit('toggle-sensitive')"
            >
              <t-icon :name="sensitiveVisible ? 'browse' : 'browse-off'" />
            </span>
          </t-tooltip>
          <t-button v-if="showAction" size="small" theme="primary" @click.stop="$emit('action')">
            {{ actionText || '编辑信息' }}
          </t-button>
        </div>
      </div>
    </template>
    <slot />
  </t-collapse-panel>
</template>
<script setup lang="ts">
withDefaults(
  defineProps<{
    value: string;
    title: string;
    percent: number;
    sensitiveToggle?: boolean;
    sensitiveVisible?: boolean;
    showAction?: boolean;
    actionText?: string;
  }>(),
  {
    showAction: true,
  },
);

defineEmits<{
  (e: 'action'): void;
  (e: 'toggle-sensitive'): void;
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

  &__operation-wrap {
    display: inline-flex;
    align-items: center;
    gap: 12px;
    flex-shrink: 0;
  }

  &__sensitive-toggle {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 24px;
    height: 24px;
    border-radius: var(--td-radius-small);
    color: var(--td-text-color-secondary);
    cursor: pointer;
    line-height: 1;
  }

  &__sensitive-toggle:hover {
    color: var(--td-text-color-primary);
    background: var(--td-bg-color-container-hover);
  }
}
</style>
