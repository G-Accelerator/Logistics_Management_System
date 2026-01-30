<template>
  <page-container
    title="创建订单"
    description="创建新的物流订单，自动规划推荐路线"
  >
    <!-- 订单表单 -->
    <el-card shadow="never" class="order-form-card">
      <el-form
        ref="formRef"
        :model="orderForm"
        :rules="rules"
        label-position="top"
      >
        <!-- 货物信息区块 -->
        <div class="form-section">
          <div class="section-title">
            <el-icon><Box /></el-icon>货物信息
          </div>
          <el-row :gutter="16">
            <el-col :xs="24" :sm="12" :md="5">
              <el-form-item label="货物名称" prop="cargoName">
                <el-input
                  v-model="orderForm.cargoName"
                  placeholder="请输入货物名称"
                />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="4">
              <el-form-item label="货物类型" prop="cargoType">
                <el-select
                  v-model="orderForm.cargoType"
                  placeholder="请选择"
                  style="width: 100%"
                >
                  <el-option
                    v-for="item in cargoTypes"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="8" :sm="4" :md="3">
              <el-form-item label="重量(kg)" prop="cargoWeight">
                <el-input-number
                  v-model="orderForm.cargoWeight"
                  :min="0"
                  :precision="2"
                  :controls="false"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :xs="8" :sm="4" :md="3">
              <el-form-item label="体积(m³)">
                <el-input-number
                  v-model="orderForm.cargoVolume"
                  :min="0"
                  :precision="2"
                  :controls="false"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :xs="8" :sm="4" :md="3">
              <el-form-item label="数量(件)">
                <el-input-number
                  v-model="orderForm.cargoQuantity"
                  :min="1"
                  :controls="false"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="备注">
                <el-input
                  v-model="orderForm.remark"
                  placeholder="订单备注信息"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <!-- 发货信息区块 -->
        <div class="form-section">
          <div class="section-title">
            <el-icon><Location /></el-icon>发货信息
          </div>
          <el-row :gutter="16">
            <el-col :xs="24" :sm="24" :md="12">
              <el-form-item label="起始地址" prop="origin">
                <el-autocomplete
                  v-model="orderForm.origin"
                  :fetch-suggestions="searchAddress"
                  placeholder="请输入起始地址"
                  style="width: 100%"
                  :trigger-on-focus="false"
                  @select="handleOriginSelect"
                >
                  <template #prefix
                    ><el-icon><Location /></el-icon
                  ></template>
                  <template #suffix>
                    <el-tooltip content="使用当前位置" placement="top">
                      <el-icon
                        class="location-btn"
                        @click="useCurrentLocation('origin')"
                        ><Aim
                      /></el-icon>
                    </el-tooltip>
                  </template>
                </el-autocomplete>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="发货人" prop="senderName">
                <el-input
                  v-model="orderForm.senderName"
                  placeholder="发货人姓名"
                  prefix-icon="User"
                />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="联系电话" prop="senderPhone">
                <el-input
                  v-model="orderForm.senderPhone"
                  placeholder="发货人电话"
                  prefix-icon="Phone"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <!-- 收货信息区块 -->
        <div class="form-section">
          <div class="section-title">
            <el-icon><Position /></el-icon>收货信息
          </div>
          <el-row :gutter="16">
            <el-col :xs="24" :sm="24" :md="12">
              <el-form-item label="目的地址" prop="destination">
                <el-autocomplete
                  v-model="orderForm.destination"
                  :fetch-suggestions="searchAddress"
                  placeholder="请输入目的地址"
                  style="width: 100%"
                  :trigger-on-focus="false"
                  @select="handleDestSelect"
                >
                  <template #prefix
                    ><el-icon><Position /></el-icon
                  ></template>
                  <template #suffix>
                    <el-tooltip content="使用当前位置" placement="top">
                      <el-icon
                        class="location-btn"
                        @click="useCurrentLocation('destination')"
                        ><Aim
                      /></el-icon>
                    </el-tooltip>
                  </template>
                </el-autocomplete>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="收货人" prop="receiverName">
                <el-input
                  v-model="orderForm.receiverName"
                  placeholder="收货人姓名"
                  prefix-icon="User"
                />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="联系电话" prop="receiverPhone">
                <el-input
                  v-model="orderForm.receiverPhone"
                  placeholder="收货人电话"
                  prefix-icon="Phone"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <!-- 规划路线区块 -->
        <div class="form-section route-section">
          <div class="section-title">
            <el-icon><Guide /></el-icon>路线规划
          </div>
          <RoutePlanner
            ref="routePlannerRef"
            :origin="orderForm.origin"
            :destination="orderForm.destination"
            :origin-coord="originCoord"
            :dest-coord="destCoord"
            @update="handleRouteUpdate"
            @show-map="showMapDialog = true"
          />
        </div>
      </el-form>
    </el-card>

    <!-- 物流站点卡片 -->
    <el-card v-if="trackPoints.length > 0" shadow="never" class="station-card">
      <template #header>
        <div class="station-header">
          <div class="station-title">
            <el-icon><Guide /></el-icon>
            <span>物流站点</span>
          </div>
        </div>
      </template>
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
          <div v-if="index < trackPoints.length - 1" class="station-line"></div>
        </div>
      </div>
    </el-card>

    <!-- 底部操作按钮 -->
    <div class="bottom-actions">
      <el-button
        type="success"
        size="large"
        @click="submitOrder"
        :loading="submitting"
      >
        <el-icon><Check /></el-icon>提交订单
      </el-button>
      <el-button size="large" @click="resetForm" :disabled="submitting"
        >重置</el-button
      >
    </div>

    <!-- 地图弹窗 -->
    <MapDialog
      v-model="showMapDialog"
      title="物流路线地图"
      :points="mapPoints"
    />
  </page-container>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import {
  ElMessage,
  ElMessageBox,
  type FormInstance,
  type FormRules,
} from "element-plus";
import {
  Location,
  Position,
  Aim,
  Box,
  Guide,
  Check,
} from "@element-plus/icons-vue";
import PageContainer from "../../../components/layout/PageContainer/index.vue";
import MapDialog from "../../../components/business/MapDialog/index.vue";
import RoutePlanner from "../../../components/business/RoutePlanner/index.vue";
import type { MapPoint } from "../../../components/business/MapDialog/types";
import type {
  RouteOptionData,
  RouteTrackPoint,
} from "../../../components/business/RoutePlanner/types";
import { createOrder } from "../../../api/order";

