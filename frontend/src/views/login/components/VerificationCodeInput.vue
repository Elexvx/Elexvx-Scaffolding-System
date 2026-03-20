<template>
  <t-form-item class="verification-code" :name="name">
    <t-input v-model="model" size="large" :placeholder="placeholder" />
    <t-button size="large" variant="outline" :disabled="countDown > 0" @click="emit('send')">
      {{ countDown === 0 ? sendLabel : resendLabel }}
    </t-button>
  </t-form-item>
</template>

<script setup lang="ts">
const model = defineModel<string>({ default: '' });
defineProps<{
  countDown: number;
  name: string;
  placeholder: string;
  sendLabel: string;
  resendLabel: string;
}>();
const emit = defineEmits<{ send: [] }>();
</script>

<style scoped lang="less">
.verification-code {
  :deep(.t-input) {
    flex: 1;
  }

  :deep(.t-button) {
    margin-left: 8px;
  }
}
</style>
