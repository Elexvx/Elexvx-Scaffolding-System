<template>
  <agreement-check v-model="model" />
</template>

<script setup lang="ts">
import AgreementCheck from '@/pages/login/components/AgreementCheck.vue';

const model = defineModel<boolean>({ default: false });
</script>
