<template>
  <register-form-panel @register-success="$emit('register-success')" @back="$emit('back')" />
</template>

<script setup lang="ts">
import RegisterFormPanel from '@/views/login/components/RegisterFormPanel.vue';

defineEmits(['register-success', 'back']);
defineOptions({ name: 'LegacyRegisterPanel' });
</script>
