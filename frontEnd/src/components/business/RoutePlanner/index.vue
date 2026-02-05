<template>
  <div class="route-planner">
    <!-- 顶部操作栏：策略选择 + 按钮 -->
    <div class="planner-header">
      <div class="strategy-group">
        <el-checkbox-group v-model="selectedStrategies" size="small">
          <el-checkbox-button :value="0">速度优先</el-checkbox-button>
          <el-checkbox-button :value="1">费用优先</el-checkbox-button>
          <el-checkbox-button :value="2">距离优先</el-checkbox-button>
          <el-checkbox-button :value="4">躲避拥堵</el-checkbox-button>
        </el-checkbox-group>
      </div>
      <div class="action-group">
        <el-button
          type="primary"
          size="small"
          @click="planRoute"
          :loading="loading"
          :disabled="selectedStrategies.length === 0 || !canPlan"
        >
          规划路线
        </el-button>
        <el-button
          v-if="routeOptions.length > 0"
          size="small"
          @click="clearRoutes"
        >
          清空
        </el-button>
        <el-button
          v-if="selectedRoute"
          type="primary"
          size="small"
          plain
          @click="emit('showMap')"
        >
          <el-icon><MapLocation /></el-icon>地图
        </el-button>
      </div>
    </div>

    <!-- 路线方案列表 -->
    <div v-if="routeOptions.length > 0" class="route-list">
      <div
        v-for="(route, index) in routeOptions"
        :key="index"
        class="route-card"
        :class="{ 'is-selected': selectedRouteIndex === index }"
        @click="selectRoute(index)"
      >
        <div class="route-card-header">
          <el-tag :type="route.tagType" size="small" effect="plain">
            {{ route.label }}
          </el-tag>
          <el-icon v-if="selectedRouteIndex === index" class="check-icon">
            <Check />
          </el-icon>
        </div>
        <div class="route-card-stats">
          <span class="stat-item">
            <el-icon><Odometer /></el-icon>
            {{ (route.distance / 1000).toFixed(1) }}km
          </span>
          <span class="stat-item">
            <el-icon><Timer /></el-icon>
            {{ formatDuration(route.duration) }}
          </span>
          <span v-if="route.tolls > 0" class="stat-item toll">
            ¥{{ route.tolls }}
          </span>
          <span v-else class="stat-item free">免费</span>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-state">
      <span>选择策略后点击"规划路线"</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from "vue";
import { ElMessage } from "element-plus";
import { MapLocation, Odometer, Timer, Check } from "@element-plus/icons-vue";
import { planRouteApi } from "../../../api/order";
import type {
  RouteStrategy,
  RouteOptionData,
  RoutePlannerProps,
} from "./types";

const props = defineProps<RoutePlannerProps>();

const emit = defineEmits<{
  (e: "update", route: RouteOptionData | null): void;
  (e: "showMap"): void;
}>();

const loading = ref(false);
const routeOptions = ref<RouteOptionData[]>([]);
const selectedRouteIndex = ref(0);
const selectedStrategies = ref<RouteStrategy[]>([0]);
const generatedStrategies = ref<Set<RouteStrategy>>(new Set());

const selectedRoute = computed(
  () => routeOptions.value[selectedRouteIndex.value] || null,
);

const canPlan = computed(() => !!props.origin && !!props.destination);

watch(
  () => [props.origin, props.destination],
  () => clearRoutes(),
);

watch(selectedRoute, (route) => emit("update", route));

const formatDuration = (s: number) => {
  const h = Math.floor(s / 3600);
  const m = Math.floor((s % 3600) / 60);
  return h > 0 ? `${h}h${m}m` : `${m}分钟`;
};

