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
          <div class="route-content">
            <el-button
              type="primary"
              @click="planRoute"
              :loading="routeLoading"
            >
              <el-icon><Van /></el-icon>规划路线
            </el-button>
            <template v-if="trackPoints.length > 0">
              <div class="route-info">
                <span class="info-item">
                  <el-icon><Odometer /></el-icon>
                  <span>{{ (routeInfo.distance / 1000).toFixed(1) }} km</span>
                </span>
                <span class="info-item">
                  <el-icon><Timer /></el-icon>
                  <span>{{ formatDuration(routeInfo.duration) }}</span>
                </span>
                <span class="info-item">
                  <el-icon><Flag /></el-icon>
                  <span>{{ trackPoints.length }} 个站点</span>
                </span>
              </div>
              <el-button type="primary" plain @click="showMapDialog = true">
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
                  <span
                    v-if="selectedRouteIndex === index"
                    class="selected-badge"
                  >
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
    <el-dialog
      v-model="showMapDialog"
      title="物流路线地图"
      width="85%"
      top="3vh"
      destroy-on-close
      @opened="initMapInDialog"
    >
      <div id="map-container" class="dialog-map"></div>
    </el-dialog>
  </page-container>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from "vue";
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
  Van,
  Aim,
  MapLocation,
  Box,
  Guide,
  Odometer,
  Timer,
  Check,
  Flag,
} from "@element-plus/icons-vue";
import PageContainer from "../../../components/layout/PageContainer/index.vue";
import type { TrackPoint, RouteOption } from "./types";

export type { TrackPoint, RouteOption } from "./types";

const formRef = ref<FormInstance>();
const router = useRouter();
let map: any = null;
const originCoord = ref<[number, number] | null>(null);
const destCoord = ref<[number, number] | null>(null);
const routeInfo = reactive({ distance: 0, duration: 0 });
const routeLoading = ref(false);
let passedPolyline: any = null;
let markers: any[] = [];
const trackPoints = ref<TrackPoint[]>([]);
const showMapDialog = ref(false);
const routeOptions = ref<RouteOption[]>([]);
const selectedRouteIndex = ref(0);

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

// 模拟后端生成订单编号
const mockGenerateOrderNo = (): Promise<string> => {
  return new Promise((resolve) => {
    setTimeout(() => {
      const d = new Date();
      const orderNo = `ORD${d.getFullYear()}${String(d.getMonth() + 1).padStart(2, "0")}${String(d.getDate()).padStart(2, "0")}${Math.random().toString(36).substring(2, 8).toUpperCase()}`;
      resolve(orderNo);
    }, 500); // 模拟网络延迟
  });
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
    clearRouteCache();
  }
};

const handleDestSelect = (item: any) => {
  if (item.location) {
    destCoord.value = [item.location.lng, item.location.lat];
    orderForm.destination = item.name;
    clearRouteCache();
  }
};

