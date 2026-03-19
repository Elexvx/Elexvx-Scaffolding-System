<template>
  <view-profile-summary-card
    v-bind="props"
    @avatar-success="$emit('avatar-success', $event)"
    @avatar-fail="$emit('avatar-fail', $event)"
  />
</template>

<script setup lang="ts">
import ViewProfileSummaryCard from '@/views/user/components/ProfileSummaryCard.vue';

const props = withDefaults(
  defineProps<{
    loading?: boolean;
    avatar?: string;
    name?: string;
    roleTag?: string;
    contacts?: string[];
    uploadHeaders?: Record<string, string>;
    uploadAction?: string;
    enableAvatarUpload?: boolean;
    avatarSize?: string;
  }>(),
  {
    loading: false,
    avatar: '',
    name: '',
    roleTag: 'admin',
    contacts: () => [],
    uploadAction: '/api/system/file/upload?page=user-profile',
    uploadHeaders: () => ({}),
    enableAvatarUpload: true,
    avatarSize: '52px',
  },
);

defineEmits<{
  (e: 'avatar-success', ctx: unknown): void;
  (e: 'avatar-fail', ctx: unknown): void;
}>();
</script>
