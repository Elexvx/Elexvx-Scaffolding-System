<template>
  <t-space class="menu-toolbar">
    <t-input v-model="keywordModel" clearable placeholder="按名称/路由搜索" style="width: 260px" />
    <t-button v-perm="'system:SystemMenu:create'" theme="primary" @click="$emit('create-root')">添加根节点</t-button>
    <t-button v-perm:disable="'system:SystemMenu:query'" variant="outline" @click="$emit('reload')">重置/更新数据</t-button>
    <t-button variant="outline" @click="$emit('toggle-expand')">{{ expandAll ? '收起全部' : '展开全部' }}</t-button>
    <t-button
      v-perm:disable="'system:SystemMenu:update'"
      theme="primary"
      :disabled="!dirty || !canUpdate"
      :loading="savingOrder"
      @click="$emit('save-order')"
      >保存排序</t-button
    >
  </t-space>
</template>

<script setup lang="ts">
const keywordModel = defineModel<string>('keyword', { required: true });

defineProps<{
  expandAll: boolean;
  dirty: boolean;
  canUpdate: boolean;
  savingOrder: boolean;
}>();

defineEmits<{
  (event: 'create-root'): void;
  (event: 'reload'): void;
  (event: 'toggle-expand'): void;
  (event: 'save-order'): void;
}>();
</script>

