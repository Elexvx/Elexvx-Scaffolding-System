<template>
  <div v-if="show" class="wm-overlay" :style="styleObj"></div>
</template>
<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';

import { useSettingStore } from '@/store';

const settingStore = useSettingStore();
const enabled = computed(() => !!settingStore.watermark?.enabled);
const route = useRoute();
const authPages = new Set(['/login', '/register', '/forgot']);
const show = computed(() => enabled.value && !authPages.has(route.path));

const genDataUrl = async () => {
  const cfg = settingStore.watermark;
  if (!cfg) return '';
  const canvas = document.createElement('canvas');
  const size = cfg.size || 120;
  canvas.width = cfg.gapX || 200;
  canvas.height = cfg.gapY || 200;
  const ctx = canvas.getContext('2d')!;
  ctx.globalAlpha = cfg.opacity || 0.12;
  ctx.translate(canvas.width / 2, canvas.height / 2);
  ctx.rotate(((cfg.rotate || 20) * Math.PI) / 180);
  ctx.translate(-canvas.width / 2, -canvas.height / 2);

  if (cfg.type === 'image_gray' && cfg.imageUrl) {
    const img = new Image();
    img.crossOrigin = 'anonymous';
    const promise: Promise<string> = new Promise((resolve) => {
      img.onload = () => {
        const w = size;
        const h = size;
        ctx.drawImage(img, (canvas.width - w) / 2, (canvas.height - h) / 2, w, h);
        const imgData = ctx.getImageData(0, 0, canvas.width, canvas.height);
        for (let i = 0; i < imgData.data.length; i += 4) {
          const r = imgData.data[i];
          const g = imgData.data[i + 1];
          const b = imgData.data[i + 2];
          const y = 0.299 * r + 0.587 * g + 0.114 * b;
          imgData.data[i] = imgData.data[i + 1] = imgData.data[i + 2] = y;
        }
        ctx.putImageData(imgData, 0, 0);
        resolve(canvas.toDataURL('image/png'));
      };
      img.onerror = () => resolve('');
    });
    img.src = cfg.imageUrl;
    return await promise;
  }

  ctx.fillStyle = '#000';
  ctx.font = `${size}px sans-serif`;
  ctx.textAlign = 'center';
  ctx.textBaseline = 'middle';
  const text = cfg.content || '';
  if (cfg.type === 'text_multi') {
    const lines = String(text).split(/\r?\n/).slice(0, 3);
    const lineHeight = size + 6;
    const startY = (canvas.height - lineHeight * lines.length) / 2;
    lines.forEach((line: string, idx: number) => {
      ctx.fillText(line, canvas.width / 2, startY + idx * lineHeight);
    });
  } else {
    ctx.fillText(text, canvas.width / 2, canvas.height / 2);
  }
  return canvas.toDataURL('image/png');
};

const dataUrl = ref('');
const styleObj = computed(() => {
  return {
    backgroundImage: `url(${dataUrl.value})`,
    backgroundRepeat: 'repeat',
  } as Record<string, string>;
});

const load = async () => {
  dataUrl.value = await genDataUrl();
};

onMounted(load);
watch(
  () => settingStore.displayMode,
  async () => {
    dataUrl.value = await genDataUrl();
  },
);

watch(
  () => settingStore.watermark,
  async () => {
    await load();
  },
  { deep: true },
);
</script>
<style scoped>
.wm-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
  pointer-events: none;
}
</style>
