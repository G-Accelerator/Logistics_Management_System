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

        <el-scrollbar
          v-if="trackInfo || trackPoints.length > 0"
          class="sidebar-body-scroll"
        >
          <div class="sidebar-scroll-inner">
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

        <el-card
          v-if="trackInfo && trackInfo.vehiclePlateNumber"
          class="vehicle-card"
        >
          <template #header>
            <div class="card-header">
              <div class="header-left">
                <el-icon class="header-icon"><Van /></el-icon>
                <span>承运车辆</span>
              </div>
              <el-tag
                :type="trackInfo.vehicleOnline ? 'success' : 'danger'"
                size="small"
              >
                {{ trackInfo.vehicleOnline ? "在线" : "离线" }}
              </el-tag>
            </div>
          </template>
          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">车牌</span>
              <span class="info-value">{{ trackInfo.vehiclePlateNumber }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">类型</span>
              <span class="info-value">{{ trackInfo.vehicleType || "-" }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">驾驶员</span>
              <span class="info-value">{{
                trackInfo.vehicleDriverName || "-"
              }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">电话</span>
              <span class="info-value">{{
                trackInfo.vehicleDriverPhone || "-"
              }}</span>
            </div>
            <div class="info-item full">
              <span class="info-label">当前车速</span>
              <span class="info-value highlight"
                >{{ trackInfo.currentSpeedKmh ?? "-" }} km/h</span
              >
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
          <div class="timeline-inner">
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
                  <div
                    v-if="
                      trackInfo?.vehiclePlateNumber &&
                      trackInfo.currentSpeedKmh != null
                    "
                    class="timeline-vehicle-speed"
                  >
                    车速 {{ trackInfo.currentSpeedKmh }} km/h ·
                    {{ trackInfo.vehiclePlateNumber }}
                  </div>
                </div>
              </el-timeline-item>
            </el-timeline>
          </div>
        </el-card>
          </div>
        </el-scrollbar>
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
import {
  buildRouteThroughWaypoints,
  coordsFromTrackPoints,
} from "../../../utils/trackPath";
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

/** 与车辆监控页一致：在途数据定时刷新 */
const TRACK_POLL_MS = 45000;
let refreshTimer: ReturnType<typeof setInterval> | null = null;

let map: any = null;
let passedPolyline: any = null;
let pendingPolyline: any = null;
let routeOverlays: any[] = [];
let markers: any[] = [];
let currentMarker: any = null;
const mapReady = ref(false);
const getQueryValue = () =>
  searchForm.queryType === "orderNo"
    ? searchForm.orderNo.trim()
    : searchForm.trackingNo.trim();

const stopAutoRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer);
    refreshTimer = null;
  }
};

const scheduleAutoRefresh = (orderStatus: string) => {
  stopAutoRefresh();
  if (orderStatus === "completed" || orderStatus === "cancelled") {
    return;
  }
  refreshTimer = setInterval(() => {
    if (!getQueryValue()) return;
    handleSearch({ silent: true });
  }, TRACK_POLL_MS);
};

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

const addRoutePolyline = (
  path: [number, number][],
  options: Record<string, unknown>,
) => {
  if (!map || path.length < 2) return null;
  const line = new AMap.Polyline({
    path,
    lineJoin: "round",
    lineCap: "round",
    showDir: true,
    dirColor: "#ffffff",
    dirOpacity: 0.95,
    ...options,
  });
  map.add(line);
  routeOverlays.push(line);
  return line;
};

