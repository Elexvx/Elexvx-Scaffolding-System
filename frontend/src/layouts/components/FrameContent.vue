<template>
  <div :class="prefixCls" :style="getWrapStyle">
    <t-loading :loading="loading" size="large" :style="getWrapStyle">
      <iframe
        ref="frameRef"
        :src="resolvedFrameSrc"
        :class="`${prefixCls}__main`"
        :sandbox="iframeSandbox"
        :referrerpolicy="iframeReferrerPolicy"
        @load="hideLoading"
      ></iframe>
    </t-loading>
  </div>
</template>
<script lang="ts" setup>
import { useWindowSize } from '@vueuse/core';
import debounce from 'lodash/debounce';
import type { CSSProperties } from 'vue';
import { computed, ref, unref, watch } from 'vue';

import { prefix } from '@/config/global';
import { useSettingStore } from '@/store';
import { resolveApiHost } from '@/utils/apiHost';

const props = defineProps({
  frameSrc: {
    type: String,
    default: '',
  },
});

const { width, height } = useWindowSize();

const loading = ref(true);
const heightRef = ref(window.innerHeight);
const frameRef = ref<HTMLFrameElement>();
const prefixCls = computed(() => [`${prefix}-iframe-page`]);
const settingStore = useSettingStore();
const isProxy = import.meta.env.VITE_IS_REQUEST_PROXY === 'true';
const apiPrefixRaw = String(import.meta.env.VITE_API_URL_PREFIX || '/api');
const apiPrefix = apiPrefixRaw.startsWith('/') ? apiPrefixRaw : `/${apiPrefixRaw}`;

const LOOPBACK_HOSTS = new Set(['localhost', '127.0.0.1', '::1']);
function isLoopbackHostname(hostname: string) {
  return LOOPBACK_HOSTS.has(String(hostname || '').toLowerCase());
}

const resolvedFrameSrc = computed(() => normalizeFrameSrc(props.frameSrc));
const isExternalFrame = computed(() => /^https?:\/\//i.test(String(props.frameSrc || '').trim()));
const iframeSandbox = computed(() =>
  isExternalFrame.value ? 'allow-forms allow-scripts allow-same-origin allow-popups' : undefined,
);
const iframeReferrerPolicy = computed(() =>
  isExternalFrame.value ? 'no-referrer' : 'strict-origin-when-cross-origin',
);

function normalizeFrameSrc(src: string) {
  const raw = String(src || '').trim();
  if (!raw) return raw;
  if (!/^https?:\/\//i.test(raw) || !isProxy) return raw;
  try {
    const url = new URL(raw);
    const apiHost = resolveApiHost();
    const apiOrigin = apiHost ? new URL(apiHost).origin : '';
    const shouldLocalize =
      isLoopbackHostname(url.hostname) ||
      (typeof window !== 'undefined' && url.hostname === window.location.hostname) ||
      (apiOrigin && url.origin === apiOrigin) ||
      url.pathname.startsWith(`${apiPrefix}/`);

    if (shouldLocalize) {
      return `${url.pathname}${url.search}${url.hash}`;
    }
  } catch {
    return raw;
  }
  return raw;
}

const getWrapStyle = computed((): CSSProperties => {
  return {
    height: `${heightRef.value}px`,
  };
});

const computedStyle = getComputedStyle(document.documentElement);
const sizeXxxl = computedStyle.getPropertyValue('--td-comp-size-xxxl');
const paddingTBXxl = computedStyle.getPropertyValue('--td-comp-paddingTB-xxl');

function getOuterHeight(dom: Element) {
  let height = dom.clientHeight;
  const computedStyle = window.getComputedStyle(dom);
  height += Number.parseInt(computedStyle.marginTop, 10);
  height += Number.parseInt(computedStyle.marginBottom, 10);
  height += Number.parseInt(computedStyle.borderTopWidth, 10);
  height += Number.parseInt(computedStyle.borderBottomWidth, 10);
  return height;
}

function calcHeight() {
  const iframe = unref(frameRef);
  if (!iframe) {
    return;
  }
  let clientHeight = 0;
  const { showFooter, isUseTabsRouter, showBreadcrumb } = settingStore;
  const headerHeight = Number.parseFloat(sizeXxxl);
  const navDom = document.querySelector('.t-tabs__nav');
  const navHeight = isUseTabsRouter ? getOuterHeight(navDom) : 0;
  const breadcrumbDom = document.querySelector('.t-breadcrumb');
  const breadcrumbHeight = showBreadcrumb ? getOuterHeight(breadcrumbDom) : 0;
  const contentPadding = Number.parseFloat(paddingTBXxl) * 2;
  const footerDom = document.querySelector('.t-layout__footer');
  const footerHeight = showFooter ? getOuterHeight(footerDom) : 0;
  const top = headerHeight + navHeight + breadcrumbHeight + contentPadding + footerHeight + 2;
  heightRef.value = window.innerHeight - top;
  clientHeight = document.documentElement.clientHeight - top;
  iframe.style.height = `${clientHeight}px`;
}

function hideLoading() {
  loading.value = false;
  calcHeight();
}

// 如果窗口大小发生变化
watch([width, height], debounce(calcHeight, 250));
watch(
  [() => settingStore.showFooter, () => settingStore.isUseTabsRouter, () => settingStore.showBreadcrumb],
  debounce(calcHeight, 250),
);
</script>
<style lang="less" scoped>
@prefix-cls: ~'@{starter-prefix}-iframe-page';

.@{prefix-cls} {
  &__mask {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
  }

  &__main {
    width: 100%;
    height: 100%;
    overflow: hidden;
    background-color: #fff;
    border: 0;
    box-sizing: border-box;
  }
}
</style>
