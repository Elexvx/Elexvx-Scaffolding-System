<template>
  <login-form-panel @register="$emit('register')" @forgot="$emit('forgot')" />
</template>

<script setup lang="ts">
import LoginFormPanel from '@/views/login/components/LoginFormPanel.vue';

defineEmits(['register', 'forgot']);
defineOptions({ name: 'LegacyLoginPanel' });
</script>
