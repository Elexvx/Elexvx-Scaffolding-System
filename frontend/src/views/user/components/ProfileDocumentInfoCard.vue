<template>
  <info-section-card
    value="document"
    title="证件信息"
    :percent="percent"
    action-text="编辑信息"
    :sensitive-toggle="true"
    :sensitive-visible="!masked"
    @toggle-sensitive="$emit('toggle-sensitive')"
    @action="$emit('edit')"
  >
    <div id="user-block-document" class="section-body">
      <field-status-item
        v-for="item in items"
        :key="item.key"
        :label="item.label"
        :value="item.value"
        :done="item.done"
        :action-text="item.done ? '' : '去完善'"
        @action="$emit('edit')"
      />
    </div>
  </info-section-card>
</template>

<script setup lang="ts">
import type { StatusItem } from '../types';
import FieldStatusItem from './FieldStatusItem.vue';
import InfoSectionCard from './InfoSectionCard.vue';

defineProps<{
  percent: number;
  masked: boolean;
  items: StatusItem[];
}>();

defineEmits<{
  (event: 'toggle-sensitive'): void;
  (event: 'edit'): void;
}>();
</script>
