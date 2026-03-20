<template>
  <forgot-password-panel @back="$emit('back')" @reset-success="$emit('reset-success')" />
</template>

<script setup lang="ts">
import ForgotPasswordPanel from '@/views/login/components/ForgotPasswordPanel.vue';

defineEmits(['back', 'reset-success']);
defineOptions({ name: 'LegacyForgotPasswordPanel' });
</script>
