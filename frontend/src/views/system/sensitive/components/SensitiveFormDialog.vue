<template>
  <t-dialog :visible="visible" placement="center" header="新增敏感词" width="520px" :confirm-btn="null" :cancel-btn="null" @update:visible="$emit('update:visible', $event)">
    <t-form :data="wordCreateForm" :rules="rules" label-align="right" label-width="88px" @submit="$emit('submit', $event)">
      <t-form-item label="敏感词" name="word">
        <t-input v-model="wordCreateForm.word" placeholder="请输入敏感词" />
      </t-form-item>
      <t-form-item label-width="0">
        <t-space style="width: 100%; justify-content: flex-end">
          <t-button variant="outline" @click="$emit('update:visible', false)">取消</t-button>
          <t-button theme="primary" :loading="savingWord" @click="$emit('confirm')">确定</t-button>
        </t-space>
      </t-form-item>
    </t-form>
  </t-dialog>
</template>

<script setup lang="ts">
import type { FormRule, SubmitContext } from 'tdesign-vue-next';

import type { SensitiveWordCreateForm } from '../types';

defineProps<{
  visible: boolean;
  wordCreateForm: SensitiveWordCreateForm;
  rules: Record<string, FormRule[]>;
  savingWord: boolean;
}>();

defineEmits<{
  (event: 'update:visible', value: boolean): void;
  (event: 'submit', context: SubmitContext): void;
  (event: 'confirm'): void;
}>();
</script>
