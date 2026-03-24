<template>
  <div class="rich-text-editor">
    <toolbar class="rich-text-editor__toolbar" :editor="editorRef" :default-config="toolbarConfig" :mode="mode" />
    <editor
      v-model="valueHtml"
      class="rich-text-editor__editor"
      :default-config="editorConfig"
      :mode="mode"
      :style="editorStyle"
      @on-created="handleCreated"
      @on-change="handleChange"
    />
  </div>
</template>
<script setup lang="ts">
import '@wangeditor/editor/dist/css/style.css';

import { Editor, Toolbar } from '@wangeditor/editor-for-vue';
import { computed, onBeforeUnmount, ref, shallowRef, watch } from 'vue';

const props = defineProps({
  modelValue: {
    type: String,
    default: '',
  },
  placeholder: {
    type: String,
    default: '请输入内容',
  },
  minHeight: {
    type: Number,
    default: 60,
  },
  height: {
    type: Number,
    default: 0,
  },
});

const emit = defineEmits(['update:modelValue']);
const editorRef = shallowRef();
const valueHtml = ref(props.modelValue || '');
const mode = 'default';

const toolbarConfig = {};
const editorConfig = {
  placeholder: props.placeholder,
  autoFocus: false,
};

const minHeight = computed(() => `${props.minHeight}px`);
const editorStyle = computed(() => {
  const style: Record<string, string> = { minHeight: minHeight.value };
  if (props.height) {
    style.height = `${props.height}px`;
  }
  return style;
});

const handleCreated = (editor: any) => {
  editorRef.value = editor;
};

const handleChange = (editor: any) => {
  const html = editor.getHtml();
  valueHtml.value = html;
  emit('update:modelValue', html);
};

watch(
  () => props.modelValue,
  (value) => {
    const next = value || '';
    if (next !== valueHtml.value) {
      valueHtml.value = next;
    }
  },
);

onBeforeUnmount(() => {
  const editor = editorRef.value;
  if (editor) {
    editor.destroy();
  }
});
</script>
<style lang="less" scoped>
.rich-text-editor {
  --w-e-textarea-bg-color: var(--td-bg-color-container);
  --w-e-textarea-color: var(--td-text-color-primary);
  --w-e-textarea-border-color: var(--td-component-border);
  --w-e-textarea-slight-border-color: var(--td-component-border);
  --w-e-textarea-slight-color: var(--td-text-color-placeholder);
  --w-e-textarea-slight-bg-color: var(--td-bg-color-secondarycontainer);
  --w-e-textarea-selected-border-color: var(--td-brand-color);
  --w-e-textarea-handler-bg-color: var(--td-brand-color);
  --w-e-toolbar-color: var(--td-text-color-secondary);
  --w-e-toolbar-bg-color: var(--td-bg-color-container);
  --w-e-toolbar-active-color: var(--td-text-color-primary);
  --w-e-toolbar-active-bg-color: var(--td-bg-color-container-hover);
  --w-e-toolbar-disabled-color: var(--td-text-color-disabled);
  --w-e-toolbar-border-color: var(--td-component-border);
  --w-e-modal-button-bg-color: var(--td-bg-color-container);
  --w-e-modal-button-border-color: var(--td-component-border);

  border: 1px solid var(--td-component-border);
  border-radius: var(--td-radius-default);
  overflow: hidden;
  background-color: var(--td-bg-color-container);

  &__toolbar {
    border-bottom: 1px solid var(--td-component-border);
  }

  &__editor {
    resize: vertical;
    overflow-y: auto;
    min-height: v-bind(minHeight);

    :deep(.w-e-text-container) {
      background-color: var(--td-bg-color-container);
      height: 100%;
      min-height: inherit;
    }

    :deep(.w-e-text) {
      color: var(--td-text-color-primary);
      line-height: 1.6;
      padding: 0 12px 12px;
      height: 100%;
      min-height: inherit;
    }
  }
}

/* 修复全屏 z-index 问题 */
:global(.w-e-full-screen-container) {
  z-index: 9999 !important;
}
</style>
