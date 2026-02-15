<template>
  <div class="ai-assistant-container">
    <t-space>
      <t-sticky-tool
        class="ai-assistant-tool"
        :style="{ position: 'relative', overflow: 'hidden' }"
        :offset="[0, 0]"
        type="compact"
        @click="handleToolClick"
      >
        <t-sticky-item
          v-if="!isMobile"
          label="二维码"
          :popup="renderPopup"
          :popup-props="{ overlayInnerStyle: { padding: '8px' } }"
        >
          <template #icon><qrcode-icon /></template>
        </t-sticky-item>
        <t-sticky-item label="回到顶部">
          <template #icon><arrow-up-icon /></template>
        </t-sticky-item>
      </t-sticky-tool>
    </t-space>
  </div>
</template>
<script setup lang="tsx">
import { ArrowUpIcon, QrcodeIcon } from 'tdesign-icons-vue-next';
import { onMounted, onUnmounted, ref } from 'vue';

import { prefix } from '@/config/global';
import { useSettingStore } from '@/store';

const settingStore = useSettingStore();
const isMobile = ref(false);
const COMPACT_BREAKPOINT = 900;

const updateIsMobile = () => {
  if (typeof window === 'undefined') return;
  isMobile.value = window.innerWidth <= COMPACT_BREAKPOINT;
};

const renderPopup = () => (
  <div class="ai-qr-popup" style={{ width: '140px', padding: '4px' }}>
    {settingStore.qrCodeUrl ? (
      <img
        class="ai-qr-popup__image"
        src={settingStore.qrCodeUrl}
        alt="二维码"
        style={{ width: '128px', height: '128px', objectFit: 'contain', display: 'block', margin: '0 auto' }}
      />
    ) : (
      <div class="ai-qr-popup__placeholder">
        <div class="ai-qr-popup__title">未配置二维码</div>
        <div class="ai-qr-popup__desc">请在系统设置 / 个性化设置 / 图片设置中配置</div>
      </div>
    )}
  </div>
);

// 处理工具栏点击事件
const handleToolClick = (context: { e: MouseEvent; item: any }) => {
  const label = typeof context.item?.label === 'string' ? context.item.label : '';

  if (label === '回到顶部') {
    handleScrollTop();
  }
};

const handleScrollTop = () => {
  // 多个候选滚动容器（按优先级）
  const selectors = [`.${prefix}-layout`, `.${prefix}-content-layout`, '.t-layout', '.t-layout__content'];

  // 1. 优先查找当前有滚动位置的容器
  for (const selector of selectors) {
    const el = document.querySelector(selector) as HTMLElement | null;
    if (el && el.scrollTop > 0) {
      el.scrollTo({ top: 0, behavior: 'smooth' });
      return;
    }
  }

  // 2. 查找可滚动的容器
  for (const selector of selectors) {
    const el = document.querySelector(selector) as HTMLElement | null;
    if (el && el.scrollHeight > el.clientHeight) {
      el.scrollTo({ top: 0, behavior: 'smooth' });
      return;
    }
  }

  // 3. 回退到window滚动
  window.scrollTo({ top: 0, behavior: 'smooth' });
};

onMounted(() => {
  updateIsMobile();
  window.addEventListener('resize', updateIsMobile);
});

onUnmounted(() => {
  window.removeEventListener('resize', updateIsMobile);
});
</script>
<style lang="less" scoped>
.ai-assistant-container {
  position: fixed;
  /* 距离底部的距离，数值越大越靠上 */
  bottom: max(24px, env(safe-area-inset-bottom));
  /* 距离右侧的距离，数值越大越靠左，负值则向右超出屏幕 */
  right: -47px;
  z-index: 2000;
  max-width: calc(100vw - 16px);
}

.ai-assistant-tool {
  :deep(.t-sticky-tool__item) {
    width: 52px;
    height: 52px;
    border-radius: 14px 0 0 14px;
    background: var(--td-bg-color-container);
    box-shadow: 0 6px 16px rgb(0 0 0 / 12%);
    border: 1px solid var(--td-component-stroke);
    margin: 8px 0;
    transition: all 0.2s ease;

    &:hover {
      transform: translateX(-2px);
      color: var(--td-brand-color);
      border-color: var(--td-brand-color);
    }
  }

  :deep(.t-sticky-tool__item__icon) {
    font-size: 20px;
  }

  :deep(.t-sticky-tool__item__label) {
    font-size: 12px;
  }
}

.ai-qr-popup {
  text-align: center;
  min-width: 140px;
}

.ai-qr-popup__image {
  width: 128px;
  height: 128px;
  object-fit: contain;
  display: block;
  margin: 0 auto;
}

.ai-qr-popup__placeholder {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 6px 4px;
  color: var(--td-text-color-secondary);
}

.ai-qr-popup__title {
  font-weight: 600;
  color: var(--td-text-color-primary);
}

.ai-qr-popup__desc {
  font-size: 12px;
  line-height: 1.4;
}

@media (max-width: 900px) {
  .ai-assistant-container {
    right: -6px;
    bottom: max(12px, env(safe-area-inset-bottom));
  }

  .ai-assistant-tool {
    :deep(.t-sticky-tool__item) {
      width: 44px;
      height: 44px;
      border-radius: 12px;
      margin: 6px 0;
    }

    :deep(.t-sticky-tool__item__icon) {
      font-size: 18px;
    }
  }
}

@media (max-width: 640px) {
  .ai-assistant-tool {
    :deep(.t-sticky-tool__item) {
      width: 40px;
      height: 40px;
      border-radius: 10px;
    }
  }
}
</style>
