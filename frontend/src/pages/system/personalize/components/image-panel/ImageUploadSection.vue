<template>
  <t-col :xs="24" :sm="12">
    <t-form-item :label="label" :name="field" :help="help">
      <t-space direction="vertical" :size="8" class="image-panel__url-wrap">
        <div class="image-panel__url-row">
          <t-input v-model="model" class="image-panel__url-input" :placeholder="placeholder" :disabled="disabled" />
          <slot name="upload" />
        </div>
        <div v-if="model" class="logo-preview-container">
          <img :src="model" :alt="`${label} Preview`" class="logo-preview-img" :style="previewStyle" />
        </div>
      </t-space>
    </t-form-item>
  </t-col>
</template>

<script setup lang="ts">
const model = defineModel<string>({ default: '' });
withDefaults(
  defineProps<{
    disabled?: boolean;
    field: string;
    help?: string;
    label: string;
    placeholder: string;
    previewStyle?: Record<string, string>;
  }>(),
  { disabled: false, help: '', previewStyle: () => ({ maxHeight: '40px', maxWidth: '100%', objectFit: 'contain' }) },
);
</script>

<style scoped lang="less">
.image-panel__url-wrap {
  width: 100%;
  max-width: 500px;
}

.image-panel__url-row {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 8px;
}

.image-panel__url-input {
  flex: 1;
  min-width: 0;
}

.logo-preview-container {
  margin-top: 8px;
  padding: 8px;
  border: 1px dashed var(--td-component-stroke);
  border-radius: var(--td-radius-default);
  background-color: var(--td-bg-color-container);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.logo-preview-img {
  display: block;
}
</style>
