<template>
  <t-dialog
    v-model:visible="visible"
    :header="title"
    :confirm-btn="{ content: '裁切并上传', loading: loading, disabled: !imageUrl }"
    :cancel-btn="{ content: '取消', theme: 'default' }"
    width="640px"
    @confirm="emit('confirm')"
  >
    <div class="logo-cropper">
      <div class="logo-cropper-frame" :style="frameStyle">
        <vue-cropper
          v-if="imageUrl"
          ref="cropperRef"
          :img="imageUrl"
          :output-size="1"
          output-type="png"
          :auto-crop="true"
          :auto-crop-width="spec?.width"
          :auto-crop-height="spec?.height"
          :fixed="true"
          :fixed-number="[spec?.width, spec?.height]"
          :center-box="true"
          :info="true"
          mode="contain"
        />
        <div v-else class="logo-cropper-empty">请先选择图片</div>
      </div>
      <div class="logo-cropper-controls">
        <div class="logo-cropper-hint">{{ hint }}</div>
      </div>
    </div>
  </t-dialog>
</template>

<script setup lang="ts">
import 'vue-cropper/dist/index.css';

import { VueCropper } from 'vue-cropper';

import type { CropSpec } from './types';

const visible = defineModel<boolean>('visible', { default: false });
defineProps<{ cropperRef: any; frameStyle: Record<string, string>; hint: string; imageUrl: string; loading: boolean; spec: CropSpec | null; title: string }>();
const emit = defineEmits<{ confirm: [] }>();
</script>

<style scoped lang="less">
.logo-cropper {
  &-frame {
    background: #ebebeb;
    border-radius: 4px;
    overflow: hidden;
  }
  &-controls {
    margin-top: 16px;
    display: flex;
    justify-content: center;
  }
  &-hint,
  &-empty {
    color: var(--td-text-color-placeholder);
    font-size: 12px;
  }
  &-empty {
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}
</style>
