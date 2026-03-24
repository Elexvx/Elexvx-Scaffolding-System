<template>
  <t-popup expand-animation placement="bottom-right" trigger="click" @visible-change="handlePopupVisibleChange">
    <template #content>
      <div class="header-msg">
        <div class="header-msg-top">
          <p>{{ t('layout.notice.title') }}</p>
          <t-button
            v-if="activeTab === 'message' && currentUnreadCount > 0"
            class="clear-btn"
            variant="text"
            theme="primary"
            @click="setRead('all')"
          >
            {{ t('layout.notice.clear') }}
          </t-button>
        </div>

        <t-tabs v-model="activeTab" class="msg-tabs">
          <t-tab-panel value="message" :label="t('layout.notice.messages')" />
          <t-tab-panel value="announcement" :label="t('layout.notice.announcements')" />
        </t-tabs>

        <div v-if="activeTab === 'message'" class="msg-filter">
          <t-radio-group v-model="readStatus" variant="default-filled" size="small">
            <t-radio-button value="unread">未读</t-radio-button>
            <t-radio-button value="read">已读</t-radio-button>
            <t-radio-button value="all">全部</t-radio-button>
          </t-radio-group>
        </div>

        <t-list v-if="activeTab === 'message' && filteredMessages.length > 0" class="narrow-scrollbar" :split="false">
          <t-list-item v-for="(item, index) in filteredMessages" :key="index" :class="{ 'is-read': !item.status }">
            <div class="msg-item-container">
              <div class="msg-item-main" role="button" tabindex="0" @click="handleMessageClick(item)">
                <p class="msg-content">
                  <span class="msg-prefix message">[{{ t('layout.notice.messages') }}]</span>
                  {{ item.content }}
                </p>
                <p class="msg-time">{{ item.date }}</p>
              </div>
            </div>
            <template #action>
              <t-button v-if="item.status" size="small" variant="outline" @click.stop="setRead('radio', item)">
                {{ t('layout.notice.setRead') }}
              </t-button>
            </template>
          </t-list-item>
        </t-list>

        <t-list
          v-else-if="activeTab === 'announcement' && announcementList.length > 0"
          class="narrow-scrollbar"
          :split="false"
        >
          <t-list-item v-for="item in announcementList" :key="item.id">
            <div class="msg-item-container">
              <div class="msg-item-main" role="button" tabindex="0" @click="handleAnnouncementClick(item)">
                <p class="msg-content">
                  <span class="msg-prefix announcement">[{{ t('layout.notice.announcements') }}]</span>
                  {{ item.title }}
                </p>
                <p v-if="item.summary" class="msg-summary">{{ item.summary }}</p>
                <p class="msg-time">{{ item.publishAt || item.updatedAt }}</p>
              </div>
            </div>
          </t-list-item>
        </t-list>

        <div v-else class="empty-list">
          <img :src="emptyImage" alt="空" />
          <p>{{ t('layout.notice.empty') }}</p>
        </div>
        <div class="header-msg-bottom">
          <t-button class="header-msg-bottom-link" variant="text" theme="default" block @click="goDetail">
            {{ t('layout.notice.viewAll') }}
          </t-button>
        </div>
      </div>
    </template>
    <t-badge :count="messageUnreadCount" :offset="[4, 4]">
      <t-button theme="default" shape="square" variant="text">
        <t-icon name="mail" />
      </t-button>
    </t-badge>
  </t-popup>

  <t-dialog v-model:visible="detailVisible" :header="activeMessageTitle" :footer="false" width="520px">
    <div class="notice-detail">
      <div class="notice-detail__meta">
        <t-tag v-if="activeMessage?.type" size="small" variant="light">
          {{ activeMessage?.type === 'announcement' ? t('layout.notice.announcements') : t('layout.notice.messages') }}
        </t-tag>
        <span class="notice-detail__time">{{ activeMessage?.date }}</span>
      </div>
      <div class="notice-detail__content">{{ activeMessage?.content }}</div>
    </div>
  </t-dialog>
</template>
<script setup lang="ts">
import { storeToRefs } from 'pinia';
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

import type { AnnouncementSummary } from '@/api/announcement';
import { fetchLatestAnnouncements } from '@/api/announcement';
import emptyImage from '@/assets/assets-empty.svg?url';
import { t } from '@/locales';
import { useNotificationStore } from '@/store';
import type { NotificationItem } from '@/types/interface';

const router = useRouter();
const store = useNotificationStore();
const { msgData } = storeToRefs(store);

const activeTab = ref('message');
const readStatus = ref('unread');
const detailVisible = ref(false);
const activeMessage = ref<NotificationItem | null>(null);

const announcements = ref<AnnouncementSummary[]>([]);

const messageItems = computed(() => msgData.value.filter((item) => item.type !== 'announcement'));

const refreshNotice = async () => {
  await store.fetchMessages();
  await loadAnnouncements();
};

const handlePopupVisibleChange = async (visible: boolean) => {
  if (visible) {
    await refreshNotice();
  }
};

onMounted(async () => {
  await refreshNotice();
});

const filteredMessages = computed(() => {
  return messageItems.value.filter((item: NotificationItem) => {
    if (readStatus.value === 'all') return true;
    if (readStatus.value === 'unread') return item.status;
    return !item.status;
  });
});

const announcementList = computed(() => announcements.value);

const messageUnreadCount = computed(() => messageItems.value.filter((item) => item.status).length);

const currentUnreadCount = computed(() => {
  return filteredMessages.value.filter((i) => i.status).length;
});

