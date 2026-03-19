<template>
  <confirm-drawer :visible="visible" :header="title" size="760px" @update:visible="(val) => $emit('update:visible', val)">
    <t-form
      ref="formInstanceRef"
      class="drawer-form--single"
      :data="form"
      :rules="rules"
      label-width="120px"
      label-align="right"
      layout="vertical"
    >
      <t-row :gutter="[24, 24]">
        <t-col :xs="24" :sm="12">
          <t-form-item label="账号" name="account">
            <t-input v-model="form.account" placeholder="如：admin" style="max-width: 500px; width: 100%" />
          </t-form-item>
        </t-col>
        <t-col :xs="24" :sm="12">
          <t-form-item label="姓名" name="name">
            <t-input v-model="form.name" placeholder="如：张三" style="max-width: 500px; width: 100%" />
          </t-form-item>
        </t-col>
        <t-col :xs="24" :sm="12">
          <t-form-item label="昵称" name="nickname">
            <t-input v-model="form.nickname" placeholder="请输入昵称" style="max-width: 500px; width: 100%" />
          </t-form-item>
        </t-col>
        <t-col :xs="24" :sm="12">
          <t-form-item label="性别" name="gender">
            <t-select v-model="form.gender" :options="genderOptions" clearable placeholder="请选择性别" style="max-width: 500px; width: 100%" />
          </t-form-item>
        </t-col>
        <t-col v-if="mode === 'create'" :xs="24" :sm="12">
          <t-form-item label="初始密码" name="password" :help="passwordHelp">
            <t-input v-model="form.password" type="password" :placeholder="passwordPlaceholder" style="max-width: 500px; width: 100%" />
          </t-form-item>
        </t-col>
        <t-col :xs="24" :sm="12">
          <t-form-item label="角色" name="roles">
            <t-select v-model="form.roles" :options="roleOptions" multiple clearable placeholder="选择角色" style="max-width: 500px; width: 100%" />
          </t-form-item>
        </t-col>
        <t-col :xs="24" :sm="12">
          <t-form-item label="所属机构" name="orgUnitIds">
            <t-tree-select
              v-model="form.orgUnitIds"
              :data="orgUnitTree"
              multiple
              clearable
              filterable
              :disabled="form.departmentIds.length > 0"
              placeholder="选择机构"
              :keys="orgTreeKeys"
              style="max-width: 500px; width: 100%"
            />
          </t-form-item>
        </t-col>
        <t-col :xs="24" :sm="12">
          <t-form-item label="所属部门" name="departmentIds">
            <t-tree-select
              v-model="form.departmentIds"
              :data="departmentTree"
              multiple
              clearable
              filterable
              :disabled="form.orgUnitIds.length > 0"
              placeholder="选择部门"
              :keys="orgTreeKeys"
              style="max-width: 500px; width: 100%"
            />
          </t-form-item>
        </t-col>
        <t-col :xs="24" :sm="12">
          <t-form-item label="状态" name="status">
            <t-radio-group v-model="form.status">
              <t-radio v-for="opt in statusRadioOptions" :key="String(opt.value)" :value="opt.value">{{ opt.label }}</t-radio>
            </t-radio-group>
          </t-form-item>
        </t-col>
        <t-col :xs="24" :sm="12">
          <t-form-item label="手机" name="mobile">
            <t-input v-model="form.mobile" placeholder="+86 138xxxx" style="max-width: 500px; width: 100%" />
          </t-form-item>
        </t-col>
        <t-col :xs="24" :sm="12">
          <t-form-item label="邮箱" name="email">
            <t-input v-model="form.email" placeholder="xxx@company.com" style="max-width: 500px; width: 100%" />
          </t-form-item>
        </t-col>
        <t-col :xs="24" :sm="12">
          <t-form-item label="省市区" name="provinceId">
            <t-cascader
              v-model="areaValueModel"
              :options="areaOptions"
              :loading="areaLoading"
              value-type="full"
              :show-all-levels="true"
              clearable
              placeholder="请选择省/市/区"
              style="max-width: 500px; width: 100%"
              @change="onAreaChange"
            />
          </t-form-item>
        </t-col>
        <t-col :xs="24" :sm="12">
          <t-form-item label="邮编" name="zipCode">
            <t-input v-model="form.zipCode" placeholder="请输入邮编" style="max-width: 500px; width: 100%" />
          </t-form-item>
        </t-col>
        <t-col :xs="24" :sm="24">
          <t-form-item label="详细地址" name="address">
            <t-input v-model="form.address" placeholder="请输入详细地址" style="max-width: 500px; width: 100%" />
          </t-form-item>
        </t-col>
        <t-col :xs="24" :sm="12">
          <t-form-item label="证件类型" name="idType">
            <t-select
              v-model="form.idType"
              :options="documentTypeOptions"
              clearable
              filterable
              placeholder="请选择证件类型"
              style="max-width: 500px; width: 100%"
            />
          </t-form-item>
        </t-col>
        <t-col :xs="24" :sm="12">
          <t-form-item label="证件号码" name="idCard">
            <t-input v-model="form.idCard" :placeholder="documentNoPlaceholder" style="max-width: 500px; width: 100%" />
          </t-form-item>
        </t-col>
        <t-col :xs="24" :sm="12">
          <t-form-item label="证件有效期起" name="idValidFrom">
            <t-date-picker v-model="form.idValidFrom" clearable format="YYYY-MM-DD" value-type="YYYY-MM-DD" style="max-width: 500px; width: 100%" />
          </t-form-item>
        </t-col>
        <t-col :xs="24" :sm="12">
          <t-form-item label="证件有效期止" name="idValidTo">
            <t-date-picker v-model="form.idValidTo" clearable format="YYYY-MM-DD" value-type="YYYY-MM-DD" style="max-width: 500px; width: 100%" />
          </t-form-item>
        </t-col>
        <t-col :xs="24" :sm="12">
          <t-form-item label="入职日期" name="joinDay">
            <t-date-picker v-model="form.joinDay" clearable format="YYYY-MM-DD" value-type="YYYY-MM-DD" style="max-width: 500px; width: 100%" />
          </t-form-item>
        </t-col>
        <t-col :xs="24" :sm="12">
          <t-form-item label="团队" name="team">
            <t-input v-model="form.team" placeholder="组织路径/团队描述" style="max-width: 500px; width: 100%" />
          </t-form-item>
        </t-col>
      </t-row>
    </t-form>

    <template #footer>
      <t-space class="tdesign-starter-action-bar">
        <t-button variant="outline" @click="$emit('cancel')">取消</t-button>
        <t-button theme="primary" :loading="saving" @click="triggerConfirm">保存</t-button>
      </t-space>
    </template>
  </confirm-drawer>
