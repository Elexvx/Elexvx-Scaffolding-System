<template>
  <div class="login-right-container">
    <div class="login-bg-image" :style="bgStyle"></div>
  </div>
</template>

<script setup lang="ts">
defineProps<{ bgStyle: Record<string, string> }>();
</script>
