<template>
  <div class="login-right-container">
    <div class="login-bg-image" :style="bgStyle"></div>
  </div>
</template>

<script setup lang="ts">
defineProps<{ bgStyle: Record<string, string> }>();
</script>

<style scoped lang="less">
.login-right-container {
  flex: 2;
  position: relative;
  overflow: hidden;

  .login-bg-image {
    width: 100%;
    height: 100%;
    transition: all 0.3s;
  }
}

/* Light/Dark mode adaptations for right image area */
:global(.light) .login-right-container {
  background-color: var(--td-brand-color-1);
}

:global(.dark) .login-right-container {
  background-color: var(--td-bg-color-secondarycontainer);

  /* Add a dim overlay for dark mode */
  &::after {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgb(0 0 0 / 40%);
  }
}

@media screen and (width <= 992px) {
  .login-right-container {
    display: none;
  }
}
</style>