const loadAnnouncements = async () => {
  try {
    announcements.value = await fetchLatestAnnouncements(6);
  } catch {
    announcements.value = [];
  }
};

const activeMessageTitle = computed(() => {
  if (!activeMessage.value) return `${t('layout.notice.messages')}??`;
  if (activeMessage.value.type === 'announcement') return `${t('layout.notice.announcements')}??`;
  return `${t('layout.notice.messages')}??`;
});

const setRead = async (type: string, item?: NotificationItem) => {
  if (type === 'all') {
    const targets = filteredMessages.value.filter((i) => i.status);
    for (const target of targets) {
      await store.markRead(target.id, true);
    }
    return;
  }
  if (item?.id) {
    await store.markRead(item.id, true);
  }
};

const handleAnnouncementClick = (item: AnnouncementSummary) => {
  router.push({ path: '/announcement/cards', query: { id: item.id, tab: 'announcement' } });
};

const handleMessageClick = async (item: NotificationItem) => {
  activeMessage.value = item;
  detailVisible.value = true;
  if (item.status) {
    await setRead('radio', item);
  }
};

const goDetail = () => {
  router.push({ path: '/announcement/cards', query: { tab: activeTab.value } });
};
</script>
<style lang="less" scoped>
.header-msg {
  width: 420px;
  margin: calc(0px - var(--td-comp-paddingTB-xs)) calc(0px - var(--td-comp-paddingLR-s));
  background: var(--td-bg-color-container);

  .empty-list {
    text-align: center;
    padding: var(--td-comp-paddingTB-xxl) 0;
    font: var(--td-font-body-medium);
    color: var(--td-text-color-secondary);

    img {
      width: var(--td-comp-size-xxxxl);
    }

    p {
      margin-top: var(--td-comp-margin-xs);
    }
  }

  .header-msg-top {
    position: relative;
    font: var(--td-font-title-medium);
    color: var(--td-text-color-primary);
    text-align: left;
    padding: var(--td-comp-paddingTB-l) var(--td-comp-paddingLR-xl) var(--td-comp-paddingTB-s);
    display: flex;
    align-items: center;
    justify-content: space-between;

    .clear-btn {
      right: calc(var(--td-comp-paddingTB-l) - var(--td-comp-paddingLR-xl));
    }
  }

  .msg-tabs {
    padding: 0 var(--td-comp-paddingLR-xl);
    margin-top: 4px;

    :deep(.t-tabs__header) {
      padding: 0;
    }
  }

  .msg-filter {
    padding: var(--td-comp-paddingTB-s) var(--td-comp-paddingLR-xl) var(--td-comp-paddingTB-m);
    display: flex;
    justify-content: flex-end;
  }

  &-bottom {
    align-items: center;
    display: flex;
    justify-content: center;
    padding: var(--td-comp-paddingTB-s) var(--td-comp-paddingLR-s);
    border-top: 1px solid var(--td-component-stroke);

    &-link {
      text-decoration: none;
      cursor: pointer;
      color: var(--td-text-color-placeholder);
    }
  }

  .t-list {
    height: 360px; // 固定高度，支持滚动
    padding: var(--td-comp-margin-s);
  }

  .t-list-item {
    position: relative;
    display: flex;
    align-items: flex-start;
    gap: var(--td-comp-margin-l);
    overflow: hidden;
    width: 100%;
    padding: var(--td-comp-paddingTB-m) var(--td-comp-paddingLR-l);
    border-radius: var(--td-radius-default);
    font: var(--td-font-body-medium);
    color: var(--td-text-color-primary);
    cursor: pointer;
    transition:
      background-color 0.2s linear,
      box-shadow 0.2s ease;

    &.is-read {
      opacity: 0.6;
    }

    &:hover {
      background-color: var(--td-bg-color-container-hover);

      .msg-content {
        color: var(--td-brand-color);
      }

      .t-list-item__action {
        button {
          opacity: 1;
          transform: translateY(0);
          pointer-events: auto;
        }
      }
    }

    .msg-item-container {
      flex: 1;
      min-width: 0;
      width: 100%;
    }

    .msg-item-main {
      display: flex;
      flex-direction: column;
      gap: 6px;
      cursor: pointer;
      outline: none;
    }

    .msg-content {
      margin: 0;
      line-height: 1.5;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
      word-break: break-all;
    }

    .msg-summary {
      margin: 0;
      line-height: 1.4;
      color: var(--td-text-color-secondary);
      font-size: 12px;
    }

    .msg-prefix {
      font-weight: bold;
      margin-right: 4px;

      &.announcement {
        color: var(--td-warning-color);
      }

      &.notification {
        color: var(--td-brand-color);
      }

      &.message {
        color: var(--td-brand-color);
      }
    }

    .t-list-item__action {
      align-self: center;
      margin-left: auto;

      button {
        opacity: 0;
        transform: translateY(4px);
        transition:
          opacity 0.2s ease,
          transform 0.2s ease;
        pointer-events: none;
      }
    }

    .msg-time {
      margin: 0;
      align-self: flex-end;
      text-align: right;
      color: var(--td-text-color-secondary);
      font-size: 12px;
    }
  }
}

.notice-detail {
  display: flex;
  flex-direction: column;
  gap: 12px;

  &__meta {
    display: flex;
    align-items: center;
    gap: 8px;
    color: var(--td-text-color-secondary);
  }

  &__time {
    font-size: 12px;
  }

  &__content {
    white-space: pre-wrap;
    line-height: 1.6;
    color: var(--td-text-color-primary);
  }
}
</style>
