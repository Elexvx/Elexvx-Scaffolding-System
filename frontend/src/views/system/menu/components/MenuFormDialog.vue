<template>
  <confirm-drawer :visible="visible" :header="title" size="760px" @update:visible="(value) => $emit('update:visible', value)">
    <t-form ref="formRef" :data="form" label-align="right" label-width="120px" layout="vertical" class="menu-drawer-form drawer-form--single">
      <t-form-item label="上级菜单" name="parentId">
        <menu-parent-selector :model-value="form.parentId" :options="parentOptions" :tree-keys="treeKeys" @change="onParentIdChange" @clear="onParentIdClear" />
      </t-form-item>

      <t-form-item label="菜单类型" name="nodeType">
        <t-radio-group v-model="form.nodeType">
          <t-radio v-for="option in nodeTypeRadioOptions" :key="String(option.value)" :value="option.value">{{ option.label }}</t-radio>
        </t-radio-group>
      </t-form-item>

      <t-form-item label="菜单图标" name="icon">
        <menu-icon-picker v-model="form.icon" :options="iconOptions" :value-display="renderIconValue" />
      </t-form-item>

      <t-form-item label="显示排序" name="orderNo">
        <t-input-number v-model="form.orderNo" :min="0" theme="column" style="width: 100%" />
      </t-form-item>

      <t-form-item label="菜单名称" name="titleZhCn" required>
        <t-input v-model="form.titleZhCn" placeholder="请输入菜单名称" />
      </t-form-item>

      <t-form-item v-if="form.nodeType === 'PAGE'" label="是否外链">
        <t-radio-group :model-value="isExternalLink" @update:model-value="(value) => $emit('update:external-link', Boolean(value))">
          <t-radio :value="true">是</t-radio>
          <t-radio :value="false">否</t-radio>
        </t-radio-group>
      </t-form-item>

      <t-form-item
        label="路由地址"
        name="path"
        required
        :help="form.nodeType === 'PAGE' ? '访问的路由地址，如：`user`，外链如 `https://...` ' : '访问的路由地址，如：`user` '"
      >
        <t-input v-if="!isExternalLink" v-model="form.path" placeholder="请输入路由地址" />
        <t-input v-else v-model="form.frameSrc" placeholder="请输入外链地址" />
      </t-form-item>

      <t-form-item v-if="form.nodeType === 'PAGE'" label="组件路径" name="component" required>
        <t-select
          v-model="form.component"
          :options="pageComponentOptions"
          filterable
          creatable
          placeholder="选择组件（如 /system/user/index）或直接输入"
          :popup-props="{ attach: 'body' }"
        />
      </t-form-item>

      <t-form-item label="显示状态">
        <t-radio-group :model-value="!form.hidden" @update:model-value="(value) => (form.hidden = !value)">
          <t-radio :value="true">显示</t-radio>
          <t-radio :value="false">隐藏</t-radio>
        </t-radio-group>
      </t-form-item>
    </t-form>

    <template #footer>
      <t-space>
        <t-button variant="outline" @click="$emit('cancel')">取消</t-button>
        <t-button theme="primary" :loading="saving" @click="$emit('confirm')">确定</t-button>
      </t-space>
    </template>
  </confirm-drawer>
</template>

<script setup lang="tsx">
import type { SelectOption } from 'tdesign-vue-next';

import ConfirmDrawer from '@/components/ConfirmDrawer.vue';

import type { MenuFormModel, MenuNode } from '../types';
import MenuIconPicker from './MenuIconPicker.vue';
import MenuParentSelector from './MenuParentSelector.vue';

defineProps<{
  visible: boolean;
  title: string;
  form: MenuFormModel;
  saving: boolean;
  isExternalLink: boolean;
  parentOptions: MenuNode[];
  treeKeys: { value: string; label: string; children: string };
  nodeTypeRadioOptions: Array<{ label: string; value: string | number | boolean; disabled?: boolean }>;
  iconOptions: SelectOption[];
  pageComponentOptions: SelectOption[];
  renderIconValue: (h: unknown, context: { value?: unknown }) => unknown;
  onParentIdChange: (value: unknown) => void;
  onParentIdClear: () => void;
}>();

defineEmits<{
  (event: 'update:visible', value: boolean): void;
  (event: 'update:external-link', value: boolean): void;
  (event: 'cancel'): void;
  (event: 'confirm'): void;
}>();
</script>
