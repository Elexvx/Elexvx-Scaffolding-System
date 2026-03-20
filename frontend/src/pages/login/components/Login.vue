<template>
  <login-form-panel @register="$emit('register')" @forgot="$emit('forgot')" />
</template>

<script setup lang="ts">
/**
 * 注意：本组件仅为兼容壳。
 * 真实的登录面板实现已迁移至 @/views/login/components/LoginFormPanel.vue
 * 请勿在此处添加新业务逻辑，后续修改请移步至真实实现文件。
 */
import LoginFormPanel from '@/views/login/components/LoginFormPanel.vue';

defineEmits(['register', 'forgot']);
defineOptions({ name: 'LegacyLoginPanel' });
</script>
