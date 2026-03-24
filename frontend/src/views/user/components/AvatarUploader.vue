<template>
  <t-upload
    :action="uploadAction"
    name="file"
    :show-file-list="false"
    :headers="headers"
    theme="custom"
    @success="$emit('success', $event)"
    @fail="$emit('fail', $event)"
  >
    <t-avatar :size="size" :image="avatar" class="avatar-uploader__avatar">
      <template v-if="!avatar" #icon>
        <t-icon name="user" />
      </template>
      <div class="avatar-uploader__overlay"><t-icon name="edit" /></div>
    </t-avatar>
  </t-upload>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    avatar?: string;
    headers?: Record<string, string>;
    uploadAction?: string;
    size?: string;
  }>(),
  {
    avatar: '',
    headers: () => ({}),
    uploadAction: '/api/system/file/upload?page=user-profile',
    size: '52px',
  },
);

defineEmits<{
  (event: 'success', context: unknown): void;
  (event: 'fail', context: unknown): void;
}>();
</script>

<style scoped lang="less">
.avatar-uploader__avatar {
  flex: 0 0 auto;
  border: 2px solid var(--td-bg-color-container);
  position: relative;
  cursor: pointer;
}

.avatar-uploader__overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: rgb(0 0 0 / 36%);
  border-radius: 50%;
  opacity: 0;
}

.avatar-uploader__avatar:hover .avatar-uploader__overlay {
  opacity: 1;
}
</style>
