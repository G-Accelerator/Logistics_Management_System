<template>
  <div ref="chartRef" class="chart-container" :style="{ height, width }"></div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from "vue";
import { useChart, type EChartsOption } from "./useChart";

interface Props {
  /** X轴数据（日期/类目） */
  xData: string[];
  /** Y轴数据（数值） */
  yData: number[];
  /** 图表标题 */
  title?: string;
  /** 系列名称 */
  seriesName?: string;
  /** 图表高度 */
  height?: string;
  /** 图表宽度 */
  width?: string;
  /** 是否显示区域填充 */
  areaStyle?: boolean;
  /** 是否平滑曲线 */
  smooth?: boolean;
  /** 线条颜色 */
  lineColor?: string;
  /** 是否显示加载状态 */
  loading?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  title: "",
  seriesName: "数量",
  height: "300px",
  width: "100%",
  areaStyle: true,
  smooth: true,
  lineColor: "#409EFF",
  loading: false,
});

const chartRef = ref<HTMLElement | null>(null);

const chartOptions = computed<EChartsOption>(() => ({
  title: props.title
    ? {
        text: props.title,
        left: "center",
        textStyle: {
          fontSize: 14,
          fontWeight: "normal",
        },
      }
    : undefined,
  tooltip: {
    trigger: "axis",
    formatter: (params: any) => {
      const data = params[0];
      return `${data.axisValue}<br/>${data.seriesName}: ${data.value}`;
    },
  },
  grid: {
    left: "3%",
    right: "4%",
    bottom: "3%",
    top: props.title ? "15%" : "10%",
    containLabel: true,
  },
  xAxis: {
    type: "category",
    boundaryGap: false,
    data: props.xData,
    axisLine: {
      lineStyle: {
        color: "#dcdfe6",
      },
    },
    axisLabel: {
      color: "#606266",
    },
  },
  yAxis: {
    type: "value",
    axisLine: {
      show: false,
    },
    axisTick: {
      show: false,
    },
    splitLine: {
      lineStyle: {
        color: "#ebeef5",
      },
    },
    axisLabel: {
      color: "#606266",
    },
  },
  series: [
    {
      name: props.seriesName,
      type: "line",
      smooth: props.smooth,
      data: props.yData,
      itemStyle: {
        color: props.lineColor,
      },
      lineStyle: {
        width: 2,
        color: props.lineColor,
      },
      areaStyle: props.areaStyle
        ? {
            color: {
              type: "linear",
              x: 0,
              y: 0,
              x2: 0,
              y2: 1,
              colorStops: [
                { offset: 0, color: `${props.lineColor}40` },
                { offset: 1, color: `${props.lineColor}05` },
              ],
            },
          }
        : undefined,
      emphasis: {
        focus: "series",
      },
    },
  ],
}));

const { showLoading, hideLoading } = useChart({
  el: chartRef,
  options: chartOptions,
});

// 监听 loading 状态
watch(
  () => props.loading,
  (val) => {
    val ? showLoading() : hideLoading();
  },
  { immediate: true },
);
</script>

<style scoped>
.chart-container {
  min-height: 200px;
}
</style>
