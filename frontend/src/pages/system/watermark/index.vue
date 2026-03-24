<template>
  <div class="wm-container">
    <t-card title="水印设置" :bordered="false">
      <t-form :data="form" label-width="120px" :rules="rules" layout="vertical" label-align="right" @submit="onSubmit">
        <t-row :gutter="[32, 24]">
          <t-col :xs="24" :sm="12">
            <t-form-item label="类型" name="type">
              <t-radio-group v-model="form.type">
                <t-radio-button value="text_single">单行文本</t-radio-button>
                <t-radio-button value="text_multi">多行文本</t-radio-button>
                <t-radio-button value="image_gray">图片灰阶</t-radio-button>
              </t-radio-group>
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="12">
            <t-form-item v-if="form.type === 'text_single'" label="文本" name="textSingle">
              <t-input v-model="form.textSingle" placeholder="输入水印文本" style="max-width: 500px; width: 100%" />
            </t-form-item>
            <t-form-item v-if="form.type === 'text_multi'" label="文本" name="textMulti">
              <t-textarea
                v-model="form.textMulti"
                placeholder="多行文本，按换行分隔"
                style="max-width: 500px; width: 100%"
              />
            </t-form-item>
            <t-form-item v-if="form.type === 'image_gray'" label="图片地址" name="imageUrl">
              <t-input
                v-model="form.imageUrl"
                placeholder="http/https 图片地址"
                style="max-width: 500px; width: 100%"
              />
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="12">
            <t-form-item label="不透明度" name="opacity">
              <t-slider v-model="form.opacity" :min="0" :max="1" :step="0.01" style="max-width: 500px; width: 100%" />
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="12">
            <t-form-item label="字号" name="size">
              <t-input-number v-model="form.size" :min="12" :max="200" style="max-width: 500px; width: 100%" />
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="12">
            <t-form-item label="横向间距" name="gapX">
              <t-input-number v-model="form.gapX" :min="50" :max="600" style="max-width: 500px; width: 100%" />
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="12">
            <t-form-item label="纵向间距" name="gapY">
              <t-input-number v-model="form.gapY" :min="50" :max="600" style="max-width: 500px; width: 100%" />
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="12">
            <t-form-item label="旋转角度" name="rotate">
              <t-input-number v-model="form.rotate" :min="0" :max="90" style="max-width: 500px; width: 100%" />
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="12">
            <t-form-item label="启用" name="enabled"><t-switch v-model="form.enabled" /></t-form-item>
          </t-col>
          <t-col :span="24">
            <t-form-item>
              <div class="tdesign-starter-action-bar">
                <t-button type="submit" theme="primary" :disabled="!isAdmin">保存</t-button>
              </div>
            </t-form-item>
          </t-col>
        </t-row>
      </t-form>
    </t-card>
  </div>
</template>
<script setup lang="ts">
import type { FormRule, SubmitContext } from 'tdesign-vue-next';
import { MessagePlugin } from 'tdesign-vue-next';
import { computed, onMounted, reactive } from 'vue';

import { useSettingStore, useUserStore } from '@/store';
import { request } from '@/utils/request';

interface WatermarkServer {
  type: string;
  content?: string;
  imageUrl?: string;
  opacity: number;
  size: number;
  gapX: number;
  gapY: number;
  rotate: number;
  enabled: boolean;
}

const form = reactive({
  type: 'text_single',
  textSingle: '',
  textMulti: '',
  imageUrl: '',
  opacity: 0.12,
  size: 120,
  gapX: 200,
  gapY: 200,
  rotate: 20,
  enabled: false,
});

const userStore = useUserStore();
const isAdmin = computed(() => (userStore.userInfo?.roles || []).includes('admin'));

const rules: Record<string, FormRule[]> = {
  type: [{ required: true, message: '请选择类型', type: 'error' }],
  textSingle: [{ required: true, message: '请输入文本', type: 'error' }],
  textMulti: [{ required: true, message: '请输入文本', type: 'error' }],
  opacity: [{ required: true, message: '请输入不透明度', type: 'error' }],
  size: [{ required: true, message: '请输入字号', type: 'error' }],
  gapX: [{ required: true, message: '请输入横向间距', type: 'error' }],
  gapY: [{ required: true, message: '请输入纵向间距', type: 'error' }],
  rotate: [{ required: true, message: '请输入旋转角度', type: 'error' }],
};

const load = async () => {
  const data = await request.get<WatermarkServer>({ url: '/system/watermark' });
  form.type = data.type || 'text_single';
  form.imageUrl = data.imageUrl || '';
  form.opacity = data.opacity ?? form.opacity;
  form.size = data.size ?? form.size;
  form.gapX = data.gapX ?? form.gapX;
  form.gapY = data.gapY ?? form.gapY;
  form.rotate = data.rotate ?? form.rotate;
  form.enabled = data.enabled ?? form.enabled;
  if (data.type === 'text_single') {
    form.textSingle = data.content || '';
  } else if (data.type === 'text_multi') {
    form.textMulti = data.content || '';
  }
};

const onSubmit = async (ctx: SubmitContext) => {
  if (!isAdmin.value) {
    MessagePlugin.warning('Only admins can update system settings.');
    return;
  }
  if (ctx.validateResult === true) {
    const payload: WatermarkServer = {
      type: form.type,
      content: form.type === 'text_single' ? form.textSingle : form.type === 'text_multi' ? form.textMulti : '',
      imageUrl: form.imageUrl,
      opacity: form.opacity,
      size: form.size,
      gapX: form.gapX,
      gapY: form.gapY,
      rotate: form.rotate,
      enabled: form.enabled,
    };
    await request.post({ url: '/system/watermark', data: payload });
    const settingStore = useSettingStore();
    await settingStore.loadWatermarkSetting();
    MessagePlugin.success('保存成功');
  }
};

onMounted(load);
</script>
<style lang="less" scoped>
@import './index.less';
</style>
