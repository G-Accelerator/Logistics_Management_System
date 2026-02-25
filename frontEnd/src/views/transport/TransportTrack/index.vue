<template>
  <div class="track-page">
    <div class="track-container">
      <!-- 左侧：查询和轨迹信息 -->
      <div class="track-sidebar">
        <!-- 查询表单 -->
        <el-card class="search-card">
          <div class="search-header">
            <el-icon class="search-icon"><Search /></el-icon>
            <span>物流查询</span>
          </div>
          <el-form :model="searchForm" label-position="top">
            <el-form-item label="查询方式">
              <el-radio-group v-model="searchForm.queryType" size="default">
                <el-radio-button value="orderNo">订单号</el-radio-button>
                <el-radio-button value="trackingNo">运单号</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-form-item
              v-if="searchForm.queryType === 'orderNo'"
              label="订单号"
            >
              <el-input
                v-model="searchForm.orderNo"
                placeholder="请输入订单号"
                clearable
                size="large"
                @keyup.enter="handleSearch"
              >
                <template #suffix>
                  <el-button
                    :icon="DocumentCopy"
                    link
                    @click="pasteOrderNo"
                    title="粘贴"
                  />
                </template>
              </el-input>
            </el-form-item>
            <el-form-item v-else label="运单号">
              <el-input
                v-model="searchForm.trackingNo"
                placeholder="请输入运单号"
                clearable
                size="large"
                @keyup.enter="handleSearch"
              >
                <template #suffix>
                  <el-button
                    :icon="DocumentCopy"
                    link
                    @click="pasteOrderNo"
                    title="粘贴"
                  />
                </template>
              </el-input>
            </el-form-item>
            <div class="search-actions">
              <el-button
                type="primary"
                size="large"
                :loading="loading"
                @click="handleSearch"
                class="search-btn"
              >
                <el-icon><Search /></el-icon>查询轨迹
              </el-button>
              <el-button size="large" @click="handleReset">重置</el-button>
            </div>
          </el-form>
        </el-card>

        <!-- 物流信息 -->
        <el-card v-if="trackInfo" class="info-card">
          <template #header>
            <div class="card-header">
              <div class="header-left">
                <el-icon class="header-icon"><Van /></el-icon>
                <span>物流信息</span>
              </div>
              <el-tag :type="getStatusType(trackInfo.status)" effect="light">
                {{ trackInfo.statusText }}
              </el-tag>
            </div>
          </template>
          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">运单号</span>
              <span class="info-value">{{ trackInfo.trackingNo }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">快递公司</span>
              <span class="info-value">{{
                trackInfo.expressCompanyName || "-"
              }}</span>
            </div>
            <div class="info-item full">
              <span class="info-label">发货地</span>
              <span class="info-value">{{ trackInfo.origin }}</span>
            </div>
            <div class="info-item full">
              <span class="info-label">目的地</span>
              <span class="info-value">{{ trackInfo.destination }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">发货时间</span>
              <span class="info-value">{{ trackInfo.sendTime || "-" }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">预计送达</span>
              <span class="info-value highlight">{{
                trackInfo.estimatedTime || "-"
              }}</span>
            </div>
          </div>
        </el-card>

        <!-- 轨迹时间线 -->
        <el-card v-if="trackPoints.length > 0" class="timeline-card">
          <template #header>
            <div class="card-header">
              <div class="header-left">
                <el-icon class="header-icon"><Clock /></el-icon>
                <span>物流轨迹</span>
              </div>
              <span class="track-count"
                >共 {{ trackPoints.length }} 条记录</span
              >
            </div>
          </template>
          <el-scrollbar class="timeline-scrollbar">
            <el-timeline>
              <el-timeline-item
                v-for="(point, index) in trackPoints"
                :key="index"
                :type="index === 0 ? 'primary' : 'info'"
                :hollow="index !== 0"
                :timestamp="point.time"
                placement="top"
              >
                <div class="timeline-content">
                  <div class="timeline-status" :class="{ active: index === 0 }">
                    {{ point.status }}
                  </div>
                  <div class="timeline-location">{{ point.location }}</div>
                </div>
              </el-timeline-item>
            </el-timeline>
          </el-scrollbar>
        </el-card>
      </div>

      <!-- 右侧：地图 -->
      <div class="track-map">
        <div id="amap-container" class="map-container"></div>
        <div class="map-controls">
          <el-button-group>
            <el-button :icon="ZoomIn" @click="handleZoomIn" />
            <el-button :icon="ZoomOut" @click="handleZoomOut" />
            <el-button :icon="Aim" @click="handleFitView" />
          </el-button-group>
        </div>
        <div v-if="!trackInfo" class="map-placeholder">
          <el-icon class="placeholder-icon"><Location /></el-icon>
          <p>输入订单号或运单号查询物流轨迹</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, onActivated } from "vue";
import { useRoute } from "vue-router";
import { ElMessage } from "element-plus";
import {
  ZoomIn,
  ZoomOut,
  Aim,
  DocumentCopy,
  Search,
  Van,
  Clock,
  Location,
} from "@element-plus/icons-vue";
import { getOrder, getTrackPoints, getStationStatus } from "../../../api/order";
import type { TrackInfo, TrackPoint } from "./types";
import type { StationInfo } from "../../../api/order/types";

defineOptions({ name: "TransportTrack" });

const route = useRoute();

const loading = ref(false);
const searchForm = reactive({
  orderNo: "",
  trackingNo: "",
  queryType: "orderNo" as "orderNo" | "trackingNo",
});
const trackInfo = ref<TrackInfo | null>(null);
const trackPoints = ref<TrackPoint[]>([]);

let map: any = null;
let passedPolyline: any = null;
let pendingPolyline: any = null;
let markers: any[] = [];
let currentMarker: any = null;
const mapReady = ref(false);

const checkRouteAndSearch = () => {
  const orderNo = route.query.orderNo;
  const trackingNo = route.query.trackingNo;
  if (trackingNo && typeof trackingNo === "string") {
    if (mapReady.value && !loading.value) {
      searchForm.queryType = "trackingNo";
      searchForm.trackingNo = trackingNo;
      handleSearch();
    }
  } else if (orderNo && typeof orderNo === "string") {
    if (mapReady.value && !loading.value) {
      searchForm.queryType = "orderNo";
      searchForm.orderNo = orderNo;
      handleSearch();
    }
  }
};

const getStatusType = (status: string) => {
  const map: Record<string, any> = {
    pending: "warning",
    shipping: "primary",
    completed: "success",
    cancelled: "info",
  };
  return map[status] || "info";
};

const pasteOrderNo = async () => {
  try {
    const text = await navigator.clipboard.readText();
    if (text) {
      if (searchForm.queryType === "orderNo") {
        searchForm.orderNo = text.trim();
      } else {
        searchForm.trackingNo = text.trim();
      }
      ElMessage.success("已粘贴");
    }
  } catch {
    ElMessage.warning("无法访问剪贴板");
  }
};

const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    pending: "待发货",
    shipping: "运输中",
    completed: "已完成",
    cancelled: "已取消",
  };
  return map[status] || status;
};

