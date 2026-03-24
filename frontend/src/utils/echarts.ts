import { LineChart } from 'echarts/charts';
import { GridComponent, TooltipComponent } from 'echarts/components';
import * as echarts from 'echarts/core';
import { CanvasRenderer } from 'echarts/renderers';

let registered = false;

export function ensureEChartsRegistered() {
  if (registered) return;
  echarts.use([LineChart, GridComponent, TooltipComponent, CanvasRenderer]);
  registered = true;
}

export function initEChart(el: HTMLDivElement) {
  ensureEChartsRegistered();
  return echarts.init(el);
}

export type EChartsInstance = echarts.ECharts;
