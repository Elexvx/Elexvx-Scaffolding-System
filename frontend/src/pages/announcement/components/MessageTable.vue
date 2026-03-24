<template>
  <div class="message-table">
    <t-space style="flex-wrap: wrap; margin-bottom: 24px" size="24px">
      <t-radio-group v-model="readStatus" variant="default-filled">
        <t-radio-button value="unread">未读</t-radio-button>
        <t-radio-button value="read">已读</t-radio-button>
        <t-radio-button value="all">全部</t-radio-button>
      </t-radio-group>
      <t-button variant="outline" @click="load">刷新</t-button>
    </t-space>

    <t-table :data="filteredMessages" :columns="columns" row-key="id" :loading="loading">
      <template #empty>
        <div class="tdesign-table-empty">
          <img :src="emptyImage" alt="暂无数据" />
          <div>暂无数据</div>
        </div>
      </template>
      <template #content="{ row }">
        <span class="message-content" @click="openDetail(row)">{{ row.content }}</span>
      </template>
      <template #quality="{ row }">
        <t-tag :theme="qualityTheme(row.quality)" variant="light-outline">{{ qualityLabel(row.quality) }}</t-tag>
      </template>
      <template #status="{ row }">
        <t-tag :theme="row.status ? 'warning' : 'success'" variant="light-outline">
          {{ row.status ? '未读' : '已读' }}
        </t-tag>
      </template>
      <template #op="{ row }">
        <t-space size="small">
          <t-button size="small" variant="text" @click="openDetail(row)">查看</t-button>
          <t-button v-if="row.status" size="small" variant="text" @click="markRead(row)">标记已读</t-button>
          <t-button v-else size="small" variant="text" @click="markUnread(row)">标记未读</t-button>
        </t-space>
      </template>
    </t-table>

    <t-dialog v-model:visible="detailVisible" header="通知详情" :footer="false" width="520px">
      <div class="message-detail">
        <div class="message-detail__meta">
          <t-tag size="small" variant="light-outline">{{ qualityLabel(activeMessage?.quality) }}</t-tag>
          <t-tag size="small" variant="light-outline">{{ activeMessage?.status ? '未读' : '已读' }}</t-tag>
          <span class="message-detail__time">{{ activeMessage?.date }}</span>
        </div>
        <div class="message-detail__content">{{ activeMessage?.content }}</div>
      </div>
    </t-dialog>
  </div>
</template>
<script setup lang="ts">
import { storeToRefs } from 'pinia';
import type { PrimaryTableCol } from 'tdesign-vue-next';
import { computed, onMounted, ref } from 'vue';

import emptyImage from '@/assets/assets-empty.svg?url';
import { useNotificationStore } from '@/store';
import type { NotificationItem } from '@/types/interface';

const store = useNotificationStore();
const { msgData } = storeToRefs(store);

const loading = ref(false);
const readStatus = ref<'unread' | 'read' | 'all'>('unread');
const detailVisible = ref(false);
const activeMessage = ref<NotificationItem | null>(null);

const messageItems = computed(() => msgData.value.filter((item) => item.type !== 'announcement'));

const qualityTheme = (value?: string) => {
  switch ((value || '').toLowerCase()) {
    case 'high':
      return 'danger';
    case 'low':
      return 'primary';
    default:
      return 'warning';
  }
};

const qualityLabel = (value?: string) => {
  switch ((value || '').toLowerCase()) {
    case 'high':
      return '高';
    case 'low':
      return '低';
    default:
      return '中';
  }
};

const filteredMessages = computed(() =>
  messageItems.value.filter((item) => {
    if (readStatus.value === 'all') return true;
    if (readStatus.value === 'unread') return item.status;
    return !item.status;
  }),
);

const columns: PrimaryTableCol[] = [
  { colKey: 'content', title: '内容', cell: 'content', minWidth: 260 },
  { colKey: 'quality', title: '优先级', cell: 'quality', width: 120 },
  { colKey: 'status', title: '状态', cell: 'status', width: 120 },
  { colKey: 'date', title: '时间', width: 180 },
  { colKey: 'op', title: '操作', cell: 'op', width: 180 },
];

const load = async () => {
  loading.value = true;
  try {
    await store.fetchMessages();
  } finally {
    loading.value = false;
  }
};

const openDetail = async (row: NotificationItem) => {
  activeMessage.value = row;
  detailVisible.value = true;
  if (row.status) {
    await store.markRead(row.id, true);
  }
};

const markRead = async (row: NotificationItem) => {
  await store.markRead(row.id, true);
};

const markUnread = async (row: NotificationItem) => {
  await store.markRead(row.id, false);
};

onMounted(load);
</script>
<style scoped lang="less">
.message-table {
  .message-content {
    cursor: pointer;
    color: var(--td-text-color-primary);
  }
}

.message-detail {
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
