<template>
  <t-card :bordered="false" class="completion-card" :loading="loading">
    <div class="completion-card__head">
      <div class="completion-card__title">信息完整度 {{ percent }}%</div>
      <t-button variant="text" theme="primary" @click="$emit('cta-click')">{{ ctaText }}</t-button>
    </div>

    <t-progress :percentage="percent" theme="line" :status="percent >= 100 ? 'success' : 'active'" />

    <t-list class="completion-card__list" split>
      <t-list-item v-for="item in todos" :key="item.key" @click="$emit('item-click', item)">
        <div class="completion-card__todo-row">
          <div class="completion-card__todo-main">
            <div class="completion-card__todo-title">{{ item.title }}</div>
            <div class="completion-card__todo-gain">预计提升 {{ item.gain }}</div>
          </div>
          <t-button size="small" variant="text" theme="primary" @click.stop="$emit('item-click', item)">
            {{ item.actionText || '去填写' }}
          </t-button>
        </div>
      </t-list-item>
    </t-list>

    <t-button class="completion-card__cta" theme="primary" block @click="$emit('cta-click')">{{ ctaText }}</t-button>
  </t-card>
</template>

<script setup lang="ts">
interface CompletionTodo {
  key: string;
  title: string;
  gain: string;
  section: string;
  actionText?: string;
}

withDefaults(
  defineProps<{
    loading?: boolean;
    percent: number;
    todos: CompletionTodo[];
    ctaText?: string;
  }>(),
  {
    loading: false,
    ctaText: '一键去补全',
  },
);

defineEmits<{
  (e: 'item-click', item: CompletionTodo): void;
  (e: 'cta-click'): void;
}>();
</script>

<style lang="less" scoped>
.completion-card {
  :deep(.t-card__body) {
    display: grid;
    gap: 12px;
    padding: 18px;
  }

  &__head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
  }

  &__title {
    font-size: 16px;
    font-weight: 600;
    color: var(--td-text-color-primary);
  }

  &__list {
    border-radius: var(--td-radius-medium);
    border: 1px solid var(--td-border-level-1-color);
    overflow: hidden;
  }

  &__todo-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    width: 100%;
  }

  &__todo-main {
    min-width: 0;
  }

  &__todo-title {
    font-size: 14px;
    color: var(--td-text-color-primary);
  }

  &__todo-gain {
    font-size: 12px;
    color: var(--td-text-color-secondary);
    margin-top: 2px;
  }

  @media (min-width: 768px) and (max-width: 1200px) {
    :deep(.t-card__body) {
      grid-template-columns: minmax(220px, 0.95fr) minmax(0, 1.05fr);
      grid-template-areas:
        'head head'
        'progress list'
        'cta cta';
      align-items: start;
    }

    &__head {
      grid-area: head;
    }

    :deep(.t-progress) {
      grid-area: progress;
      margin-top: 8px;
    }

    &__list {
      grid-area: list;
    }

    &__cta {
      grid-area: cta;
    }
  }

  @media (max-width: 767px) {
    :deep(.t-card__body) {
      padding: 16px;
    }
  }
}
</style>