const initMap = () => {
  if (typeof AMap === "undefined") {
    ElMessage.warning("高德地图 API 未加载");
    return;
  }

  map = new AMap.Map("amap-container", {
    zoom: 5,
    zooms: [4, 12],
    center: [108.5, 34.5],
    mapStyle: "amap://styles/normal",
  });

  AMap.plugin(["AMap.Scale"], () => {
    map.addControl(new AMap.Scale());
  });

  map.on("complete", () => {
    mapReady.value = true;
    checkRouteAndSearch();
  });
};

const drawTrack = (points: TrackPoint[]) => {
  if (!map || points.length === 0) return;
  clearTrack();

  const passedPoints = points.filter((p) => p.passed);
  const pendingPoints = points.filter((p) => !p.passed);
  const currentPoint = points.find((p) => p.isCurrent);

  if (passedPoints.length > 1) {
    const passedPath = passedPoints.map((p) => [p.lng, p.lat]);
    passedPolyline = new AMap.Polyline({
      path: passedPath,
      strokeColor: "#0ea5e9",
      strokeWeight: 4,
      strokeOpacity: 1,
      lineJoin: "round",
      lineCap: "round",
    });
    map.add(passedPolyline);
  }

  if (pendingPoints.length > 0) {
    let pendingPath: number[][];
    if (currentPoint) {
      pendingPath = [
        [currentPoint.lng, currentPoint.lat],
        ...pendingPoints.map((p) => [p.lng, p.lat]),
      ];
    } else if (passedPoints.length > 0) {
      const lastPassed = passedPoints[passedPoints.length - 1]!;
      pendingPath = [
        [lastPassed.lng, lastPassed.lat],
        ...pendingPoints.map((p) => [p.lng, p.lat]),
      ];
    } else {
      pendingPath = pendingPoints.map((p) => [p.lng, p.lat]);
    }

    pendingPolyline = new AMap.Polyline({
      path: pendingPath,
      strokeColor: "#94a3b8",
      strokeWeight: 3,
      strokeOpacity: 0.6,
      strokeStyle: "dashed",
      lineJoin: "round",
      lineCap: "round",
    });
    map.add(pendingPolyline);
  }

  points.forEach((point, index) => {
    const isStart = index === 0;
    const isEnd = index === points.length - 1;
    const isCurrent = point.isCurrent;
    const isPassed = point.passed;

    if (isCurrent) {
      currentMarker = new AMap.Marker({
        position: [point.lng, point.lat],
        content: `<div style="width:18px;height:18px;background:#0ea5e9;border:3px solid #fff;border-radius:50%;box-shadow:0 2px 8px rgba(14,165,233,0.5);animation:pulse 1.5s infinite;"></div>`,
        offset: new AMap.Pixel(-9, -9),
      });
      map.add(currentMarker);
      markers.push(currentMarker);

      const label = new AMap.Marker({
        position: [point.lng, point.lat],
        content: `<div style="background:#0ea5e9;color:#fff;padding:4px 10px;border-radius:6px;font-size:12px;white-space:nowrap;box-shadow:0 2px 8px rgba(0,0,0,0.15);">当前: ${point.location}</div>`,
        offset: new AMap.Pixel(-60, -38),
      });
      map.add(label);
      markers.push(label);
    } else if (isStart || isEnd) {
      const color = isStart ? "#10b981" : "#f59e0b";
      const marker = new AMap.Marker({
        position: [point.lng, point.lat],
        content: `<div style="width:14px;height:14px;background:${color};border:2px solid #fff;border-radius:50%;box-shadow:0 1px 4px rgba(0,0,0,0.2);"></div>`,
        offset: new AMap.Pixel(-7, -7),
      });
      map.add(marker);
      markers.push(marker);

      const labelPrefix = isStart ? "发货: " : "收货: ";
      const extraInfo =
        isEnd && point.estimatedTime
          ? `<br/><span style="font-size:10px;opacity:0.8;">预计${point.estimatedTime}</span>`
          : "";
      const labelMarker = new AMap.Marker({
        position: [point.lng, point.lat],
        content: `<div style="background:${color};color:#fff;padding:5px 10px;border-radius:6px;font-size:12px;white-space:nowrap;text-align:center;line-height:1.4;box-shadow:0 2px 8px rgba(0,0,0,0.15);">${labelPrefix}${point.location}${extraInfo}</div>`,
        offset: new AMap.Pixel(-50, -42),
      });
      map.add(labelMarker);
      markers.push(labelMarker);
    } else {
      const marker = new AMap.Marker({
        position: [point.lng, point.lat],
        content: `<div style="width:8px;height:8px;background:${isPassed ? "#0ea5e9" : "#cbd5e1"};border:2px solid #fff;border-radius:50%;box-shadow:0 1px 3px rgba(0,0,0,0.2);"></div>`,
        offset: new AMap.Pixel(-4, -4),
      });
      map.add(marker);
      markers.push(marker);

      marker.on("click", () => {
        const infoWindow = new AMap.InfoWindow({
          content: `<div style="padding:8px;font-size:12px;"><div style="font-weight:500;color:${isPassed ? "#0ea5e9" : "#94a3b8"};">${point.status}</div><div style="color:#64748b;margin-top:4px;">${point.location}</div><div style="color:#94a3b8;font-size:11px;margin-top:2px;">${point.time || "待到达"}</div></div>`,
          offset: new AMap.Pixel(0, -5),
        });
        infoWindow.open(map, marker.getPosition());
      });
    }
  });

  map.setFitView(null, false, [50, 50, 50, 50]);
};

