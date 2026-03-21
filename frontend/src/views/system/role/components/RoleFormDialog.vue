<template>
  <confirm-drawer :visible="visible" :header="drawerTitle" size="760px" @update:visible="$emit('update:visible', $event)">
    <t-form
      ref="localFormRef"
      class="drawer-form--single"
      :data="form"
      :rules="rules"
      label-width="120px"
      layout="vertical"
      label-align="right"
      @submit="$emit('submit', $event)"
    >
      <t-row :gutter="[24, 24]">
        <t-col :xs="24" :sm="12">
          <t-form-item label="角色名" name="name">
            <t-input v-model="form.name" :disabled="form.name === 'admin'" style="max-width: 500px; width: 100%" />
          </t-form-item>
        </t-col>
        <t-col :xs="24" :sm="12">
          <t-form-item label="描述" name="description">
            <t-input v-model="form.description" style="max-width: 500px; width: 100%" />
          </t-form-item>
        </t-col>
        <t-col :xs="24" :sm="14">
          <t-form-item label="菜单权限">
            <t-space direction="vertical" style="width: 100%">
              <t-space>
                <t-checkbox :model-value="expandAll" @change="$emit('expand-all', $event)">展开/折叠</t-checkbox>
                <t-checkbox :model-value="selectAll" @change="$emit('select-all', $event)">全选/全不选</t-checkbox>
                <t-checkbox v-model="formLinkCheck">父子联动</t-checkbox>
              </t-space>
              <div class="menu-tree-container">
                <t-tree
                  ref="treeRef"
                  v-model="form.menuIds"
                  :expanded="expandedMenuIds"
                  :data="menuTree"
                  :keys="{ value: 'id', label: 'titleZhCn' }"
                  checkable
                  :check-strictly="!formLinkCheck"
                  :disabled="form.name === 'admin'"
                  hover
                  @update:expanded="$emit('update:expanded-menu-ids', $event)"
                  @click="$emit('menu-click', $event)"
                />
              </div>
            </t-space>
          </t-form-item>
        </t-col>
        <t-col :xs="24" :sm="10">
          <t-form-item label="页面动作权限">
            <t-space direction="vertical" style="width: 100%">
              <t-select
                :value="activeActionMenuId"
                :options="actionMenuOptions"
                clearable
                placeholder="请选择已勾选的页面"
                :disabled="form.name === 'admin'"
                @update:value="$emit('update:active-action-menu-id', $event)"
              />
              <div class="menu-action-container">
                <template v-if="activeActionCatalog">
                  <div class="menu-action-title">{{ activeActionCatalog.title }}</div>
                  <t-checkbox-group
                    :model-value="getMenuActions(activeActionCatalog.menuId)"
                    :disabled="form.name === 'admin'"
                    @change="$emit('set-menu-actions', activeActionCatalog.menuId, $event as string[])"
                  >
                    <t-space wrap>
                      <t-checkbox v-for="action in activeActionCatalog.actions" :key="action" :value="action">
                        {{ getActionLabel(action) }}
                      </t-checkbox>
                    </t-space>
                  </t-checkbox-group>
                </template>
                <span v-else class="menu-action-empty">请先勾选页面菜单，再选择页面配置动作权限</span>
              </div>
            </t-space>
          </t-form-item>
        </t-col>
      </t-row>
    </t-form>
    <template #footer>
      <t-space class="tdesign-starter-action-bar">
        <t-button variant="outline" @click="$emit('update:visible', false)">取消</t-button>
        <t-button v-perm:disable="editingId ? 'system:SystemRole:update' : 'system:SystemRole:create'" theme="primary" :loading="saving" @click="$emit('submit-form')">
          保存
        </t-button>
      </t-space>
    </template>
  </confirm-drawer>
</template>

<script setup lang="ts">
import type { FormInstanceFunctions, FormRule } from 'tdesign-vue-next';
import { computed } from 'vue';

import ConfirmDrawer from '@/components/ConfirmDrawer.vue';

import type { ActionMenuOption, MenuNode, PermissionCatalogItem } from '../types';

const props = defineProps<{
  visible: boolean;
  drawerTitle: string;
  formRef: FormInstanceFunctions | undefined;
  form: Record<string, any>;
  rules: Record<string, FormRule[]>;
  menuTree: MenuNode[];
  treeRef: any;
  expandAll: boolean;
  expandedMenuIds: number[];
  selectAll: boolean;
  checkStrictly: boolean;
  activeActionMenuId: number | null;
  actionMenuOptions: ActionMenuOption[];
  activeActionCatalog: PermissionCatalogItem | null;
  editingId: number | null;
  saving: boolean;
  getActionLabel: (action: string) => string;
  getMenuActions: (menuId: number) => string[];
}>();

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void;
  (e: 'submit', ctx: any): void;
  (e: 'submit-form'): void;
  (e: 'expand-all', value: boolean): void;
  (e: 'select-all', value: boolean): void;
  (e: 'menu-click', ctx: any): void;
  (e: 'set-menu-actions', menuId: number, actions: string[]): void;
  (e: 'update:expanded-menu-ids', ids: number[]): void;
  (e: 'update:active-action-menu-id', menuId: number | null): void;
  (e: 'update:check-strictly', value: boolean): void;
}>();

const localFormRef = computed({
  get: () => props.formRef,
  set: () => {},
});

const formLinkCheck = computed({
  get: () => props.checkStrictly,
  set: (value: boolean) => emit('update:check-strictly', value),
});
</script>
