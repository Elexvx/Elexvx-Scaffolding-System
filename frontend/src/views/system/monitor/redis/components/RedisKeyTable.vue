<template>
  <div>
    <t-card title="命令统计" :bordered="false" style="margin-top: 16px">
      <t-table :data="commandStats" :columns="commandColumns" row-key="command" :pagination="null" />
    </t-card>
    <t-card title="Key信息" :bordered="false" style="margin-top: 16px">
      <t-table :data="keyspaceInfo" :columns="keyspaceColumns" row-key="db" :pagination="null" />
    </t-card>
  </div>
</template>
<script setup lang="ts">
defineProps<{ commandStats: any[]; keyspaceInfo: any[]; commandColumns: any[]; keyspaceColumns: any[] }>();
</script>