const clearTrack = () => {
  if (passedPolyline) {
    map.remove(passedPolyline);
    passedPolyline = null;
  }
  if (pendingPolyline) {
    map.remove(pendingPolyline);
    pendingPolyline = null;
  }
  if (markers.length > 0) {
    map.remove(markers);
    markers = [];
  }
  if (currentMarker) {
    map.remove(currentMarker);
    currentMarker = null;
  }
};

const handleSearch = async () => {
  const queryValue =
    searchForm.queryType === "orderNo"
      ? searchForm.orderNo
      : searchForm.trackingNo;
  if (!queryValue) {
    ElMessage.warning(
      searchForm.queryType === "orderNo" ? "请输入订单号" : "请输入运单号",
    );
    return;
  }

  loading.value = true;
  try {
    const order = await getOrder(queryValue, searchForm.queryType);
    if (!order) {
      ElMessage.warning("未找到该订单");
      return;
    }

    const orderStatus = order.status || "pending";
    const duration = order.duration || 0;
    const createTime = order.createTime
      ? new Date(order.createTime.replace(" ", "T"))
      : new Date();

    const formatDateTime = (d: Date) => {
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")} ${String(d.getHours()).padStart(2, "0")}:${String(d.getMinutes()).padStart(2, "0")}`;
    };

    const hasSent = orderStatus !== "pending";
    const sendTime = hasSent ? createTime : null;
    const estimatedArrival =
      sendTime && duration > 0
        ? new Date(sendTime.getTime() + duration * 1000)
        : null;

    trackInfo.value = {
      trackingNo: order.orderNo || queryValue,
      expressCompanyName: order.expressCompanyName || order.expressCompany,
      origin: order.origin,
      destination: order.destination,
      status: orderStatus,
      statusText: getStatusText(orderStatus),
      sendTime: hasSent ? order.createTime || "" : "",
      estimatedTime: estimatedArrival ? formatDateTime(estimatedArrival) : "",
    };

    const orderNo = order.orderNo || queryValue;
    let stationStatusList: StationInfo[] = [];
    try {
      stationStatusList = await getStationStatus(orderNo);
    } catch {
      console.warn("获取站点状态失败，使用轨迹点数据");
    }

    if (stationStatusList.length > 0) {
      let lastArrivedIndex = -1;
      for (let i = 0; i < stationStatusList.length; i++) {
        const station = stationStatusList[i];
        if (station && station.status === "arrived") {
          lastArrivedIndex = i;
        }
      }

      const allPoints: TrackPoint[] = stationStatusList.map((station, idx) => {
        const isStart = idx === 0;
        const isEnd = idx === stationStatusList.length - 1;
        const passed = station.status === "arrived";
        const isCurrent = idx === lastArrivedIndex && !isEnd;

        let status: string;
        if (isStart) status = passed ? "已发货" : "待发货";
        else if (isEnd) status = passed ? "已送达" : "待到达";
        else status = passed ? "已到达" : "待到达";

        const stationProgress = idx / (stationStatusList.length - 1);
        const stationTime =
          sendTime && duration > 0
            ? new Date(sendTime.getTime() + stationProgress * duration * 1000)
            : null;
        const estimatedTimeStr = stationTime
          ? formatDateTime(stationTime).slice(5)
          : undefined;

        return {
          time: station.arrivalTime || "",
          status,
          location: station.location,
          lng: station.lng,
          lat: station.lat,
          passed,
          isCurrent,
          estimatedTime: !passed && isEnd ? estimatedTimeStr : undefined,
        };
      });

      trackPoints.value = allPoints.filter((p) => p.passed).reverse();
      drawTrack(allPoints);
      ElMessage.success("查询成功");
      return;
    }

    const points = await getTrackPoints(orderNo);
    if (points.length === 0) {
      ElMessage.warning("该订单暂无轨迹数据");
      trackPoints.value = [];
      clearTrack();
      return;
    }

    let progress = 0;
    if (orderStatus === "pending") progress = -1;
    else if (orderStatus === "completed") progress = 1;
    else if (orderStatus === "shipping" && sendTime && duration > 0) {
      const now = new Date();
      const elapsed = now.getTime() - sendTime.getTime();
      progress = Math.min(elapsed / (duration * 1000), 0.99);
    } else if (orderStatus === "cancelled") progress = -1;

    const currentIdx =
      progress >= 0 ? Math.floor(progress * (points.length - 1)) : -1;

    const allPoints: TrackPoint[] = points.map((pt, idx) => {
      const isStart = idx === 0;
      const isEnd = idx === points.length - 1;
      const stationProgress = idx / (points.length - 1);
      const stationTime =
        sendTime && duration > 0
          ? new Date(sendTime.getTime() + stationProgress * duration * 1000)
          : null;
      const stationTimeStr = stationTime ? formatDateTime(stationTime) : "";
      const passed = idx <= currentIdx;
      const isCurrent = idx === currentIdx && progress < 1;

      let status: string;
      if (isStart) status = passed ? "已发货" : "待发货";
      else if (isEnd) status = passed ? "已送达" : "待到达";
      else status = passed ? "已到达" : "待到达";

      return {
        time: passed ? stationTimeStr : "",
        status,
        location: pt.location,
        lng: pt.lng,
        lat: pt.lat,
        passed,
        isCurrent,
        estimatedTime:
          !passed && isEnd && stationTimeStr
            ? stationTimeStr.slice(5)
            : undefined,
      };
    });

    trackPoints.value = allPoints.filter((p) => p.passed).reverse();
    drawTrack(allPoints);
    ElMessage.success("查询成功");
  } catch (error: any) {
    if (error?.response?.status === 404) ElMessage.warning("未找到该订单");
    else ElMessage.error("查询失败");
  } finally {
    loading.value = false;
  }
};

