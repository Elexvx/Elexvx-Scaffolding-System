<template>
  <t-card title="角色管理" :bordered="false">
    <t-space direction="vertical" style="width: 100%">
      <role-toolbar @create="openCreate" @reload="reload" />
      <role-table :rows="rows" :columns="columns" :loading="loading" @edit="openEdit" @remove="removeRow" />
    </t-space>

    <confirm-drawer v-model:visible="drawerVisible" :header="drawerTitle" size="760px">
      <t-form
        ref="formRef"
        class="drawer-form--single"
        :data="form"
        :rules="rules"
        label-width="120px"
        layout="vertical"
        label-align="right"
        @submit="onSubmit"
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
                  <t-checkbox v-model="expandAll" @change="onExpandAllToggle">展开/折叠</t-checkbox>
                  <t-checkbox v-model="selectAll" @change="onSelectAllToggle">全选/全不选</t-checkbox>
                  <t-checkbox v-model="checkStrictly">父子联动</t-checkbox>
                </t-space>
                <div class="menu-tree-container">
                  <t-tree
                    ref="treeRef"
                    v-model="form.menuIds"
                    v-model:expanded="expandedMenuIds"
                    :data="menuTree"
                    :keys="{ value: 'id', label: 'titleZhCn' }"
                    checkable
                    :check-strictly="!checkStrictly"
                    :disabled="form.name === 'admin'"
                    hover
                    @click="onMenuNodeClick"
                  />
                </div>
              </t-space>
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="10">
            <t-form-item label="页面动作权限">
              <t-space direction="vertical" style="width: 100%">
                <t-select
                  v-model="activeActionMenuId"
                  :options="actionMenuOptions"
                  clearable
                  placeholder="请选择已勾选的页面"
                  :disabled="form.name === 'admin'"
                />
                <div class="menu-action-container">
                  <template v-if="activeActionCatalog">
                    <div class="menu-action-title">{{ activeActionCatalog.title }}</div>
                    <t-checkbox-group
                      :model-value="getMenuActions(activeActionCatalog.menuId)"
                      :disabled="form.name === 'admin'"
                      @change="(value) => setMenuActions(activeActionCatalog.menuId, value as string[])"
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
          <t-button variant="outline" @click="drawerVisible = false">取消</t-button>
          <t-button
            v-perm:disable="editingId ? 'system:SystemRole:update' : 'system:SystemRole:create'"
            theme="primary"
            :loading="saving"
            @click="submitForm"
            >保存</t-button
          >
        </t-space>
      </template>
    </confirm-drawer>
  </t-card>
</template>
<script setup lang="ts">
import { onMounted } from 'vue';

import ConfirmDrawer from '@/components/ConfirmDrawer.vue';
import RoleTable from './RoleTable.vue';
import RoleToolbar from './RoleToolbar.vue';
import { useRoleColumns } from '../hooks/useRoleColumns';
import { useRolePageState } from '../hooks/useRolePageState';

const { columns } = useRoleColumns();
const {
  loading,
  saving,
  canCreate,
  canUpdate,
  rows,
  drawerVisible,
  drawerTitle,
  formRef,
  form,
  menuTree,
  treeRef,
  expandAll,
  expandedMenuIds,
  selectAll,
  checkStrictly,
  activeActionMenuId,
  actionMenuOptions,
  activeActionCatalog,
  rules,
  editingId,
  getActionLabel,
  getMenuActions,
  setMenuActions,
  onExpandAllToggle,
  onSelectAllToggle,
  onMenuNodeClick,
  reload,
  openCreate,
  openEdit,
  submitForm,
  onSubmit,
  removeRow,
} = useRolePageState();

onMounted(async () => {
  await reload();
});
</script>
<style lang="less" scoped>
.menu-tree-container {
  margin-top: 8px;
  padding: 8px;
  border: 1px solid var(--td-component-stroke);
  border-radius: var(--td-radius-default);
  max-height: 400px;
  overflow-y: auto;
}

.menu-action-container {
  margin-top: 8px;
  padding: 12px;
  border: 1px solid var(--td-component-stroke);
  border-radius: var(--td-radius-default);
  min-height: 120px;
}

.menu-action-title {
  margin-bottom: 8px;
  color: var(--td-text-color-primary);
  font-weight: 600;
}

.menu-action-empty {
  color: var(--td-text-color-placeholder);
}
</style>