const drawTrack = (points: TrackPoint[]) => {
  if (!map || points.length === 0) return;
  clearTrack();

  const passedPoints = points.filter((p) => p.passed);
  const pendingPoints = points.filter((p) => !p.passed);
  const currentPoint = points.find((p) => p.isCurrent);

  if (passedPoints.length > 1) {
    const passedPath = buildRouteThroughWaypoints(
      coordsFromTrackPoints(passedPoints),
    );
    passedPolyline = addRoutePolyline(passedPath, {
      strokeColor: "#0ea5e9",
      strokeWeight: 5,
      strokeOpacity: 1,
    });
  }

  if (pendingPoints.length > 0) {
    let pendingWaypoints: [number, number][];
    if (currentPoint) {
      pendingWaypoints = [
        [currentPoint.lng, currentPoint.lat],
        ...coordsFromTrackPoints(pendingPoints),
      ];
    } else if (passedPoints.length > 0) {
      const lastPassed = passedPoints[passedPoints.length - 1]!;
      pendingWaypoints = [
        [lastPassed.lng, lastPassed.lat],
        ...coordsFromTrackPoints(pendingPoints),
      ];
    } else {
      pendingWaypoints = coordsFromTrackPoints(pendingPoints);
    }

    if (pendingWaypoints.length > 1) {
      const pendingPath = buildRouteThroughWaypoints(pendingWaypoints);
      pendingPolyline = addRoutePolyline(pendingPath, {
        strokeColor: "#94a3b8",
        strokeWeight: 4,
        strokeOpacity: 0.75,
        strokeStyle: "dashed",
      });
    }
  }

  points.forEach((point, index) => {
    const isStart = index === 0;
    const isEnd = index === points.length - 1;
    const isCurrent = point.isCurrent;
    const isPassed = point.passed;

    if (isCurrent) {
      currentMarker = new AMap.Marker({
        position: [point.lng, point.lat],
        content: `<div class="track-dot track-dot-current"></div>`,
        offset: new AMap.Pixel(-13, -13),
      });
      map.add(currentMarker);
      markers.push(currentMarker);

      const label = new AMap.Marker({
        position: [point.lng, point.lat],
        content: `<div class="track-label track-label-current">当前: ${point.location}</div>`,
        offset: new AMap.Pixel(-68, -44),
      });
      map.add(label);
      markers.push(label);
    } else if (isStart || isEnd) {
      const color = isStart ? "#10b981" : "#f59e0b";
      const marker = new AMap.Marker({
        position: [point.lng, point.lat],
        content: `<div class="track-dot track-dot-endpoint" style="background:${color};"></div>`,
        offset: new AMap.Pixel(-11, -11),
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
        content: `<div class="track-label" style="background:${color};">${labelPrefix}${point.location}${extraInfo}</div>`,
        offset: new AMap.Pixel(-54, -48),
      });
      map.add(labelMarker);
      markers.push(labelMarker);
    } else {
      const marker = new AMap.Marker({
        position: [point.lng, point.lat],
        content: `<div class="track-dot track-dot-waypoint" style="background:${isPassed ? "#0ea5e9" : "#cbd5e1"};"></div>`,
        offset: new AMap.Pixel(-7, -7),
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

  const fitTargets = [...routeOverlays, ...markers];
  if (fitTargets.length > 0) {
    map.setFitView(fitTargets, false, [56, 56, 56, 56]);
  }
};

const clearTrack = () => {
  if (!map) return;
  if (routeOverlays.length > 0) {
    map.remove(routeOverlays);
    routeOverlays = [];
  }
  if (passedPolyline) {
    passedPolyline = null;
  }
  if (pendingPolyline) {
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

const handleSearch = async (options?: { silent?: boolean }) => {
  const silent = options?.silent === true;
  const queryValue = getQueryValue();
  if (!queryValue) {
    if (!silent) {
      ElMessage.warning(
        searchForm.queryType === "orderNo" ? "请输入订单号" : "请输入运单号",
      );
    }
    return;
  }

  if (!silent) loading.value = true;
  try {
    const order = await getOrder(queryValue, searchForm.queryType);
    if (!order) {
      if (!silent) ElMessage.warning("未找到该订单");
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
      trackingNo: order.trackingNo || order.orderNo || queryValue,
      expressCompanyName: order.expressCompanyName || order.expressCompany,
      origin: order.origin,
      destination: order.destination,
      status: orderStatus,
      statusText: getStatusText(orderStatus),
      sendTime: hasSent ? order.createTime || "" : "",
      estimatedTime: estimatedArrival ? formatDateTime(estimatedArrival) : "",
      vehiclePlateNumber: order.vehiclePlateNumber,
      vehicleType: order.vehicleType,
      vehicleDriverName: order.vehicleDriverName,
      vehicleDriverPhone: order.vehicleDriverPhone,
      vehicleOnline: order.vehicleOnline,
      currentSpeedKmh: order.currentSpeedKmh,
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
      scheduleAutoRefresh(orderStatus);
      if (!silent) ElMessage.success("查询成功");
      return;
    }

    const points = await getTrackPoints(orderNo);
    if (points.length === 0) {
      if (!silent) {
        ElMessage.warning("该订单暂无轨迹数据");
        trackPoints.value = [];
        clearTrack();
      }
      stopAutoRefresh();
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
    scheduleAutoRefresh(orderStatus);
    if (!silent) ElMessage.success("查询成功");
  } catch (error: any) {
    if (!silent) {
      if (error?.response?.status === 404) ElMessage.warning("未找到该订单");
      else ElMessage.error("查询失败");
    }
  } finally {
    if (!silent) loading.value = false;
  }
};

const handleReset = () => {
  stopAutoRefresh();
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
  stopAutoRefresh();
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
  min-height: 0;
}

.track-sidebar {
  width: min(400px, 38vw);
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 0;
  height: 100%;
  overflow: hidden;
}

/* 查询以下整块可纵向滚动，避免信息被裁切 */
.sidebar-body-scroll {
  flex: 1 1 0;
  min-height: 0;
}

.sidebar-body-scroll :deep(.el-scrollbar__wrap) {
  overflow-x: hidden;
}

.sidebar-scroll-inner {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 2px 8px 0;
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

/* 轨迹卡片：高度随内容增长，由外侧 sidebar-body-scroll 统一滚动 */
.timeline-card {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  border-radius: var(--radius-lg);
  overflow: visible;
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
  padding: 0;
}

.timeline-inner {
  padding: 12px 16px 16px;
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

.timeline-vehicle-speed {
  font-size: 11px;
  color: var(--text-tertiary);
  margin-top: 4px;
}

.vehicle-card {
  margin-top: 0;
}

/* 地图区域 */
.track-map {
  flex: 1;
  min-width: 0;
  min-height: 0;
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

:global(.track-dot) {
  border: 3px solid #fff;
  border-radius: 50%;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
  box-sizing: border-box;
}

:global(.track-dot-current) {
  width: 26px;
  height: 26px;
  background: #0ea5e9;
  animation: pulse 1.5s infinite;
}

:global(.track-dot-endpoint) {
  width: 22px;
  height: 22px;
}

:global(.track-dot-waypoint) {
  width: 14px;
  height: 14px;
}

:global(.track-label) {
  color: #fff;
  padding: 5px 11px;
  border-radius: 6px;
  font-size: 12px;
  white-space: nowrap;
  line-height: 1.4;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

:global(.track-label-current) {
  background: #0ea5e9;
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
