<template><t-form-item><t-button theme="primary" :loading="saving" :disabled="!canEdit" @click="$emit('save')">保存设置</t-button></t-form-item></template>
<script setup lang="ts">defineProps<{ saving: boolean; canEdit: boolean }>(); defineEmits(['save']);</script>
