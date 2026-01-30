<template>
  <el-dialog
    v-model="visible"
    :title="title"
    :width="width"
    top="3vh"
    destroy-on-close
    @opened="initMap"
    @closed="destroyMap"
  >
    <div
      ref="mapContainerRef"
      class="map-dialog-container"
      :style="{ height: mapHeight }"
    ></div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";
import type { MapPoint } from "./types";

export type { MapPoint, MapDialogProps } from "./types";

interface Props {
  modelValue: boolean;
  title?: string;
  width?: string;
  mapHeight?: string;
  points: MapPoint[];
  lineColor?: string;
  startColor?: string;
  endColor?: string;
  middleColor?: string;
}

const props = withDefaults(defineProps<Props>(), {
  title: "查看地图",
  width: "85%",
  mapHeight: "65vh",
  lineColor: "#6366f1",
  startColor: "#10b981",
  endColor: "#f59e0b",
  middleColor: "#6366f1",
});

const emit = defineEmits<{
  "update:modelValue": [value: boolean];
}>();

const visible = ref(props.modelValue);
const mapContainerRef = ref<HTMLElement | null>(null);

let map: any = null;
let polyline: any = null;
let markers: any[] = [];

watch(
  () => props.modelValue,
  (val) => {
    visible.value = val;
  },
);

watch(visible, (val) => {
  emit("update:modelValue", val);
});

const initMap = () => {
  if (!mapContainerRef.value) return;

  // 销毁旧地图实例
  destroyMap();

  // 创建新地图
  map = new AMap.Map(mapContainerRef.value, {
    zoom: 5,
    center: [116.397428, 39.90923],
  });

  // 添加比例尺控件
  (AMap as any).plugin(["AMap.Scale"], () => {
    map.addControl(new AMap.Scale());
  });

  // 绘制路线和标记
  drawRoute();
};

const destroyMap = () => {
  if (polyline) {
    map?.remove(polyline);
    polyline = null;
  }
  markers.forEach((m) => map?.remove(m));
  markers = [];
  if (map) {
    map.destroy();
    map = null;
  }
};

const drawRoute = () => {
  if (!map || props.points.length === 0) return;

  const points = props.points;

  // 绘制路线
  if (points.length > 1) {
    polyline = new AMap.Polyline({
      path: points.map((p) => [p.lng, p.lat]),
      strokeColor: props.lineColor,
      strokeWeight: 5,
      strokeOpacity: 0.9,
      strokeStyle: "dashed",
      lineJoin: "round",
      lineCap: "round",
    });
    map.add(polyline);
  }

  // 绘制站点标记
  points.forEach((pt, idx) => {
    const isStart = pt.isStart ?? idx === 0;
    const isEnd = pt.isEnd ?? idx === points.length - 1;
    const color = isStart
      ? props.startColor
      : isEnd
        ? props.endColor
        : props.middleColor;
    const size = isStart || isEnd ? 16 : 10;

    // 站点圆点
    const marker = new AMap.Marker({
      position: [pt.lng, pt.lat],
      content: `<div style="width:${size}px;height:${size}px;background:${color};border:${isStart || isEnd ? 3 : 2}px solid #fff;border-radius:50%;box-shadow:0 2px 6px rgba(0,0,0,0.3);"></div>`,
      offset: new AMap.Pixel(-size / 2, -size / 2),
    });
    map.add(marker);
    markers.push(marker);

    // 站点名称标签
    const labelText =
      pt.address.length > 15 ? pt.address.substring(0, 15) + "..." : pt.address;
    const label = new AMap.Marker({
      position: [pt.lng, pt.lat],
      content: `<div style="background:${color};color:#fff;padding:3px 8px;border-radius:4px;font-size:11px;font-weight:500;box-shadow:0 2px 4px rgba(0,0,0,0.2);white-space:nowrap;max-width:150px;overflow:hidden;text-overflow:ellipsis;">${labelText}</div>`,
      offset: new AMap.Pixel(-20, -32),
    });
    map.add(label);
    markers.push(label);
  });

  // 自适应视野
  map.setFitView(null, false, [60, 60, 60, 60]);
};

// 暴露方法供外部调用
defineExpose({
  refresh: () => {
    if (map) drawRoute();
  },
});
</script>

<style scoped>
.map-dialog-container {
  width: 100%;
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-dialog__body) {
  padding: 12px 20px 20px;
}

:deep(.amap-logo),
:deep(.amap-copyright) {
  display: none !important;
}
</style>
