<template>
  <t-dialog :visible="visible" width="min(1120px, calc(100vw - 48px))" placement="center" :header="title" :footer="false" @update:visible="emit('update:visible', $event)">
    <div class="leader-dialog">
      <div class="leader-dialog__search">
        <t-input
          :model-value="keyword"
          clearable
          placeholder="请输入用户姓名"
          class="leader-dialog__keyword"
          @update:model-value="emit('update:keyword', $event as string)"
        />
        <t-space class="leader-dialog__actions" size="small">
          <t-button theme="primary" @click="emit('search')">搜索</t-button>
          <t-button variant="outline" @click="emit('reset')">重置</t-button>
        </t-space>
      </div>
      <div class="leader-dialog__content">
        <org-tree-panel
          title="组织机构"
          :keyword="orgKeyword"
          :data="treeData"
          :tree-keys="treeKeys"
          @update:keyword="emit('update:org-keyword', $event)"
          @select="emit('org-select', $event)"
        />
        <div class="leader-panel">
          <div class="leader-panel__title">用户列表</div>
          <div class="leader-panel__body">
            <div class="leader-table">
              <t-table
                row-key="id"
                :data="rows"
                :columns="columns"
                :pagination="pagination"
                :selected-row-keys="selection.map((user) => user.id)"
                @select-change="(selectedKeys, context) => emit('select-change', selectedKeys, context)"
                @page-change="(pageInfo) => emit('page-change', pageInfo)"
              />
            </div>
          </div>
        </div>
        <div class="leader-panel">
          <div class="leader-panel__title">已选择用户 ({{ selection.length }}人)</div>
          <div class="leader-panel__body leader-selected">
            <t-tag v-for="user in selection" :key="user.id" theme="primary" variant="light" closable @close="emit('remove', user.id)">
              {{ user.name }}
            </t-tag>
          </div>
        </div>
      </div>
      <div class="dialog-footer">
        <t-button variant="outline" @click="emit('update:visible', false)">取消</t-button>
        <t-button theme="primary" :loading="confirmLoading" @click="emit('confirm')">{{ confirmText }}</t-button>
      </div>
    </div>
  </t-dialog>
</template>

<script setup lang="ts">
import type { PrimaryTableCol, TreeProps } from 'tdesign-vue-next';

import type { OrgUnitNode, UserRow } from '../types';
import OrgTreePanel from './OrgTreePanel.vue';

defineProps<{
  visible: boolean;
  title: string;
  keyword: string;
  orgKeyword: string;
  treeData: OrgUnitNode[];
  treeKeys: TreeProps['keys'];
  rows: UserRow[];
  columns: PrimaryTableCol[];
  pagination: { current: number; pageSize: number; total: number };
  selection: UserRow[];
  confirmText: string;
  confirmLoading?: boolean;
}>();

const emit = defineEmits([
  'update:visible',
  'update:keyword',
  'update:org-keyword',
  'search',
  'reset',
  'org-select',
  'select-change',
  'page-change',
  'remove',
  'confirm',
]);
</script>
