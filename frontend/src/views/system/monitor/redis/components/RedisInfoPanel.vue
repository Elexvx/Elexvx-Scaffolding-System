<template>
  <t-row :gutter="16" style="margin-top: 16px">
    <t-col :xs="24" :sm="12" :md="6"><t-card title="内存趋势 (MB)" :bordered="false"><div ref="memoryRef" class="chart"></div></t-card></t-col>
    <t-col :xs="24" :sm="12" :md="6"><t-card title="吞吐趋势 (OPS)" :bordered="false"><div ref="opsRef" class="chart"></div></t-card></t-col>
  </t-row>
</template>
<script setup lang="ts">
import { watch } from 'vue';
const props = defineProps<{ memoryChartRef?: HTMLDivElement; opsChartRef?: HTMLDivElement }>();
const memoryRef = defineModel<HTMLDivElement | undefined>('memoryChartRef');
const opsRef = defineModel<HTMLDivElement | undefined>('opsChartRef');
watch(() => props.memoryChartRef, (val) => { memoryRef.value = val; }, { immediate: true });
watch(() => props.opsChartRef, (val) => { opsRef.value = val; }, { immediate: true });
</script>
<style scoped lang="less">.chart { height: 240px; }</style>
