<template>
  <t-dialog :visible="visible" placement="center" header="修改密码" width="560px" :confirm-btn="null" :cancel-btn="null" @update:visible="$emit('update:visible', $event)">
    <t-form
      ref="passwordFormRef"
      class="password-form"
      :data="passwordForm"
      :rules="passwordRules"
      label-align="right"
      label-width="120px"
      @submit="$emit('submit', $event)"
    >
      <t-form-item label="当前密码" name="oldPassword">
        <t-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入当前密码" />
      </t-form-item>
      <t-form-item label="新密码" name="newPassword">
        <t-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" />
      </t-form-item>
      <t-form-item label="确认新密码" name="confirmPassword">
        <t-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" />
      </t-form-item>
      <t-form-item class="form-submit" label-width="0">
        <t-space>
          <t-button variant="outline" @click="$emit('update:visible', false)">取消</t-button>
          <t-button theme="primary" type="submit" :loading="loading">修改密码</t-button>
        </t-space>
      </t-form-item>
    </t-form>
  </t-dialog>
</template>

<script setup lang="ts">
import type { FormInstanceFunctions, FormRule, SubmitContext } from 'tdesign-vue-next';
import { ref } from 'vue';

import type { PasswordFormModel } from '../types';

defineProps<{
  visible: boolean;
  loading: boolean;
  passwordForm: PasswordFormModel;
  passwordRules: Record<string, FormRule[]>;
}>();

defineEmits<{
  (event: 'update:visible', visible: boolean): void;
  (event: 'submit', context: SubmitContext): void;
}>();

const passwordFormRef = ref<FormInstanceFunctions>();
</script>

<style scoped lang="less">
.password-form {
  :deep(.t-form__controls) {
    min-width: 0;
    flex: 1;
  }

  :deep(.t-input) {
    width: 100%;
  }
}

.form-submit {
  :deep(.t-form__controls-content) {
    display: flex;
    justify-content: flex-end;
  }
}
</style>