// 清除路线缓存
const clearRouteCache = () => {
  trackPoints.value = [];
  routeOptions.value = [];
  selectedRouteIndex.value = 0;
  routeInfo.distance = 0;
  routeInfo.duration = 0;
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

      // 如果没有地址，使用逆地理编码获取
      if (!address) {
        address = await new Promise<string>((resolve) => {
          const geocoder = new (AMap as any).Geocoder();
          geocoder.getAddress([lng, lat], (status: string, result: any) => {
            if (status === "complete" && result.regeocode) {
              resolve(result.regeocode.formattedAddress || "");
            } else {
              resolve("");
            }
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
      clearRouteCache();
      ElMessage.success(address ? "已获取当前位置" : "已获取坐标位置");
    } else {
      ElMessage.error("获取位置失败");
    }
  });
};

const geocodeAddress = (addr: string): Promise<[number, number] | null> =>
  new Promise((resolve) => {
    new (AMap as any).Geocoder().getLocation(addr, (s: string, r: any) =>
      resolve(
        s === "complete" && r.geocodes.length
          ? [r.geocodes[0].location.lng, r.geocodes[0].location.lat]
          : null,
      ),
    );
  });

const planRoute = async () => {
  if (!orderForm.origin || !orderForm.destination) {
    ElMessage.warning("请先输入起始地和目的地");
    return;
  }
  routeLoading.value = true;
  trackPoints.value = [];
  routeOptions.value = [];
  selectedRouteIndex.value = 0;

  try {
    if (!originCoord.value)
      originCoord.value = await geocodeAddress(orderForm.origin);
    if (!destCoord.value)
      destCoord.value = await geocodeAddress(orderForm.destination);

    if (!originCoord.value || !destCoord.value) {
      ElMessage.error("地址解析失败");
      routeLoading.value = false;
      return;
    }

    // 定义不同的路线策略
    const policies = [
      {
        policy: (AMap as any).DrivingPolicy.LEAST_TIME,
        label: "最快路线",
        tagType: "primary" as const,
        description: "优先选择耗时最短的路线",
      },
      {
        policy: (AMap as any).DrivingPolicy.LEAST_DISTANCE,
        label: "最短路线",
        tagType: "success" as const,
        description: "优先选择距离最短的路线",
      },
      {
        policy: (AMap as any).DrivingPolicy.LEAST_FEE,
        label: "最省钱",
        tagType: "warning" as const,
        description: "优先选择收费最少的路线",
      },
      {
        policy: (AMap as any).DrivingPolicy.REAL_TRAFFIC,
        label: "躲避拥堵",
        tagType: "info" as const,
        description: "根据实时路况避开拥堵路段",
      },
    ];

    const routeResults: RouteOption[] = [];

    // 并行请求所有路线策略
    const routePromises = policies.map(
      ({ policy, label, tagType, description }) => {
        return new Promise<RouteOption | null>((resolve) => {
          const driving = new (AMap as any).Driving({ policy });
          driving.search(
            originCoord.value,
            destCoord.value,
            (status: string, result: any) => {
              if (status === "complete" && result.routes?.length > 0) {
                const route = result.routes[0];
                const pathPoints: [number, number][] = [];
                route.steps?.forEach((step: any) => {
                  step.path?.forEach((p: any) =>
                    pathPoints.push([p.lng, p.lat]),
                  );
                });
                resolve({
                  label,
                  tagType,
                  description,
                  distance: route.distance,
                  duration: route.time,
                  pathPoints,
                  policy,
                });
              } else {
                resolve(null);
              }
            },
          );
        });
      },
    );

    const results = await Promise.all(routePromises);

    // 过滤有效结果并去重（根据距离和时间判断是否重复）
    const seen = new Set<string>();
    results.forEach((r) => {
      if (r) {
        const key = `${Math.round(r.distance / 100)}-${Math.round(r.duration / 60)}`;
        if (!seen.has(key)) {
          seen.add(key);
          routeResults.push(r);
        }
      }
    });

    if (routeResults.length > 0) {
      routeOptions.value = routeResults;
      // 默认选择第一条路线（最快）
      await selectRoute(0);
      ElMessage.success(`已规划 ${routeResults.length} 条可选路线`);
    } else {
      ElMessage.error("路线规划失败");
    }
  } catch (e) {
    console.error("路线规划错误:", e);
    ElMessage.error("路线规划失败");
  } finally {
    routeLoading.value = false;
  }
};

const selectRoute = async (index: number) => {
  if (index < 0 || index >= routeOptions.value.length) return;

  selectedRouteIndex.value = index;
  const selected = routeOptions.value[index];
  if (!selected) return;

  routeInfo.distance = selected.distance;
  routeInfo.duration = selected.duration;

  // 使用缓存的站点数据，避免重复请求
  if (selected.trackPoints && selected.trackPoints.length > 0) {
    trackPoints.value = selected.trackPoints;
  } else {
    await generateTrackPoints(selected.pathPoints);
    // 缓存生成的站点数据
    selected.trackPoints = [...trackPoints.value];
  }
};

const generateTrackPoints = async (pathPoints: [number, number][]) => {
  const points: TrackPoint[] = [];
  const stationCount = Math.min(
    8,
    Math.max(4, Math.floor(pathPoints.length / 100)),
  );
  const step = Math.floor(pathPoints.length / (stationCount - 1));
  const indices = [0];
  for (let i = 1; i < stationCount - 1; i++)
    indices.push(Math.min(i * step, pathPoints.length - 1));
  indices.push(pathPoints.length - 1);

  const geocoder = new (AMap as any).Geocoder();
  const seenLocations = new Set<string>();

  for (let i = 0; i < indices.length; i++) {
    const idx = indices[i];
    if (idx === undefined) continue;
    const coord = pathPoints[idx];
    if (!coord) continue;

    const isStart = i === 0;
    const isEnd = i === indices.length - 1;

    let location: string;
    if (isStart) {
      location = orderForm.origin;
    } else if (isEnd) {
      location = orderForm.destination;
    } else {
      const address = await new Promise<string>((resolve) => {
        geocoder.getAddress(coord, (s: string, r: any) => {
          if (s === "complete" && r.regeocode) {
            const a = r.regeocode.addressComponent;
            resolve((a.city || a.province || "") + (a.district || ""));
          } else resolve("");
        });
      });
      location = address ? `${address}转运中心` : "";
    }

    // 跳过重复的中转站点（起点终点始终保留）
    if (!isStart && !isEnd) {
      const locationKey = location.replace(/转运中心$/, "");
      if (!location || seenLocations.has(locationKey)) continue;
      seenLocations.add(locationKey);
    }

    points.push({
      time: "",
      status: isStart ? "发货站点" : isEnd ? "收货站点" : "中转站点",
      location,
      lng: coord[0],
      lat: coord[1],
      passed: false,
      isCurrent: false,
    });
  }
  trackPoints.value = points;
};

const initMapInDialog = () => {
  if (map) {
    map.destroy();
    map = null;
  }
  map = new AMap.Map("map-container", {
    zoom: 5,
    center: [116.397428, 39.90923],
  });
  (AMap as any).plugin(["AMap.Scale"], () => map.addControl(new AMap.Scale()));
  drawTrack();
};

const drawTrack = () => {
  if (!map || trackPoints.value.length === 0) return;
  const points = trackPoints.value;

  if (points.length > 1) {
    passedPolyline = new AMap.Polyline({
      path: points.map((p) => [p.lng, p.lat]),
      strokeColor: "#6366f1",
      strokeWeight: 5,
      strokeOpacity: 0.9,
      strokeStyle: "dashed",
      lineJoin: "round",
      lineCap: "round",
    });
    map.add(passedPolyline);
  }

  points.forEach((pt, idx) => {
    const isStart = idx === 0;
    const isEnd = idx === points.length - 1;
    const color = isStart ? "#10b981" : isEnd ? "#f59e0b" : "#6366f1";
    const size = isStart || isEnd ? 16 : 10;

    // 站点圆点
    const m = new AMap.Marker({
      position: [pt.lng, pt.lat],
      content: `<div style="width:${size}px;height:${size}px;background:${color};border:${isStart || isEnd ? 3 : 2}px solid #fff;border-radius:50%;box-shadow:0 2px 6px rgba(0,0,0,0.3);"></div>`,
      offset: new AMap.Pixel(-size / 2, -size / 2),
    });
    map.add(m);
    markers.push(m);

    // 站点名称标签
    const labelText =
      pt.location.length > 15
        ? pt.location.substring(0, 15) + "..."
        : pt.location;
    const label = new AMap.Marker({
      position: [pt.lng, pt.lat],
      content: `<div style="background:${color};color:#fff;padding:3px 8px;border-radius:4px;font-size:11px;font-weight:500;box-shadow:0 2px 4px rgba(0,0,0,0.2);white-space:nowrap;max-width:150px;overflow:hidden;text-overflow:ellipsis;">${labelText}</div>`,
      offset: new AMap.Pixel(-20, -32),
    });
    map.add(label);
    markers.push(label);
  });
  map.setFitView(null, false, [60, 60, 60, 60]);
};

const formatDuration = (s: number) => {
  const h = Math.floor(s / 3600),
    m = Math.floor((s % 3600) / 60);
  return h > 0 ? `${h}小时${m}分钟` : `${m}分钟`;
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
        // 模拟后端生成订单编号
        const orderNo = await mockGenerateOrderNo();
        orderForm.orderNo = orderNo;

        console.log("提交订单:", {
          ...orderForm,
          trackPoints: trackPoints.value,
        });

        // 显示成功弹窗
        ElMessageBox.confirm(`订单编号：${orderNo}`, "订单创建成功", {
          confirmButtonText: "查看订单列表",
          cancelButtonText: "继续新建",
          type: "success",
          closeOnClickModal: false,
        })
          .then(() => {
            // 跳转到订单列表前清理缓存
            resetForm();
            router.push("/order/list");
          })
          .catch(() => {
            // 继续新建 - 重置表单
            resetForm();
          });
      } finally {
        submitting.value = false;
      }
    }
  });
};

