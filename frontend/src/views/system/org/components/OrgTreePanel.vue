<template>
  <div class="leader-panel">
    <div class="leader-panel__title">{{ title }}</div>
    <div class="leader-panel__body">
      <t-input
        :model-value="keyword"
        clearable
        placeholder="请输入组织名称"
        class="leader-panel__filter"
        @update:model-value="emit('update:keyword', $event as string)"
      />
      <t-tree class="leader-tree" :data="data" :keys="treeKeys" hover activable @click="emit('select', $event)" />
    </div>
  </div>
</template>

<script setup lang="ts">
import type { TreeProps } from 'tdesign-vue-next';

import type { OrgUnitNode } from '../types';

defineProps<{
  title: string;
  keyword: string;
  data: OrgUnitNode[];
  treeKeys: TreeProps['keys'];
}>();

const emit = defineEmits<{
  'update:keyword': [string];
  select: [unknown];
}>();
</script>
