<template>
  <div class="announcement-cards">
    <t-card :bordered="false">
      <template #title>
        <div class="card-title">
          <span>{{ t('layout.notice.title') }}</span>
        </div>
      </template>
      <t-tabs v-model="activeTab" class="notice-tabs">
        <t-tab-panel value="announcement" :label="t('layout.notice.announcements')" :destroy-on-hide="true">
          <div v-if="activeTab === 'announcement'" class="tab-content">
            <t-space style="flex-wrap: wrap; margin-bottom: 24px" size="24px">
              <t-input v-model="query.keyword" placeholder="按标题/摘要搜索" style="width: 240px" @enter="load" />
              <t-select
                v-model="query.priority"
                clearable
                :options="priorityOptions"
                placeholder="全部优先级"
                style="width: 160px"
              />
              <t-button theme="primary" @click="load">查询</t-button>
              <t-button variant="outline" @click="reset">重置</t-button>
            </t-space>

            <t-loading :loading="loading">
              <t-row v-if="list.length > 0" :gutter="[24, 24]">
                <t-col v-for="item in list" :key="item.id" :xs="12" :sm="6" :md="4" :lg="3">
                  <div class="announcement-card" @click="openDetail(item.id)">
                    <div class="card-cover">
                      <img :src="item.coverUrl || placeholderImage" alt="cover" />
                      <span class="read-time">{{ getReadTime(item.content) }}分钟 阅读</span>
                    </div>
                    <div class="card-content">
                      <t-tag size="small" variant="light" class="card-tag">{{ typeLabel(item.type) }}</t-tag>
                      <div class="card-header" :title="item.title">{{ item.title }}</div>
                      <div class="card-footer">
                        <div class="card-footer-item">
                          <browse-icon />
                          <span>{{ getViews(item.id) }}</span>
                        </div>
                        <div class="card-footer-item">
                          <chat-icon />
                          <span>{{ getComments(item.id) }}</span>
                        </div>
                        <div class="card-footer-date">{{ formatDate(item.publishAt) }}</div>
                      </div>
                    </div>
                  </div>
                </t-col>
              </t-row>
              <div v-else-if="!loading" class="tdesign-table-empty">
                <img :src="placeholderImage" alt="空" />
                <div>暂无数据</div>
              </div>
              <t-pagination
                v-if="pagination.total > pagination.pageSize"
                :page-size="pagination.pageSize"
                :total="pagination.total"
                :current="pagination.current"
                style="margin-top: 16px"
                @change="handlePageChange"
              />
            </t-loading>
          </div>
        </t-tab-panel>
        <t-tab-panel value="message" :label="t('layout.notice.messages')" :destroy-on-hide="true">
          <div v-if="activeTab === 'message'" class="tab-content">
            <message-table v-if="activeTab === 'message'" />
          </div>
        </t-tab-panel>
      </t-tabs>
    </t-card>

    <t-drawer
      v-model:visible="detailVisible"
      :close-on-overlay-click="true"
      size="760px"
      :header="current?.title || '公告详情'"
    >
      <div class="detail-meta">
        <t-space>
          <t-tag :theme="priorityTheme(current?.priority)" variant="light-outline">
            {{ priorityLabel(current?.priority) }}
          </t-tag>
          <t-tag size="small" variant="light-outline">{{ typeLabel(current?.type) }}</t-tag>
          <span class="time">发布时间：{{ current?.publishAt || '未发布' }}</span>
        </t-space>
      </div>
      <div v-if="current?.attachmentUrl" class="detail-attachments">
        <t-link :href="current?.attachmentUrl" target="_blank" rel="noopener noreferrer" theme="primary">
          {{ current?.attachmentName || '附件' }}
        </t-link>
      </div>
      <div class="detail-content" v-html="safeCurrentContent"></div>
    </t-drawer>
  </div>
</template>
<script setup lang="ts">
import { BrowseIcon, ChatIcon } from 'tdesign-icons-vue-next';
import { MessagePlugin } from 'tdesign-vue-next';
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import type { AnnouncementItem } from '@/api/announcement';
import { fetchAnnouncementDetail, fetchAnnouncements } from '@/api/announcement';
import placeholderImage from '@/assets/assets-empty.svg?url';
import { useDictionary } from '@/hooks/useDictionary';
import { t } from '@/locales';
import { buildDictOptions, resolveLabel } from '@/utils/dict';
import { sanitizeHtml } from '@/utils/sanitizeHtml';

import MessageTable from '@/pages/announcement/components/MessageTable.vue';

const list = ref<AnnouncementItem[]>([]);
const loading = ref(false);
const detailVisible = ref(false);
const current = ref<AnnouncementItem>();
const route = useRoute();
const router = useRouter();

const validTabs = new Set(['announcement', 'message']);
const resolveTab = (value: unknown) => {
  const tab = typeof value === 'string' ? value : '';
  return validTabs.has(tab) ? tab : 'announcement';
};
const activeTab = ref(resolveTab(route.query.tab));
const safeCurrentContent = computed(() => sanitizeHtml(current.value?.content || ''));

const query = reactive({
  keyword: '',
  priority: '',
  status: 'published',
});

const pagination = reactive({
  current: 1,
  pageSize: 8,
  total: 0,
});

const typeDict = useDictionary('announcement_type');
const priorityDict = useDictionary('announcement_priority');

const fallbackPriorityOptions = [
  { label: '全部', value: '' },
  { label: '高', value: 'high' },
  { label: '中', value: 'middle' },
  { label: '低', value: 'low' },
];

