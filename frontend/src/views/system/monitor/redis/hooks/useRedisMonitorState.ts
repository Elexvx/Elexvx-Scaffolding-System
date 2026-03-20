import type { EChartsInstance } from '@/utils/echarts';
import { ref } from 'vue';

import type { CommandStat, KeyspaceInfo, MetricPoint, RedisInfo } from '../types';

export const useRedisMonitorState = () => {
  const redisInfo = ref<RedisInfo>({});
  const commandStats = ref<CommandStat[]>([]);
  const keyspaceInfo = ref<KeyspaceInfo[]>([]);
  const redisEnabled = ref(true);
  const redisAvailable = ref(true);
  const redisMessage = ref('');
  const loading = ref(false);
  const history = ref<MetricPoint[]>([]);
  const memoryChartRef = ref<HTMLDivElement>();
  const opsChartRef = ref<HTMLDivElement>();
  const memoryChart = ref<EChartsInstance | null>(null);
  const opsChart = ref<EChartsInstance | null>(null);
  return { redisInfo, commandStats, keyspaceInfo, redisEnabled, redisAvailable, redisMessage, loading, history, memoryChartRef, opsChartRef, memoryChart, opsChart };
};