const handleReset = () => {
  searchForm.orderNo = "";
  searchForm.trackingNo = "";
  searchForm.queryType = "orderNo";
  trackInfo.value = null;
  trackPoints.value = [];
  clearTrack();
  if (map) map.setZoomAndCenter(5, [108.5, 34.5]);
};

const handleZoomIn = () => map?.zoomIn();
const handleZoomOut = () => map?.zoomOut();
const handleFitView = () => map?.setFitView();

onMounted(() => {
  (AMap as any).plugin(
    ["AMap.Geocoder", "AMap.AutoComplete", "AMap.Geolocation"],
    () => {},
  );
  setTimeout(() => initMap(), 100);
});

onActivated(() => checkRouteAndSearch());

onUnmounted(() => {
  if (map) {
    map.destroy();
    map = null;
  }
});
</script>

<style scoped>
.track-page {
  height: calc(100vh - 148px);
  min-height: 500px;
}

.track-container {
  display: flex;
  gap: 20px;
  height: 100%;
}

.track-sidebar {
  width: 380px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow: hidden;
}

/* 查询卡片 - 紧凑样式 */
.search-card {
  flex-shrink: 0;
  border-radius: var(--radius-lg);
}

.search-card :deep(.el-card__body) {
  padding: 16px;
}

