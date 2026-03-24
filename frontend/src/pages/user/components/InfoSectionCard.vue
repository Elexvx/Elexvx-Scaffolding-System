<template>
  <view-info-section-card
    v-bind="props"
    @action="$emit('action')"
    @toggle-sensitive="$emit('toggle-sensitive')"
  >
    <slot />
  </view-info-section-card>
</template>

<script setup lang="ts">
import ViewInfoSectionCard from '@/views/user/components/InfoSectionCard.vue';

const props = withDefaults(
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
