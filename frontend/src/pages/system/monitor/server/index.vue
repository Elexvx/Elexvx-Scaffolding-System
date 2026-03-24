<template>
  <div class="server-monitor">
    <t-row :gutter="16">
      <!-- CPU 信息 -->
      <t-col :xs="24" :sm="12" :md="6">
        <t-card title="CPU" :bordered="false">
          <t-descriptions :column="2" layout="horizontal">
            <t-descriptions-item label="核心数">{{ serverInfo.cpu?.cpuNum || '-' }}</t-descriptions-item>
            <t-descriptions-item label="用户使用率">{{ serverInfo.cpu?.used || '-' }}</t-descriptions-item>
            <t-descriptions-item label="系统使用率">{{ serverInfo.cpu?.sys || '-' }}</t-descriptions-item>
            <t-descriptions-item label="当前空闲率">{{ serverInfo.cpu?.free || '-' }}</t-descriptions-item>
          </t-descriptions>
        </t-card>
      </t-col>

      <!-- 内存信息 -->
      <t-col :xs="24" :sm="12" :md="6">
        <t-card title="内存" :bordered="false">
          <t-descriptions :column="2" layout="horizontal">
            <t-descriptions-item label="总内存">{{ serverInfo.mem?.total || '-' }}</t-descriptions-item>
            <t-descriptions-item label="使用率">{{ serverInfo.mem?.usage || '-' }}</t-descriptions-item>
            <t-descriptions-item label="已用内存">{{ serverInfo.mem?.used || '-' }}</t-descriptions-item>
            <t-descriptions-item label="剩余内存">{{ serverInfo.mem?.free || '-' }}</t-descriptions-item>
          </t-descriptions>
        </t-card>
      </t-col>
    </t-row>

    <!-- 服务器信息 -->
    <t-card title="服务器信息" :bordered="false" style="margin-top: 16px">
      <t-descriptions :column="2" layout="horizontal">
        <t-descriptions-item label="服务器名称">{{ serverInfo.sys?.computerName || '-' }}</t-descriptions-item>
        <t-descriptions-item label="操作系统">{{ serverInfo.sys?.osName || '-' }}</t-descriptions-item>
        <t-descriptions-item label="服务器IP">{{ serverInfo.sys?.computerIp || '-' }}</t-descriptions-item>
        <t-descriptions-item label="系统架构">{{ serverInfo.sys?.osArch || '-' }}</t-descriptions-item>
      </t-descriptions>
    </t-card>

    <!-- Java虚拟机信息 -->
    <t-card title="Java虚拟机信息" :bordered="false" style="margin-top: 16px">
      <t-descriptions :column="2" layout="horizontal">
        <t-descriptions-item label="Java名称">{{ serverInfo.jvm?.name || '-' }}</t-descriptions-item>
        <t-descriptions-item label="Java版本">{{ serverInfo.jvm?.version || '-' }}</t-descriptions-item>
        <t-descriptions-item label="启动时间">{{ serverInfo.jvm?.startTime || '-' }}</t-descriptions-item>
        <t-descriptions-item label="运行时长">{{ serverInfo.jvm?.runTime || '-' }}</t-descriptions-item>
        <t-descriptions-item label="安装路径" :span="2">{{ serverInfo.jvm?.home || '-' }}</t-descriptions-item>
        <t-descriptions-item label="项目路径" :span="2">{{ serverInfo.jvm?.projectPath || '-' }}</t-descriptions-item>
        <t-descriptions-item label="运行参数" :span="2">{{ serverInfo.jvm?.inputArgs || '-' }}</t-descriptions-item>
      </t-descriptions>
    </t-card>

    <!-- 磁盘状态 -->
    <t-card title="磁盘状态" :bordered="false" style="margin-top: 16px">
      <t-table :data="serverInfo.sysFiles || []" :columns="diskColumns" row-key="dirName" :pagination="false as any" />
    </t-card>
  </div>
</template>
<script setup lang="ts">
import type { PrimaryTableCol } from 'tdesign-vue-next';
import { onActivated, onDeactivated, onMounted, onUnmounted, ref } from 'vue';

import { request } from '@/utils/request';

interface ServerInfo {
  cpu?: {
    cpuNum: number;
    used: string;
    sys: string;
    free: string;
  };
  mem?: {
    total: string;
    used: string;
    free: string;
    usage: string;
    jvmTotal: string;
    jvmUsed: string;
    jvmFree: string;
    jvmUsage: string;
  };
  sys?: {
    computerName: string;
    computerIp: string;
    osName: string;
    osArch: string;
  };
  jvm?: {
    name: string;
    version: string;
    startTime: string;
    runTime: string;
    home: string;
    projectPath: string;
    inputArgs: string;
  };
  sysFiles?: Array<{
    dirName: string;
    sysTypeName: string;
    typeName: string;
    total: string;
    free: string;
    used: string;
    usage: string;
  }>;
}

const serverInfo = ref<ServerInfo>({});

const diskColumns: PrimaryTableCol[] = [
  { colKey: 'dirName', title: '盘符路径', width: 150 },
  { colKey: 'sysTypeName', title: '文件系统', width: 150 },
  { colKey: 'typeName', title: '盘符类型', width: 150 },
  { colKey: 'total', title: '总大小', width: 150 },
  { colKey: 'free', title: '可用大小', width: 150 },
  { colKey: 'used', title: '已用大小', width: 150 },
  { colKey: 'usage', title: '已用百分比', width: 150 },
];

const loadServerInfo = async () => {
  try {
    serverInfo.value = await request.get<ServerInfo>({ url: '/system/monitor/server' });
  } catch (error) {
    console.error('获取服务器信息失败:', error);
  }
};

let timer: number | null = null;

const REFRESH_MS = 3000;

const stop = () => {
  if (timer) {
    clearInterval(timer);
    timer = null;
  }
};

const start = () => {
  stop();
  loadServerInfo();
  timer = window.setInterval(() => {
    loadServerInfo();
  }, REFRESH_MS);
};

onMounted(start);
onActivated(start);
onDeactivated(stop);
onUnmounted(stop);
</script>
<style lang="less" scoped>
.server-monitor {
  :deep(.t-card) {
    .t-card__title {
      font-weight: 500;
    }
  }

  :deep(.t-descriptions-item__label) {
    color: var(--td-text-color-secondary);
  }
}
</style>
