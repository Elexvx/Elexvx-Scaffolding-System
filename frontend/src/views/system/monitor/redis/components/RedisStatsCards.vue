<template>
  <t-space direction="vertical" style="width: 100%">
    <t-row :gutter="16">
      <t-col :span="3"><t-statistic title="Redis版本" :value="(info.redis_version || '-') as unknown as number" /></t-col>
      <t-col :span="3"><t-statistic title="运行模式" :value="(info.redis_mode || '-') as unknown as number" /></t-col>
      <t-col :span="3"><t-statistic title="端口" :value="tcpPort" /></t-col>
      <t-col :span="3"><t-statistic title="客户端数" :value="connectedClients" /></t-col>
    </t-row>
    <t-row :gutter="16">
      <t-col :span="3"><t-statistic title="运行时间(天)" :value="uptimeDays" /></t-col>
      <t-col :span="3"><t-statistic title="内存使用量" :value="usedMemoryMb" unit="MB" /></t-col>
      <t-col :span="3"><t-statistic title="内存峰值" :value="usedMemoryPeakMb" unit="MB" /></t-col>
      <t-col :span="3"><t-statistic title="命中率" :value="hitRate" unit="%" /></t-col>
    </t-row>
    <t-row :gutter="16">
      <t-col :span="3"><t-statistic title="命中次数" :value="hitCount" /></t-col>
      <t-col :span="3"><t-statistic title="未命中次数" :value="missCount" /></t-col>
      <t-col :span="3"><t-statistic title="QPS" :value="info.instantaneous_ops_per_sec ?? 0" /></t-col>
      <t-col :span="3"><t-statistic title="键数量" :value="info.keyCount ?? 0" /></t-col>
    </t-row>
  </t-space>
</template>
<script setup lang="ts">defineProps<{ info: Record<string, any>; tcpPort: number; connectedClients: number; uptimeDays: number; usedMemoryMb: number; usedMemoryPeakMb: number; hitRate: number; hitCount: number; missCount: number }>();</script>
