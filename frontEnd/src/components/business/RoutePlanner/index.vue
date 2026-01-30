<template>
  <div class="route-planner">
    <!-- 策略选择 -->
    <div class="strategy-select">
      <span class="strategy-label">选择路线策略：</span>
      <el-checkbox-group v-model="selectedStrategies">
        <el-checkbox :value="0">最快路线</el-checkbox>
        <el-checkbox :value="1">最短路线</el-checkbox>
        <el-checkbox :value="2">最省钱</el-checkbox>
        <el-checkbox :value="4">躲避拥堵</el-checkbox>
      </el-checkbox-group>
    </div>

    <!-- 操作按钮 -->
    <div class="route-actions">
      <el-button
        type="primary"
        @click="planRoute"
        :loading="loading"
        :disabled="selectedStrategies.length === 0 || !canPlan"
      >
        <el-icon><Van /></el-icon>规划路线
      </el-button>
      <el-button
        v-if="routeOptions.length > 0"
        type="danger"
        plain
        @click="clearRoutes"
      >
        <el-icon><Delete /></el-icon>清空路线
      </el-button>
      <template v-if="selectedRoute">
        <div class="route-info">
          <span class="info-item">
            <el-icon><Odometer /></el-icon>
            <span>{{ (selectedRoute.distance / 1000).toFixed(1) }} km</span>
          </span>
          <span class="info-item">
            <el-icon><Timer /></el-icon>
            <span>{{ formatDuration(selectedRoute.duration) }}</span>
          </span>
          <span class="info-item">
            <el-icon><Flag /></el-icon>
            <span>{{ selectedRoute.trackPoints.length }} 个站点</span>
          </span>
        </div>
        <el-button type="primary" plain @click="emit('showMap')">
          <el-icon><MapLocation /></el-icon>查看地图
        </el-button>
      </template>
    </div>

    <!-- 多路线选择 -->
    <div v-if="routeOptions.length > 0" class="route-options">
      <div class="route-options-title">选择路线方案：</div>
      <div class="route-options-list">
        <div
          v-for="(route, index) in routeOptions"
          :key="index"
          class="route-option-item"
          :class="{ 'is-selected': selectedRouteIndex === index }"
          @click="selectRoute(index)"
        >
          <div class="route-option-header">
            <el-tag :type="route.tagType" size="small">{{
              route.label
            }}</el-tag>
            <span v-if="selectedRouteIndex === index" class="selected-badge">
              <el-icon><Check /></el-icon>已选
            </span>
          </div>
          <div class="route-option-info">
            <span class="route-stat">
              <el-icon><Odometer /></el-icon>
              {{ (route.distance / 1000).toFixed(1) }} km
            </span>
            <span class="route-stat">
              <el-icon><Timer /></el-icon>
              {{ formatDuration(route.duration) }}
            </span>
          </div>
          <div class="route-option-desc">{{ route.description }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from "vue";
import { ElMessage } from "element-plus";
import {
  Van,
  Delete,
  MapLocation,
  Odometer,
  Timer,
  Check,
  Flag,
} from "@element-plus/icons-vue";
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

// 当前选中的路线
const selectedRoute = computed(
  () => routeOptions.value[selectedRouteIndex.value] || null,
);

// 是否可以规划（需要起点和终点）
const canPlan = computed(() => !!props.origin && !!props.destination);

// 监听地址变化，清空路线
watch(
  () => [props.origin, props.destination],
  () => {
    clearRoutes();
  },
);

// 监听选中路线变化，通知父组件
watch(selectedRoute, (route) => {
  emit("update", route);
});

const formatDuration = (s: number) => {
  const h = Math.floor(s / 3600);
  const m = Math.floor((s % 3600) / 60);
  return h > 0 ? `${h}小时${m}分钟` : `${m}分钟`;
};

// 地理编码：地址转坐标
const geocodeAddress = (address: string): Promise<[number, number] | null> =>
  new Promise((resolve) => {
    if (typeof AMap === "undefined") {
      resolve(null);
      return;
    }
    new (AMap as any).Geocoder().getLocation(
      address,
      (status: string, result: any) => {
        if (status === "complete" && result.geocodes?.length > 0) {
          const { lng, lat } = result.geocodes[0].location;
          resolve([lng, lat]);
        } else {
          resolve(null);
        }
      },
    );
  });

const planRoute = async () => {
  if (!canPlan.value) {
    ElMessage.warning("请先输入起始地和目的地");
    return;
  }
  if (selectedStrategies.value.length === 0) {
    ElMessage.warning("请至少选择一种路线策略");
    return;
  }

  const newStrategies = selectedStrategies.value.filter(
    (s) => !generatedStrategies.value.has(s),
  );

  if (newStrategies.length === 0) {
    ElMessage.info("所选策略的路线已生成");
    return;
  }

  loading.value = true;

  try {
    // 如果没有坐标，先通过前端高德 API 获取
    let originCoord = props.originCoord;
    let destCoord = props.destCoord;

    if (!originCoord) {
      originCoord = await geocodeAddress(props.origin);
    }
    if (!destCoord) {
      destCoord = await geocodeAddress(props.destination);
    }

    const response = await planRouteApi({
      origin: props.origin,
      destination: props.destination,
      originCoord: originCoord || undefined,
      destCoord: destCoord || undefined,
      strategies: newStrategies,
    });

    if (response.routes && response.routes.length > 0) {
      routeOptions.value = [...routeOptions.value, ...response.routes];
      newStrategies.forEach((s) => generatedStrategies.value.add(s));
      if (selectedRouteIndex.value === 0 && routeOptions.value.length > 0) {
        selectRoute(0);
      }
      ElMessage.success(`已新增 ${response.routes.length} 条路线`);
    } else {
      ElMessage.warning("未能规划出新路线");
    }
  } catch (e) {
    console.error("路线规划错误:", e);
    ElMessage.error("路线规划失败");
  } finally {
    loading.value = false;
  }
};

const selectRoute = (index: number) => {
  if (index >= 0 && index < routeOptions.value.length) {
    selectedRouteIndex.value = index;
  }
};

const clearRoutes = () => {
  routeOptions.value = [];
  selectedRouteIndex.value = 0;
  generatedStrategies.value.clear();
  emit("update", null);
};

// 暴露方法给父组件
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

.strategy-select {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.strategy-label {
  font-size: 14px;
  color: var(--el-text-color-regular);
}

.route-actions {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.route-info {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 0 16px;
  border-left: 1px solid var(--el-border-color-lighter);
}

.info-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: var(--el-text-color-regular);
}

.info-item .el-icon {
  font-size: 16px;
  color: var(--el-color-primary);
}

.route-options {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px dashed var(--el-border-color-lighter);
}

.route-options-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--el-text-color-primary);
  margin-bottom: 12px;
}

.route-options-list {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.route-option-item {
  flex: 1;
  min-width: 180px;
  max-width: 220px;
  padding: 12px;
  border: 2px solid var(--el-border-color-light);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  background: var(--el-bg-color);
}

.route-option-item:hover {
  border-color: var(--el-color-primary-light-3);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.route-option-item.is-selected {
  border-color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
}

.route-option-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.selected-badge {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  font-size: 12px;
  color: var(--el-color-primary);
  font-weight: 500;
}

.route-option-info {
  display: flex;
  gap: 12px;
  margin-bottom: 6px;
}

.route-stat {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--el-text-color-regular);
}

.route-stat .el-icon {
  font-size: 14px;
  color: var(--el-color-primary);
}

.route-option-desc {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.4;
}

@media (max-width: 768px) {
  .route-options-list {
    flex-direction: column;
  }
  .route-option-item {
    min-width: 100%;
    max-width: 100%;
  }
}
</style>
