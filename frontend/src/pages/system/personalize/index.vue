<template>
  <div class="wm-container">
    <t-card title="个性化设置" :bordered="false">
      <t-tabs v-model="activeTab">
        <t-tab-panel value="personalize" label="个性化设置" :destroy-on-hide="false">
          <div class="tab-content">
            <personalize-panel v-if="activeTab === 'personalize'" />
          </div>
        </t-tab-panel>
        <t-tab-panel value="images" label="图片设置" :destroy-on-hide="false">
          <div class="tab-content">
            <image-panel v-if="activeTab === 'images'" />
          </div>
        </t-tab-panel>
        <t-tab-panel value="version" label="版本设置" :destroy-on-hide="false">
          <div class="tab-content">
            <version-panel v-if="activeTab === 'version'" />
          </div>
        </t-tab-panel>
        <t-tab-panel value="watermark" label="水印设置" :destroy-on-hide="false">
          <div class="tab-content">
            <watermark-panel v-if="activeTab === 'watermark'" />
          </div>
        </t-tab-panel>
        <t-tab-panel value="copyright" label="版权设置" :destroy-on-hide="false">
          <div class="tab-content">
            <copyright-panel v-if="activeTab === 'copyright'" />
          </div>
        </t-tab-panel>
        <t-tab-panel value="agreement" label="协议设置" :destroy-on-hide="false">
          <div class="tab-content">
            <agreement-panel v-if="activeTab === 'agreement'" />
          </div>
        </t-tab-panel>
      </t-tabs>
    </t-card>
  </div>
</template>
<script setup lang="ts">
import { defineAsyncComponent, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();
const validTabs = new Set(['personalize', 'images', 'version', 'watermark', 'copyright', 'agreement']);

const resolveTab = (value: unknown) => {
  const tab = typeof value === 'string' ? value : '';
  return validTabs.has(tab) ? tab : 'personalize';
};

const activeTab = ref(resolveTab(route.query.tab));

watch(
  () => route.query.tab,
  (value) => {
    const next = resolveTab(value);
    if (next !== activeTab.value) {
      activeTab.value = next;
    }
  },
);

watch(
  activeTab,
  (value) => {
    if (route.query.tab === value) return;
    router.replace({ query: { ...route.query, tab: value } }).catch(() => {});
  },
  { immediate: true },
);

// Lazy-load components
const PersonalizePanel = defineAsyncComponent(() => import('./components/PersonalizePanel.vue'));
const ImagePanel = defineAsyncComponent(() => import('./components/ImagePanel.vue'));
const CopyrightPanel = defineAsyncComponent(() => import('./components/CopyrightPanel.vue'));
const VersionPanel = defineAsyncComponent(() => import('./components/VersionPanel.vue'));
const AgreementPanel = defineAsyncComponent(() => import('./components/AgreementPanel.vue'));
const WatermarkPanel = defineAsyncComponent(() => import('./components/WatermarkPanel.vue'));
</script>
<style lang="less" scoped>
@import '../watermark/index.less';

.wm-container {
  :deep(.t-card__body) {
    padding: 8px 24px 24px;
  }
}

.tab-content {
  padding-top: 24px;
}
</style>
