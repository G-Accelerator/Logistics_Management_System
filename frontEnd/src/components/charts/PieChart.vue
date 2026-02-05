<template>
  <div ref="chartRef" class="chart-container" :style="{ height, width }"></div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from "vue";
import { useChart, type EChartsOption } from "./useChart";

interface PieDataItem {
  name: string;
  value: number;
}

interface Props {
  /** 饼图数据 */
  data: PieDataItem[];
  /** 图表标题 */
  title?: string;
  /** 图表高度 */
  height?: string;
  /** 图表宽度 */
  width?: string;
  /** 是否显示图例 */
  showLegend?: boolean;
  /** 是否为环形图 */
  ring?: boolean;
  /** 是否显示标签 */
  showLabel?: boolean;
  /** 是否显示加载状态 */
  loading?: boolean;
  /** 自定义颜色 */
  colors?: string[];
}

const props = withDefaults(defineProps<Props>(), {
  title: "",
  height: "300px",
  width: "100%",
  showLegend: true,
  ring: false,
  showLabel: true,
  loading: false,
  colors: () => [
    "#409EFF",
    "#67C23A",
    "#E6A23C",
    "#F56C6C",
    "#909399",
    "#00D4FF",
    "#FF6B6B",
    "#4ECDC4",
  ],
});

const emit = defineEmits<{
  (e: "click", data: PieDataItem): void;
}>();

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
    trigger: "item",
    formatter: (params: any) => {
      return `${params.name}: ${params.value} (${params.percent}%)`;
    },
  },
  legend: props.showLegend
    ? {
        orient: "horizontal",
        bottom: "5%",
        left: "center",
        textStyle: {
          color: "#606266",
        },
      }
    : undefined,
  color: props.colors,
  series: [
    {
      name: props.title || "分布",
      type: "pie",
      radius: props.ring ? ["40%", "70%"] : "70%",
      center: ["50%", props.showLegend ? "45%" : "50%"],
      avoidLabelOverlap: true,
      itemStyle: {
        borderRadius: props.ring ? 6 : 0,
        borderColor: "#fff",
        borderWidth: 2,
      },
      label: {
        show: props.showLabel,
        formatter: "{b}: {d}%",
      },
      emphasis: {
        label: {
          show: true,
          fontSize: 14,
          fontWeight: "bold",
        },
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: "rgba(0, 0, 0, 0.5)",
        },
      },
      labelLine: {
        show: props.showLabel,
      },
      data: props.data,
    },
  ],
}));

const { chartInstance, showLoading, hideLoading } = useChart({
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

// 监听图表实例，绑定点击事件
watch(chartInstance, (instance) => {
  if (instance) {
    instance.on("click", (params: any) => {
      emit("click", { name: params.name, value: params.value });
    });
  }
});
</script>

<style scoped>
.chart-container {
  min-height: 200px;
}
</style>