const formRef = ref<FormInstance>();
const routePlannerRef = ref<InstanceType<typeof RoutePlanner>>();
const router = useRouter();
const originCoord = ref<[number, number] | null>(null);
const destCoord = ref<[number, number] | null>(null);
const trackPoints = ref<RouteTrackPoint[]>([]);
const showMapDialog = ref(false);

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

const orderForm = reactive({
  orderNo: "",
  origin: "北京市朝阳区望京SOHO",
  destination: "上海市浦东新区陆家嘴",
  senderName: "张三",
  senderPhone: "13800138001",
  receiverName: "李四",
  receiverPhone: "13900139002",
  cargoName: "电子产品",
  cargoType: "normal",
  cargoWeight: 500 as number | null,
  cargoVolume: 10 as number | null,
  cargoQuantity: 20 as number | null,
  remark: "轻拿轻放",
});

const cargoTypes = [
  { label: "普通货物", value: "normal" },
  { label: "易碎品", value: "fragile" },
  { label: "冷链货物", value: "cold" },
  { label: "危险品", value: "dangerous" },
];

const rules: FormRules = {
  origin: [{ required: true, message: "请输入起始地址", trigger: "blur" }],
  destination: [{ required: true, message: "请输入目的地址", trigger: "blur" }],
  senderName: [
    { required: true, message: "请输入发货人姓名", trigger: "blur" },
  ],
  senderPhone: [
    { required: true, message: "请输入发货人电话", trigger: "blur" },
  ],
  receiverName: [
    { required: true, message: "请输入收货人姓名", trigger: "blur" },
  ],
  receiverPhone: [
    { required: true, message: "请输入收货人电话", trigger: "blur" },
  ],
  cargoName: [{ required: true, message: "请输入货物名称", trigger: "blur" }],
  cargoType: [{ required: true, message: "请选择货物类型", trigger: "change" }],
  cargoWeight: [{ required: true, message: "请输入货物重量", trigger: "blur" }],
};

const searchAddress = (query: string, cb: (r: any[]) => void) => {
  if (!query) {
    cb([]);
    return;
  }
  const ac = new (AMap as any).AutoComplete({ city: "全国" });
  ac.search(query, (s: string, r: any) => {
    cb(
      s === "complete" && r.tips
        ? r.tips
            .filter((t: any) => t.location)
            .map((t: any) => ({
              value: t.name + (t.district ? ` (${t.district})` : ""),
              location: t.location,
              name: t.name,
            }))
        : [],
    );
  });
};

const handleOriginSelect = (item: any) => {
  if (item.location) {
    originCoord.value = [item.location.lng, item.location.lat];
    orderForm.origin = item.name;
  }
};

const handleDestSelect = (item: any) => {
  if (item.location) {
    destCoord.value = [item.location.lng, item.location.lat];
    orderForm.destination = item.name;
  }
};

