import { onMounted, onUnmounted, watch, type Ref, shallowRef } from "vue";
import * as echarts from "echarts";
import type { EChartsOption, ECharts } from "echarts";

export interface UseChartOptions {
  /** 图表容器元素引用 */
  el: Ref<HTMLElement | null>;
  /** 图表配置项 */
  options: Ref<EChartsOption>;
  /** 是否自动调整大小 */
  autoResize?: boolean;
}

export interface UseChartReturn {
  /** ECharts 实例 */
  chartInstance: Ref<ECharts | null>;
  /** 设置图表配置 */
  setOption: (option: EChartsOption, notMerge?: boolean) => void;
  /** 调整图表大小 */
  resize: () => void;
  /** 显示加载动画 */
  showLoading: () => void;
  /** 隐藏加载动画 */
  hideLoading: () => void;
}

/**
 * ECharts 图表组合式函数
 * 提供图表初始化、配置更新、自动调整大小等功能
 */
export function useChart(options: UseChartOptions): UseChartReturn {
  const { el, options: chartOptions, autoResize = true } = options;
  const chartInstance = shallowRef<ECharts | null>(null);

  // 初始化图表
  const initChart = () => {
    if (!el.value) return;

    // 销毁已存在的实例
    if (chartInstance.value) {
      chartInstance.value.dispose();
    }

    chartInstance.value = echarts.init(el.value);
    chartInstance.value.setOption(chartOptions.value);
  };

  // 设置图表配置
  const setOption = (option: EChartsOption, notMerge = false) => {
    if (chartInstance.value) {
      chartInstance.value.setOption(option, notMerge);
    }
  };

  // 调整图表大小
  const resize = () => {
    chartInstance.value?.resize();
  };

  // 显示加载动画
  const showLoading = () => {
    chartInstance.value?.showLoading({
      text: "加载中...",
      color: "#409EFF",
      maskColor: "rgba(255, 255, 255, 0.8)",
    });
  };

  // 隐藏加载动画
  const hideLoading = () => {
    chartInstance.value?.hideLoading();
  };

  // 监听配置变化
  watch(
    chartOptions,
    (newOptions) => {
      setOption(newOptions, true);
    },
    { deep: true },
  );

  // 监听容器元素变化
  watch(el, () => {
    initChart();
  });

  // 窗口大小变化处理
  const handleResize = () => {
    resize();
  };

  onMounted(() => {
    initChart();
    if (autoResize) {
      window.addEventListener("resize", handleResize);
    }
  });

  onUnmounted(() => {
    if (autoResize) {
      window.removeEventListener("resize", handleResize);
    }
    chartInstance.value?.dispose();
    chartInstance.value = null;
  });

  return {
    chartInstance,
    setOption,
    resize,
    showLoading,
    hideLoading,
  };
}

// 导出 echarts 类型供组件使用
export type { EChartsOption, ECharts };
