<template>
  <t-card :bordered="false" class="profile-summary-card" :loading="loading">
    <div class="profile-summary-card__body">
      <div class="profile-summary-card__left">
        <t-upload
          v-if="enableAvatarUpload"
          :action="uploadAction"
          name="file"
          :show-file-list="false"
          :headers="uploadHeaders"
          theme="custom"
          @success="$emit('avatar-success', $event)"
          @fail="$emit('avatar-fail', $event)"
        >
          <t-avatar :size="avatarSize" :image="avatar" class="profile-summary-card__avatar">
            <template v-if="!avatar" #icon>
              <t-icon name="user" />
            </template>
            <div class="profile-summary-card__avatar-overlay"><t-icon name="edit" /></div>
          </t-avatar>
        </t-upload>
        <t-avatar v-else :size="avatarSize" :image="avatar" class="profile-summary-card__avatar">
          <template v-if="!avatar" #icon>
            <t-icon name="user" />
          </template>
        </t-avatar>

        <div class="profile-summary-card__meta">
          <div class="profile-summary-card__title-row">
            <div class="profile-summary-card__name">{{ name || '-' }}</div>
            <t-tag size="small" variant="light">{{ roleTag || 'admin' }}</t-tag>
          </div>
          <div class="profile-summary-card__contact-row">
            <span v-for="item in visibleContacts" :key="item" class="profile-summary-card__contact-item">{{
              item
            }}</span>
            <t-popup v-if="hiddenContacts.length > 0" trigger="click" placement="bottom-right">
              <t-link theme="primary" hover="color">更多</t-link>
              <template #content>
                <div class="profile-summary-card__more-contact">
                  <div v-for="item in hiddenContacts" :key="item">{{ item }}</div>
                </div>
              </template>
            </t-popup>
          </div>
        </div>
      </div>
    </div>
  </t-card>
</template>
<script setup lang="ts">
import { computed } from 'vue';

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

const visibleContacts = computed(() => props.contacts.slice(0, 2));
const hiddenContacts = computed(() => props.contacts.slice(2));
</script>
<style lang="less" scoped>
.profile-summary-card {
  :deep(.t-card__body) {
    padding: 18px;
  }

  &__body {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
  }

  &__left {
    display: flex;
    align-items: center;
    gap: 12px;
    min-width: 0;
  }

  &__avatar {
    flex: 0 0 auto;
    border: 2px solid var(--td-bg-color-container);
    position: relative;
    cursor: pointer;
  }

  &__avatar-overlay {
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

  &__avatar:hover &__avatar-overlay {
    opacity: 1;
  }

  &__meta {
    min-width: 0;
  }

  &__title-row {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    max-width: 100%;
  }

  &__name {
    font-size: 16px;
    font-weight: 600;
    color: var(--td-text-color-primary);
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  &__contact-row {
    margin-top: 6px;
    display: flex;
    align-items: center;
    gap: 8px;
    flex-wrap: wrap;
    color: var(--td-text-color-secondary);
    font-size: 13px;
    line-height: 20px;
  }

  &__contact-item {
    max-width: 220px;
    white-space: nowrap;
    text-overflow: ellipsis;
    overflow: hidden;
  }

  &__more-contact {
    width: 220px;
    padding: 8px 12px;
    color: var(--td-text-color-secondary);
    line-height: 22px;
  }

  @media (max-width: 767px) {
    :deep(.t-card__body) {
      padding: 16px;
    }

    &__body {
      align-items: flex-start;
      flex-direction: column;
    }

    &__contact-item {
      max-width: 180px;
    }
  }
}
</style>
