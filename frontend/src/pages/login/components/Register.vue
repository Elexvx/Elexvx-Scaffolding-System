<template>
  <register-form-panel @register-success="$emit('register-success')" @back="$emit('back')" />
</template>

<script setup lang="ts">
/**
 * @deprecated 新开发应使用 views/login/components/RegisterFormPanel.vue
 */
import RegisterFormPanel from '@/views/login/components/RegisterFormPanel.vue';

defineEmits(['register-success', 'back']);
defineOptions({ name: 'LegacyRegisterPanel' });
</script>
