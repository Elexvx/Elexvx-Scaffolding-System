<template>
  <transition name="route-loading-fade">
    <div v-if="visible" class="route-loading" role="status" aria-live="polite">
      <t-loading size="large" text="加载中..." class="route-loading__spinner">
        <template #indicator>
          <t-icon name="loading" class="route-loading__icon t-icon-loading" />
        </template>
      </t-loading>
    </div>
  </transition>
</template>
<script setup lang="ts">
defineProps<{
  visible: boolean;
}>();
</script>
<style lang="less" scoped>
.route-loading {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgb(255 255 255 / 72%);
  backdrop-filter: blur(6px);
  z-index: 3000;
}

:deep(.route-loading__spinner) {
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

:deep(.route-loading__icon) {
  font-size: 1em;
}

:global(html[theme-mode='dark']) .route-loading {
  background: rgb(0 0 0 / 45%);
}

.route-loading-fade-enter-active,
.route-loading-fade-leave-active {
  transition: opacity 0.2s ease;
}

.route-loading-fade-enter-from,
.route-loading-fade-leave-to {
  opacity: 0;
}
</style>