const useCurrentLocation = (type: "origin" | "destination") => {
  const geo = new (AMap as any).Geolocation({
    enableHighAccuracy: true,
    timeout: 10000,
  });
  ElMessage.info("正在获取当前位置...");
  geo.getCurrentPosition(async (s: string, r: any) => {
    if (s === "complete") {
      const { lng, lat } = r.position;
      let address = r.formattedAddress || "";
      if (!address) {
        address = await new Promise<string>((resolve) => {
          const geocoder = new (AMap as any).Geocoder();
          geocoder.getAddress([lng, lat], (status: string, result: any) => {
            resolve(
              status === "complete" && result.regeocode
                ? result.regeocode.formattedAddress || ""
                : "",
            );
          });
        });
      }
      if (type === "origin") {
        orderForm.origin = address || `${lng.toFixed(6)}, ${lat.toFixed(6)}`;
        originCoord.value = [lng, lat];
      } else {
        orderForm.destination =
          address || `${lng.toFixed(6)}, ${lat.toFixed(6)}`;
        destCoord.value = [lng, lat];
      }
      ElMessage.success(address ? "已获取当前位置" : "已获取坐标位置");
    } else {
      ElMessage.error("获取位置失败");
    }
  });
};

const handleRouteUpdate = (route: RouteOptionData | null) => {
  trackPoints.value = route?.trackPoints || [];
};

const submitting = ref(false);

const submitOrder = async () => {
  if (!formRef.value) return;
  await formRef.value.validate(async (valid) => {
    if (valid) {
      if (trackPoints.value.length === 0) {
        ElMessage.warning("请先规划路线");
        return;
      }
      submitting.value = true;
      try {
        // 调用后端创建订单，包含站点数据
        const result = await createOrder({
          cargoName: orderForm.cargoName,
          cargoType: orderForm.cargoType,
          origin: orderForm.origin,
          destination: orderForm.destination,
          senderName: orderForm.senderName,
          receiverName: orderForm.receiverName,
          senderPhone: orderForm.senderPhone,
          receiverPhone: orderForm.receiverPhone,
          trackPoints: trackPoints.value,
        });

        ElMessageBox.confirm(`订单编号：${result.orderNo}`, "订单创建成功", {
          confirmButtonText: "查看订单列表",
          cancelButtonText: "继续新建",
          type: "success",
          closeOnClickModal: false,
        })
          .then(() => {
            resetForm();
            router.push("/order/list");
          })
          .catch(() => {
            resetForm();
          });
      } catch (e) {
        ElMessage.error("创建订单失败");
      } finally {
        submitting.value = false;
      }
    }
  });
};

const resetForm = () => {
  formRef.value?.resetFields();
  orderForm.orderNo = "";
  originCoord.value = null;
  destCoord.value = null;
  trackPoints.value = [];
  routePlannerRef.value?.clearRoutes();
};

onMounted(() => {
  (AMap as any).plugin(
    ["AMap.Geocoder", "AMap.AutoComplete", "AMap.Geolocation", "AMap.Scale"],
    () => {},
  );
});
</script>

<style scoped>
.order-form-card {
  margin-bottom: 16px;
}

.form-section {
  padding: 16px 0;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.form-section:last-of-type {
  border-bottom: none;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 15px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-bottom: 16px;
}

.section-title .el-icon {
  color: var(--el-color-primary);
}

.route-section {
  border-bottom: none;
}

.bottom-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding: 20px 0;
  margin-top: 16px;
}

.location-btn {
  cursor: pointer;
  transition: color 0.2s;
}

.location-btn:hover {
  color: var(--el-color-primary);
}

/* 站点卡片样式 */
.station-card {
  margin-top: 16px;
}

.station-header {
  display: flex;
  justify-content: flex-start;
  align-items: center;
}

.station-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}

.station-title .el-icon {
  color: var(--el-color-primary);
  font-size: 18px;
}

/* 横向时间线样式 */
.station-timeline {
  display: flex;
  align-items: flex-start;
  padding: 20px 0;
  overflow-x: auto;
}

.station-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  min-width: 140px;
  flex: 1;
}

.station-dot {
  width: 20px;
  height: 20px;
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
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #fff;
}

.station-content {
  text-align: center;
  margin-top: 12px;
  padding: 0 8px;
}

.station-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-bottom: 4px;
}

.station-address {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.4;
  max-width: 120px;
  word-break: break-all;
}

.station-line {
  position: absolute;
  top: 10px;
  left: calc(50% + 10px);
  width: calc(100% - 20px);
  height: 2px;
  background: linear-gradient(90deg, #6366f1 50%, transparent 50%);
  background-size: 8px 2px;
}

.station-item.is-start .station-line {
  background: linear-gradient(90deg, #10b981 50%, transparent 50%);
  background-size: 8px 2px;
}

@media (max-width: 768px) {
  .station-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
