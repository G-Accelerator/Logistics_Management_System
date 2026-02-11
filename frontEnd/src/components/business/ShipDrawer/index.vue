<template>
  <el-drawer v-model="visible" title="发货" size="800px" @closed="handleClosed">
    <div class="ship-drawer-content">
      <!-- 订单信息 -->
      <el-descriptions :column="2" border size="small" class="order-info">
        <el-descriptions-item label="订单号">{{
          order?.orderNo
        }}</el-descriptions-item>
        <el-descriptions-item label="快递公司">{{
          expressCompanyName
        }}</el-descriptions-item>
        <el-descriptions-item label="发货地址">{{
          order?.origin
        }}</el-descriptions-item>
        <el-descriptions-item label="收货地址">{{
          order?.destination
        }}</el-descriptions-item>
      </el-descriptions>

      <!-- 路线规划 -->
      <div class="route-section">
        <div class="section-title">选择运输路线</div>
        <route-planner
          ref="routePlannerRef"
          :origin="order?.origin || ''"
          :destination="order?.destination || ''"
          :origin-coord="orderOriginCoord"
          :dest-coord="orderDestCoord"
          @update="handleRouteUpdate"
          @show-map="showMapDialog = true"
        />
      </div>

      <!-- 站点预览 -->
      <div v-if="trackPoints.length > 0" class="station-section">
        <div class="section-title">物流站点预览</div>
        <div class="station-timeline">
          <div
            v-for="(point, index) in trackPoints"
            :key="index"
            class="station-item"
            :class="{
              'is-start': index === 0,
              'is-end': index === trackPoints.length - 1,
            }"
          >
            <div class="station-dot">
              <span class="dot-inner"></span>
            </div>
            <div class="station-content">
              <div class="station-name">{{ point.status }}</div>
              <div class="station-address">{{ point.location }}</div>
            </div>
            <div
              v-if="index < trackPoints.length - 1"
              class="station-line"
            ></div>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button
        type="primary"
        :loading="shipping"
        :disabled="!selectedRoute"
        @click="handleShip"
      >
        确认发货
      </el-button>
    </template>

    <!-- 地图弹窗 -->
    <map-dialog
      v-model="showMapDialog"
      title="物流路线地图"
      :points="mapPoints"
    />
  </el-drawer>
</template>

<script setup lang="ts">
import { ref, computed } from "vue";
import { ElMessage } from "element-plus";
import RoutePlanner from "../RoutePlanner/index.vue";
import MapDialog from "../MapDialog/index.vue";
import { shipOrder } from "../../../api/order";
import type { Order } from "../../../api/order/types";
import type { RouteOptionData, RouteTrackPoint } from "../RoutePlanner/types";
import type { MapPoint } from "../MapDialog/types";

const props = defineProps<{
  modelValue: boolean;
  order: Order | null;
}>();

const emit = defineEmits<{
  (e: "update:modelValue", value: boolean): void;
  (e: "success", order: Order): void;
}>();

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit("update:modelValue", val),
});

const routePlannerRef = ref<InstanceType<typeof RoutePlanner> | null>(null);
const selectedRoute = ref<RouteOptionData | null>(null);
const trackPoints = ref<RouteTrackPoint[]>([]);
const shipping = ref(false);
const showMapDialog = ref(false);

// 快递公司名称（直接使用后端返回的名称）
const expressCompanyName = computed(() => {
  return props.order?.expressCompanyName || props.order?.expressCompany || "-";
});

// 订单中的坐标
const orderOriginCoord = computed<[number, number] | null>(() => {
  const o = props.order;
  return o?.originLng && o?.originLat ? [o.originLng, o.originLat] : null;
});

const orderDestCoord = computed<[number, number] | null>(() => {
  const o = props.order;
  return o?.destLng && o?.destLat ? [o.destLng, o.destLat] : null;
});

// 地图点位
const mapPoints = computed<MapPoint[]>(() => {
  return trackPoints.value.map((pt, idx) => ({
    lng: pt.lng,
    lat: pt.lat,
    name: pt.status,
    address: pt.location,
    isStart: idx === 0,
    isEnd: idx === trackPoints.value.length - 1,
  }));
});

// 路线更新
const handleRouteUpdate = (route: RouteOptionData | null) => {
  selectedRoute.value = route;
  trackPoints.value = route?.trackPoints || [];
};

// 确认发货
const handleShip = async () => {
  if (!props.order?.orderNo || !selectedRoute.value) {
    ElMessage.warning("请先选择运输路线");
    return;
  }

  shipping.value = true;
  try {
    const result = await shipOrder(props.order.orderNo, {
      trackPoints: selectedRoute.value.trackPoints,
      duration: selectedRoute.value.duration,
    });
    ElMessage.success(`发货成功，运单号：${result.trackingNo}`);
    emit("success", result);
    visible.value = false;
  } catch (error: any) {
    ElMessage.error(error?.message || "发货失败");
  } finally {
    shipping.value = false;
  }
};

// 抽屉关闭时清理
const handleClosed = () => {
  selectedRoute.value = null;
  trackPoints.value = [];
  routePlannerRef.value?.clearRoutes();
};
</script>

<style scoped>
.ship-drawer-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.order-info {
  margin-bottom: 8px;
}

.route-section,
.station-section {
  padding: 16px;
  background: var(--el-fill-color-lighter);
  border-radius: 8px;
}

.section-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--el-text-color-primary);
  margin-bottom: 16px;
}

/* 站点时间线样式 */
.station-timeline {
  display: flex;
  align-items: flex-start;
  padding: 16px 0;
  overflow-x: auto;
}

.station-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  min-width: 120px;
  flex: 1;
}

.station-dot {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1;
}

.station-item.is-start .station-dot {
  background: #10b981;
}

.station-item.is-end .station-dot {
  background: #f59e0b;
}

.station-item:not(.is-start):not(.is-end) .station-dot {
  background: #6366f1;
}

.dot-inner {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #fff;
}

.station-content {
  text-align: center;
  margin-top: 10px;
  padding: 0 6px;
}

.station-name {
  font-size: 12px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-bottom: 2px;
}

.station-address {
  font-size: 11px;
  color: var(--el-text-color-secondary);
  line-height: 1.3;
  max-width: 100px;
  word-break: break-all;
}

.station-line {
  position: absolute;
  top: 9px;
  left: calc(50% + 9px);
  width: calc(100% - 18px);
  height: 2px;
  background: linear-gradient(90deg, #6366f1 50%, transparent 50%);
  background-size: 6px 2px;
}

.station-item.is-start .station-line {
  background: linear-gradient(90deg, #10b981 50%, transparent 50%);
  background-size: 6px 2px;
}
</style>
