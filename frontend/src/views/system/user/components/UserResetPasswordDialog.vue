<template>
  <t-dialog :visible="visible" header="重置密码" width="480px" :close-on-overlay-click="false" @update:visible="(val) => $emit('update:visible', val)">
    <t-form ref="formInstanceRef" :data="form" :rules="rules" label-width="90px" layout="vertical">
      <t-form-item label="新密码" name="password" :help="passwordHelp">
        <t-input v-model="form.password" type="password" :placeholder="passwordPlaceholder" />
      </t-form-item>
    </t-form>
    <template #footer>
      <t-space>
        <t-button variant="outline" @click="$emit('cancel')">取消</t-button>
        <t-button theme="primary" :loading="submitting" @click="triggerConfirm">确认重置</t-button>
      </t-space>
    </template>
  </t-dialog>
</template>

<script setup lang="ts">
import type { FormInstanceFunctions } from 'tdesign-vue-next';
import { ref } from 'vue';

import type { ResetPasswordModel, UserFormRules } from '../types';

defineProps<{
  visible: boolean;
  form: ResetPasswordModel;
  rules: UserFormRules;
  submitting: boolean;
  passwordHelp: string;
  passwordPlaceholder: string;
}>();

const emit = defineEmits<{
  (event: 'update:visible', value: boolean): void;
  (event: 'cancel'): void;
  (event: 'confirm'): void | Promise<void>;
}>();

const formInstanceRef = ref<FormInstanceFunctions>();

const triggerConfirm = async () => {
  const result = await formInstanceRef.value?.validate();
  if (result === true) {
    await emit('confirm');
  }
};

</script>
