<template>
  <confirm-drawer :visible="visible" :header="title" size="760px" @update:visible="emit('update:visible', $event)">
    <t-form ref="innerFormRef" class="drawer-form--single" :data="form" :rules="rules" label-width="120px" layout="vertical" label-align="right" @submit="emit('submit', $event)">
      <t-row :gutter="[24, 24]">
        <t-col :xs="24">
          <t-form-item label="上级机构" name="parentId">
            <org-parent-selector v-model="form.parentId" :data="orgTree" :tree-keys="orgTreeKeys" />
          </t-form-item>
        </t-col>
        <t-col :xs="24" :sm="12">
          <t-form-item label="机构名称" name="name">
            <t-input v-model="form.name" placeholder="请输入机构名称" />
          </t-form-item>
        </t-col>
        <t-col :xs="24" :sm="12">
          <t-form-item label="机构简称" name="shortName">
            <t-input v-model="form.shortName" placeholder="请输入机构简称" />
          </t-form-item>
        </t-col>
        <t-col :xs="24" :sm="12">
          <t-form-item label="机构类型" name="type">
            <t-select v-model="form.type" :options="typeOptions" placeholder="请选择机构类型" />
          </t-form-item>
        </t-col>
        <t-col :xs="24">
          <t-form-item label="负责人" name="leaderIds">
            <t-input :model-value="leaderDisplay" readonly placeholder="请选择部门领导" @click="emit('open-leader-dialog')">
              <template #suffixIcon>
                <t-icon name="user" />
              </template>
            </t-input>
          </t-form-item>
        </t-col>
        <t-col v-if="editingId" :xs="24">
          <t-form-item label="加入用户">
            <t-input :model-value="memberDisplay" readonly placeholder="选择用户加入该机构" @click="emit('open-member-dialog')">
              <template #suffixIcon>
                <t-icon name="user" />
              </template>
            </t-input>
          </t-form-item>
        </t-col>
        <t-col :xs="24" :sm="12">
          <t-form-item label="联系电话" name="phone">
            <t-input v-model="form.phone" placeholder="请输入联系电话" />
          </t-form-item>
        </t-col>
        <t-col :xs="24" :sm="12">
          <t-form-item label="邮箱" name="email">
            <t-input v-model="form.email" placeholder="请输入邮箱" />
          </t-form-item>
        </t-col>
        <t-col :xs="24">
          <t-form-item label="机构状态" name="status">
            <t-radio-group v-model="form.status">
              <t-radio :value="1">正常</t-radio>
              <t-radio :value="0">停用</t-radio>
            </t-radio-group>
          </t-form-item>
        </t-col>
      </t-row>
    </t-form>
    <template #footer>
      <t-space class="tdesign-starter-action-bar">
        <t-button variant="outline" @click="emit('update:visible', false)">取消</t-button>
        <t-button theme="primary" :loading="saving" @click="handleConfirm">确定</t-button>
      </t-space>
    </template>
  </confirm-drawer>
</template>

<script setup lang="ts">
import type { FormInstanceFunctions, FormRule, SelectOption, SubmitContext, TreeProps } from 'tdesign-vue-next';
import { ref } from 'vue';

import ConfirmDrawer from '@/components/ConfirmDrawer.vue';

import type { OrgFormModel, OrgUnitNode } from '../types';
import OrgParentSelector from './OrgParentSelector.vue';

defineProps<{
  visible: boolean;
  title: string;
  form: OrgFormModel;
  rules: Record<string, FormRule[]>;
  orgTree: OrgUnitNode[];
  orgTreeKeys: TreeProps['keys'];
  typeOptions: SelectOption[];
  editingId: number | null;
  leaderDisplay: string;
  memberDisplay: string;
  saving: boolean;
}>();

const emit = defineEmits<{
  'update:visible': [boolean];
  submit: [SubmitContext];
  'open-leader-dialog': [];
  'open-member-dialog': [];
}>();

const innerFormRef = ref<FormInstanceFunctions>();

const handleConfirm = () => {
  innerFormRef.value?.submit();
};
</script>
