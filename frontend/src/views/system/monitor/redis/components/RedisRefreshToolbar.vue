<template><t-space><t-button variant="outline" size="small" :disabled="!canRefresh" @click="$emit('refresh')">立即刷新</t-button></t-space></template>
<script setup lang="ts">defineProps<{ canRefresh: boolean }>(); defineEmits(['refresh']);</script>
