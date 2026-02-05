<template>
  <div ref="chartRef" class="chart-container" :style="{ height, width }"></div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from "vue";
import { useChart, type EChartsOption } from "./useChart";

interface Props {
  /** X轴数据（类目） */
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
  /** 是否水平显示 */
  horizontal?: boolean;
  /** 柱子颜色 */
  barColor?: string;
  /** 是否显示渐变色 */
  gradient?: boolean;
  /** 是否显示加载状态 */
  loading?: boolean;
  /** 是否显示数值标签 */
  showLabel?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  title: "",
  seriesName: "数量",
  height: "300px",
  width: "100%",
  horizontal: false,
  barColor: "#409EFF",
  gradient: true,
  loading: false,
  showLabel: false,
});

const chartRef = ref<HTMLElement | null>(null);

const chartOptions = computed<EChartsOption>(() => {
  const categoryAxis = {
    type: "category" as const,
    data: props.xData,
    axisLine: {
      lineStyle: {
        color: "#dcdfe6",
      },
    },
    axisLabel: {
      color: "#606266",
      interval: 0,
      rotate: props.horizontal ? 0 : props.xData.length > 6 ? 30 : 0,
    },
  };

  const valueAxis = {
    type: "value" as const,
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
  };

  return {
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
      axisPointer: {
        type: "shadow",
      },
      formatter: (params: any) => {
        const data = params[0];
        return `${data.axisValue}<br/>${data.seriesName}: ${data.value}`;
      },
    },
    grid: {
      left: "3%",
      right: "4%",
      bottom: props.horizontal ? "3%" : "10%",
      top: props.title ? "15%" : "10%",
      containLabel: true,
    },
    xAxis: props.horizontal ? valueAxis : categoryAxis,
    yAxis: props.horizontal ? categoryAxis : valueAxis,
    series: [
      {
        name: props.seriesName,
        type: "bar",
        data: props.yData,
        barWidth: "60%",
        itemStyle: {
          borderRadius: props.horizontal ? [0, 4, 4, 0] : [4, 4, 0, 0],
          color: props.gradient
            ? {
                type: "linear",
                x: props.horizontal ? 0 : 0,
                y: props.horizontal ? 0 : 1,
                x2: props.horizontal ? 1 : 0,
                y2: props.horizontal ? 0 : 0,
                colorStops: [
                  { offset: 0, color: `${props.barColor}80` },
                  { offset: 1, color: props.barColor },
                ],
              }
            : props.barColor,
        },
        label: {
          show: props.showLabel,
          position: props.horizontal ? "right" : "top",
          color: "#606266",
        },
        emphasis: {
          itemStyle: {
            color: props.barColor,
          },
        },
      },
    ],
  };
});

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