</template>

<script setup lang="ts">
import type { CascaderValue, FormInstanceFunctions, SelectOption } from 'tdesign-vue-next';
import { ref } from 'vue';

import ConfirmDrawer from '@/components/ConfirmDrawer.vue';

import type { AreaOption, Mode, OrgUnitNode, UserFormModel, UserFormRules } from '../types';

const areaValueModel = defineModel<Array<number | string>>('areaValue', { required: true });

defineProps<{
  visible: boolean;
  title: string;
  mode: Mode;
  form: UserFormModel;
  rules: UserFormRules;
  saving: boolean;
  genderOptions: SelectOption[];
  documentTypeOptions: SelectOption[];
  roleOptions: SelectOption[];
  statusRadioOptions: Array<{
    label: string;
    value: string | number | boolean;
    disabled?: boolean;
  }>;
  orgUnitTree: OrgUnitNode[];
  departmentTree: OrgUnitNode[];
  orgTreeKeys: {
    value: string;
    label: string;
    children: string;
  };
  areaOptions: AreaOption[];
  areaLoading: boolean;
  passwordHelp: string;
  passwordPlaceholder: string;
  documentNoPlaceholder: string;
}>();

const emit = defineEmits<{
  (event: 'update:visible', value: boolean): void;
  (event: 'cancel'): void;
  (event: 'confirm'): void | Promise<void>;
  (event: 'area-change', value: Array<number | string>, context: { node?: { getPath?: () => unknown[] } }): void;
}>();

const formInstanceRef = ref<FormInstanceFunctions>();

const triggerConfirm = async () => {
  const result = await formInstanceRef.value?.validate();
  if (result === true) {
    await emit('confirm');
  }
};

const onAreaChange = (value: CascaderValue, context: { node?: { getPath?: () => unknown[] } }) => {
  const normalized = Array.isArray(value) ? (value as Array<number | string>) : [];
  emit('area-change', normalized, context);
};
</script>