const resetForm = () => {
  formRef.value?.resetFields();
  orderForm.orderNo = "";
  if (map) {
    map.destroy();
    map = null;
  }
  originCoord.value = null;
  destCoord.value = null;
  routeInfo.distance = 0;
  routeInfo.duration = 0;
  trackPoints.value = [];
  routeOptions.value = [];
  selectedRouteIndex.value = 0;
  markers = [];
  passedPolyline = null;
};

onMounted(() => {
  // 预加载 AMap 插件
  (AMap as any).plugin(
    [
      "AMap.Driving",
      "AMap.Geocoder",
      "AMap.AutoComplete",
      "AMap.Geolocation",
      "AMap.Scale",
    ],
    () => {},
  );
});

onUnmounted(() => {
  if (map) map.destroy();
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

.plan-action {
  display: flex;
  justify-content: flex-start;
  padding-top: 16px;
  border-top: 1px solid var(--el-border-color-lighter);
  margin-top: 8px;
}

.route-section {
  border-bottom: none;
}

.route-content {
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

/* 地图弹窗 */
.dialog-map {
  width: 100%;
  height: 65vh;
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

/* 响应式调整 */
@media (max-width: 768px) {
  .station-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .form-actions {
    flex-wrap: wrap;
  }

  .route-options-list {
    flex-direction: column;
  }

  .route-option-item {
    min-width: 100%;
  }
}

/* 多路线选择样式 */
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
</style>