.search-card :deep(.el-form-item) {
  margin-bottom: 12px;
}

.search-card :deep(.el-form-item__label) {
  padding-bottom: 4px;
  font-size: 13px;
}

/* 物流信息卡片 - 紧凑样式 */
.info-card {
  flex-shrink: 0;
  border-radius: var(--radius-lg);
}

.info-card :deep(.el-card__header) {
  padding: 10px 16px;
  background: linear-gradient(
    180deg,
    var(--bg-secondary) 0%,
    var(--bg-primary) 100%
  );
  border-bottom: 1px solid var(--border-color);
}

.info-card :deep(.el-card__body) {
  padding: 12px 16px;
}

/* 轨迹卡片 - 占据剩余空间 */
.timeline-card {
  flex: 1;
  min-height: 180px;
  display: flex;
  flex-direction: column;
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.timeline-card :deep(.el-card__header) {
  padding: 10px 16px;
  background: linear-gradient(
    180deg,
    var(--bg-secondary) 0%,
    var(--bg-primary) 100%
  );
  border-bottom: 1px solid var(--border-color);
}

.timeline-card :deep(.el-card__body) {
  flex: 1;
  padding: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.timeline-scrollbar {
  flex: 1;
  padding: 12px 16px;
}

.track-count {
  font-size: 12px;
  color: var(--text-tertiary);
  font-weight: 400;
}

/* 查询表单头部 */
.search-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 12px;
}

.search-icon {
  color: var(--primary-color);
  font-size: 18px;
}

.search-actions {
  display: flex;
  gap: 12px;
  margin-top: 4px;
}

.search-btn {
  flex: 1;
}

/* 卡片头部通用样式 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 14px;
}

.header-icon {
  color: var(--primary-color);
  font-size: 16px;
}

/* 物流信息网格 - 更紧凑 */
.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.info-item.full {
  grid-column: 1 / -1;
}

.info-label {
  font-size: 11px;
  color: var(--text-tertiary);
}

.info-value {
  font-size: 13px;
  color: var(--text-primary);
  line-height: 1.3;
}

.info-value.highlight {
  color: var(--primary-color);
  font-weight: 500;
}

/* 时间线内容 */
.timeline-content {
  line-height: 1.4;
}

.timeline-status {
  font-weight: 500;
  font-size: 13px;
  color: var(--text-secondary);
}

.timeline-status.active {
  color: var(--primary-color);
}

.timeline-location {
  font-size: 12px;
  color: var(--text-tertiary);
  margin-top: 2px;
}

/* 地图区域 */
.track-map {
  flex: 1;
  position: relative;
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-md);
  background: var(--bg-secondary);
}

.map-container {
  width: 100%;
  height: 100%;
}

.map-controls {
  position: absolute;
  top: 16px;
  right: 16px;
  z-index: 100;
}

.map-placeholder {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  color: var(--text-tertiary);
}

.placeholder-icon {
  font-size: 64px;
  color: var(--border-color);
  margin-bottom: 16px;
}

.map-placeholder p {
  margin: 0;
  font-size: 14px;
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(14, 165, 233, 0.5);
  }
  70% {
    box-shadow: 0 0 0 15px rgba(14, 165, 233, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(14, 165, 233, 0);
  }
}

@media (max-width: 1024px) {
  .track-container {
    flex-direction: column;
  }

  .track-sidebar {
    width: 100%;
  }

  .track-map {
    min-height: 400px;
  }
}
</style>