const geocodeAddress = (address: string): Promise<[number, number] | null> =>
  new Promise((resolve) => {
    if (typeof AMap === "undefined") {
      console.warn("AMap 未加载");
      return resolve(null);
    }

    // 确保 Geocoder 插件已加载
    (AMap as any).plugin("AMap.Geocoder", () => {
      try {
        const geocoder = new (AMap as any).Geocoder({ city: "全国" });
        geocoder.getLocation(address, (status: string, result: any) => {
          console.log("地理编码结果:", address, status, result);
          if (status === "complete" && result.geocodes?.length > 0) {
            const { lng, lat } = result.geocodes[0].location;
            resolve([lng, lat]);
          } else {
            console.warn("地理编码失败:", address, status, result?.info);
            resolve(null);
          }
        });
      } catch (e) {
        console.error("地理编码异常:", e);
        resolve(null);
      }
    });
  });

const planRoute = async () => {
  if (!canPlan.value) return ElMessage.warning("请先输入起始地和目的地");
  if (selectedStrategies.value.length === 0)
    return ElMessage.warning("请至少选择一种策略");

  const newStrategies = selectedStrategies.value.filter(
    (s) => !generatedStrategies.value.has(s),
  );
  if (newStrategies.length === 0) return ElMessage.info("所选策略已生成");

  loading.value = true;
  try {
    // 优先使用传入的坐标，否则用前端 AMap JS SDK 地理编码
    let originCoord = props.originCoord;
    let destCoord = props.destCoord;

    if (typeof AMap !== "undefined") {
      if (!originCoord) {
        originCoord = await geocodeAddress(props.origin);
      }
      if (!destCoord) {
        destCoord = await geocodeAddress(props.destination);
      }
    }

    // 必须有坐标才能规划路线
    if (!originCoord || !destCoord) {
      ElMessage.error("无法获取地址坐标，请检查地址是否正确");
      return;
    }

    const response = await planRouteApi({
      origin: props.origin,
      destination: props.destination,
      originCoord: originCoord,
      destCoord: destCoord,
      strategies: newStrategies,
    });

    if (response.routes?.length > 0) {
      routeOptions.value = [...routeOptions.value, ...response.routes];
      newStrategies.forEach((s) => generatedStrategies.value.add(s));
      if (selectedRouteIndex.value === 0) selectRoute(0);
      ElMessage.success(`已生成 ${response.routes.length} 条路线`);
    } else {
      ElMessage.warning("未能规划出路线");
    }
  } catch (e: any) {
    ElMessage.error(e?.message || "路线规划失败");
  } finally {
    loading.value = false;
  }
};

const selectRoute = (index: number) => {
  if (index >= 0 && index < routeOptions.value.length)
    selectedRouteIndex.value = index;
};

const clearRoutes = () => {
  routeOptions.value = [];
  selectedRouteIndex.value = 0;
  generatedStrategies.value.clear();
  emit("update", null);
};

defineExpose({
  clearRoutes,
  getSelectedRoute: () => selectedRoute.value,
  getTrackPoints: () => selectedRoute.value?.trackPoints || [],
});
</script>

<style scoped>
.route-planner {
  width: 100%;
}

.planner-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.strategy-group {
  flex-shrink: 0;
}

.action-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.route-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 10px;
  margin-top: 16px;
}

.route-card {
  padding: 10px 12px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.15s ease;
  background: var(--el-bg-color);
}

.route-card:hover {
  border-color: var(--el-color-primary-light-5);
}

.route-card.is-selected {
  border-color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
}

.route-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.check-icon {
  color: var(--el-color-primary);
  font-size: 16px;
}

.route-card-stats {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  font-size: 12px;
}

.stat-item {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  color: var(--el-text-color-secondary);
}

.stat-item .el-icon {
  font-size: 12px;
  color: var(--el-color-primary);
}

.stat-item.toll {
  color: var(--el-color-warning);
  font-weight: 500;
}

.stat-item.free {
  color: var(--el-color-success);
  font-weight: 500;
}

.empty-state {
  margin-top: 16px;
  padding: 20px;
  text-align: center;
  color: var(--el-text-color-placeholder);
  font-size: 13px;
  background: var(--el-fill-color-lighter);
  border-radius: 6px;
}
</style>
