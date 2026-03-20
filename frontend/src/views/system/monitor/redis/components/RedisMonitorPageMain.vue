<template>
  <div class="redis-monitor">
    <t-card v-if="!redisEnabled" :bordered="false"><t-alert theme="warning" title="提示"><template #message><div><p>Redis 监控功能当前未启用。</p><p>如需使用此功能，请在系统配置中启用 Redis 并确保 Redis 服务正在运行。</p></div></template></t-alert></t-card>
    <template v-else>
      <t-loading :loading="loading">
        <t-card v-if="!redisAvailable" :bordered="false"><t-alert theme="error" title="错误"><template #message><div><p>无法连接到 Redis 服务。</p><p>{{ redisMessage || '请检查 Redis 服务是否正在运行，以及配置是否正确。' }}</p></div></template></t-alert></t-card>
        <template v-else>
          <t-card title="Redis信息" :bordered="false">
            <template #actions><redis-refresh-toolbar :can-refresh="canRefresh" @refresh="() => loadRedisInfo({ showLoading: true })" /></template>
            <redis-stats-cards :info="redisInfo" :tcp-port="tcpPort" :connected-clients="connectedClients" :uptime-days="uptimeDays" :used-memory-mb="usedMemoryMb" :used-memory-peak-mb="usedMemoryPeakMb" :hit-rate="hitRate" :hit-count="hitCount" :miss-count="missCount" />
          </t-card>
          <t-row :gutter="16" style="margin-top: 16px">
            <t-col :xs="24" :sm="12" :md="6"><t-card title="内存趋势 (MB)" :bordered="false"><div ref="memoryChartRef" class="chart"></div></t-card></t-col>
            <t-col :xs="24" :sm="12" :md="6"><t-card title="吞吐趋势 (OPS)" :bordered="false"><div ref="opsChartRef" class="chart"></div></t-card></t-col>
          </t-row>
          <redis-key-table :command-stats="commandStats" :keyspace-info="keyspaceInfo" :command-columns="commandColumns" :keyspace-columns="keyspaceColumns" />
        </template>
      </t-loading>
    </template>
  </div>
</template>
<script setup lang="ts">
import { commandColumns, keyspaceColumns } from '../constants/redisOptions';
import { useRedisMonitorState } from '../hooks/useRedisMonitorState';
import { useRedisOverview } from '../hooks/useRedisOverview';
import { useRedisPermissions } from '../hooks/useRedisPermissions';
import RedisKeyTable from './RedisKeyTable.vue';
import RedisRefreshToolbar from './RedisRefreshToolbar.vue';
import RedisStatsCards from './RedisStatsCards.vue';

const state = useRedisMonitorState();
const { canRefresh } = useRedisPermissions();
const { tcpPort, connectedClients, uptimeDays, hitCount, missCount, hitRate, usedMemoryMb, usedMemoryPeakMb, loadRedisInfo } = useRedisOverview(state);
const { redisInfo, commandStats, keyspaceInfo, redisEnabled, redisAvailable, redisMessage, loading, memoryChartRef, opsChartRef } = state;
</script>
<style scoped lang="less">
.redis-monitor {
  :deep(.t-card) { .t-card__title { font-weight: 500; } }
  :deep(.t-statistic) { .t-statistic__title { color: var(--td-text-color-secondary); } }
  .chart { height: 240px; }
}
</style>