const priorityLabelMap: Record<string, string> = {
  high: '高',
  middle: '中',
  low: '低',
};

const typeLabelMap: Record<string, string> = {
  announcement: '公告',
};

const priorityOptions = computed(() => {
  const dictOptions = buildDictOptions(priorityDict.items.value, []);
  if (dictOptions.length === 0) return fallbackPriorityOptions;
  return [{ label: '全部', value: '' }, ...dictOptions];
});

const priorityTheme = (value?: string) => {
  switch ((value || '').toLowerCase()) {
    case 'high':
      return 'danger';
    case 'middle':
      return 'warning';
    default:
      return 'primary';
  }
};

const priorityLabel = (value?: string) => {
  return resolveLabel(value, priorityDict.items.value, priorityLabelMap);
};

const typeLabel = (value?: string) => {
  return resolveLabel(value, typeDict.items.value, typeLabelMap);
};

const getReadTime = (content?: string) => {
  if (!content) return 1;
  return Math.max(1, Math.ceil(content.length / 500));
};

const getViews = (id: number) => {
  // Mock data based on ID
  return (id * 123) % 10000;
};

const getComments = (id: number) => {
  // Mock data based on ID
  return (id * 7) % 100;
};

const formatDate = (dateStr?: string) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  const month = date.getMonth() + 1;
  const day = date.getDate();
  const weekDay = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][date.getDay()];
  return `${month}月${day}日 ${weekDay}`;
};

const load = async () => {
  loading.value = true;
  try {
    const res = await fetchAnnouncements({
      page: pagination.current - 1,
      size: pagination.pageSize,
      keyword: query.keyword,
      status: query.status,
      priority: query.priority,
    });
    list.value = res.list || [];
    pagination.total = res.total || 0;
  } finally {
    loading.value = false;
  }
};

const reset = () => {
  query.keyword = '';
  query.priority = '';
  query.status = 'published';
  pagination.current = 1;
  load();
};

const handlePageChange = ({ current, pageSize }: { current: number; pageSize: number }) => {
  pagination.current = current;
  pagination.pageSize = pageSize;
  load();
};

const openDetail = async (id: number) => {
  try {
    current.value = await fetchAnnouncementDetail(id);
    detailVisible.value = true;
  } catch (err: any) {
    MessagePlugin.error(String(err?.message || '获取详情失败'));
  }
};

const handleRouteDetail = async () => {
  if (route.query.id) {
    activeTab.value = 'announcement';
  }
  const raw = route.query.id;
  if (!raw) return;
  const id = Number(raw);
  if (Number.isNaN(id)) return;
  await openDetail(id);
};

const loadDictionaries = async () => {
  await Promise.all([typeDict.load(), priorityDict.load()]);
};

onMounted(async () => {
  await loadDictionaries();
  await load();
  await handleRouteDetail();
});

watch(
  () => route.query.tab,
  (value) => {
    const next = resolveTab(value);
    if (next !== activeTab.value) {
      activeTab.value = next;
    }
  },
);

watch(
  activeTab,
  (value) => {
    if (route.query.tab === value) return;
    router.replace({ query: { ...route.query, tab: value } }).catch(() => {});
  },
  { immediate: true },
);

watch(
  () => route.query.id,
  async () => {
    await handleRouteDetail();
  },
);
</script>
<style scoped lang="less">
.announcement-cards {
  :deep(.t-card__title) {
    font-weight: 600;
  }

  :deep(.t-card__body) {
    padding: 0 24px 24px;
  }
}

.notice-tabs {
  :deep(.t-tabs__header) {
    padding: 0;
  }
}

.tab-content {
  padding-top: 24px;
}

.empty-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 0;
  color: var(--td-text-color-secondary);

  img {
    width: 120px;
    margin-bottom: 16px;
  }
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.announcement-card {
  cursor: pointer;
  background-color: var(--td-bg-color-container);
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid var(--td-component-stroke);
  height: 100%;
  display: flex;
  flex-direction: column;

  .card-cover {
    position: relative;
    width: 100%;
    height: 160px;
    overflow: hidden;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.5s cubic-bezier(0.4, 0, 0.2, 1);
    }

    .read-time {
      position: absolute;
      right: 8px;
      bottom: 8px;
      background: rgb(0 0 0 / 60%);
      color: #fff;
      padding: 2px 8px;
      border-radius: 4px;
      font-size: 12px;
      backdrop-filter: blur(4px);
    }
  }

  &:hover .card-cover img {
    transform: scale(1.08);
  }

  .card-content {
    padding: 16px;
    flex: 1;
    display: flex;
    flex-direction: column;

    .card-tag {
      width: fit-content;
      margin-bottom: 8px;
      border: none;
      background-color: var(--td-bg-color-secondarycontainer);
    }

    .card-header {
      font-size: 16px;
      font-weight: 600;
      color: var(--td-text-color-primary);
      margin-bottom: 12px;
      line-height: 1.4;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
      flex: 1;
    }

    .card-footer {
      display: flex;
      align-items: center;
      gap: 16px;
      color: var(--td-text-color-placeholder);
      font-size: 12px;

      &-item {
        display: flex;
        align-items: center;
        gap: 4px;

        .t-icon {
          font-size: 14px;
        }
      }

      &-date {
        margin-left: auto;
      }
    }
  }
}

.detail-meta {
  margin-bottom: 12px;

  .time {
    color: var(--td-text-color-secondary);
  }
}

.detail-content {
  padding: 8px 0;
  line-height: 1.6;

  :deep(img) {
    max-width: 100%;
  }
}
</style>
