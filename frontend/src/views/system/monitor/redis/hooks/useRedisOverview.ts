import debounce from 'lodash/debounce';
import { MessagePlugin } from 'tdesign-vue-next';
import { computed, nextTick, onActivated, onDeactivated, onMounted, onUnmounted } from 'vue';

import { initEChart } from '@/utils/echarts';
import { request } from '@/utils/request';

import { REFRESH_MS } from '../constants/redisOptions';
import { applyRedisResponse, replaceIfChanged } from '../utils/redisMappers';
import { createMetricPoint, parseHumanToMb, safeInt } from '../utils/redisMetrics';
import type { RedisHealthStatus, RedisMonitorResponse } from '../types';

export const useRedisOverview = (state: ReturnType<typeof import('./useRedisMonitorState').useRedisMonitorState>) => {
  const { redisInfo, commandStats, keyspaceInfo, redisEnabled, redisAvailable, redisMessage, loading, history, memoryChartRef, opsChartRef, memoryChart, opsChart } = state;
  const tcpPort = computed(() => safeInt(redisInfo.value.tcp_port));
  const connectedClients = computed(() => safeInt(redisInfo.value.connected_clients));
  const uptimeDays = computed(() => safeInt(redisInfo.value.uptime_in_days));
  const hitCount = computed(() => safeInt(redisInfo.value.hitCount ?? redisInfo.value.keyspace_hits));
  const missCount = computed(() => safeInt(redisInfo.value.missCount ?? redisInfo.value.keyspace_misses));
  const hitRate = computed(() => {
    const total = hitCount.value + missCount.value;
    return total <= 0 ? 0 : Number(((hitCount.value / total) * 100).toFixed(2));
  });
  const usedMemoryMb = computed(() => {
    const bytes = Number(redisInfo.value.usedMemory || 0);
    return Number.isFinite(bytes) && bytes > 0 ? Number((bytes / 1024 / 1024).toFixed(2)) : parseHumanToMb(redisInfo.value.used_memory_human);
  });
  const usedMemoryPeakMb = computed(() => parseHumanToMb(redisInfo.value.used_memory_peak_human));

  const renderCharts = () => {
    const labels = history.value.map((p) => new Date(p.timestamp).toLocaleTimeString().replace(/^\d{2}:/, ''));
    const memoryData = history.value.map((p) => Number(p.memory.toFixed(2)));
    const opsData = history.value.map((p) => Number(p.ops.toFixed(2)));
    if (memoryChartRef.value && !memoryChart.value) memoryChart.value = initEChart(memoryChartRef.value);
    if (opsChartRef.value && !opsChart.value) opsChart.value = initEChart(opsChartRef.value);
    memoryChart.value?.setOption({ tooltip: { trigger: 'axis' }, xAxis: { type: 'category', data: labels }, yAxis: { type: 'value', axisLabel: { formatter: '{value} MB' } }, series: [{ type: 'line', data: memoryData, smooth: true, areaStyle: {} }] });
    opsChart.value?.setOption({ tooltip: { trigger: 'axis' }, xAxis: { type: 'category', data: labels }, yAxis: { type: 'value' }, series: [{ type: 'line', data: opsData, smooth: true, areaStyle: {} }] });
  };
  const debouncedRenderCharts = debounce(renderCharts, 200);

  const pushHistory = () => {
    history.value.push(createMetricPoint(redisInfo.value));
    if (history.value.length > 30) history.value.shift();
  };

  const loadRedisInfo = async (options: { showLoading?: boolean } = {}) => {
    const showLoading = options.showLoading ?? false;
    if (showLoading) loading.value = true;
    try {
      const health = await request.get<RedisHealthStatus>({ url: '/health' });
      redisEnabled.value = health.redisEnabled;
      redisAvailable.value = health.redisAvailable;
      redisMessage.value = health.redisMessage || '';
      if (redisEnabled.value && redisAvailable.value) {
        const response = await request.get<RedisMonitorResponse>({ url: '/system/monitor/redis' });
        redisInfo.value = applyRedisResponse(response);
        commandStats.value = replaceIfChanged(commandStats.value, response.commandStats || []);
        keyspaceInfo.value = replaceIfChanged(keyspaceInfo.value, response.keyspace || []);
        pushHistory();
        await nextTick();
        debouncedRenderCharts();
      }
    } catch (error) {
      console.error('获取Redis信息失败:', error);
      MessagePlugin.error('获取 Redis 信息失败，请检查后端日志');
      redisAvailable.value = false;
      redisMessage.value = '无法获取详细监控数据，可能是连接中断或配置错误。';
    } finally {
      if (showLoading) loading.value = false;
    }
  };

  let timer: number | null = null;
  const stop = (): void => { if (timer) { clearInterval(timer); timer = null; } };
  const start = (): void => {
    stop();
    void loadRedisInfo({ showLoading: true });
    timer = window.setInterval((): void => {
      void loadRedisInfo({ showLoading: false });
    }, REFRESH_MS);
  };

  onMounted(start);
  onActivated(start);
  onDeactivated(stop);
  onUnmounted(() => {
    stop();
    memoryChart.value?.dispose();
    opsChart.value?.dispose();
    memoryChart.value = null;
    opsChart.value = null;
    debouncedRenderCharts.cancel();
  });

  return { tcpPort, connectedClients, uptimeDays, hitCount, missCount, hitRate, usedMemoryMb, usedMemoryPeakMb, loadRedisInfo };
};
