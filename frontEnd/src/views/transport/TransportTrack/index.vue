<template>
  <page-container title="物流追踪" description="实时查看物流运输轨迹">
    <div class="track-container">
      <!-- 左侧：查询和轨迹信息 -->
      <div class="track-sidebar">
        <!-- 查询表单 -->
        <el-card class="search-card">
          <el-form :model="searchForm" label-width="80px">
            <el-form-item label="运单号">
              <el-input
                v-model="searchForm.trackingNo"
                placeholder="请输入运单号"
                clearable
                @keyup.enter="handleSearch"
              >
                <template #append>
                  <el-button
                    :icon="DocumentCopy"
                    @click="pasteOrderNo"
                    title="粘贴"
                  />
                </template>
              </el-input>
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                :loading="loading"
                @click="handleSearch"
              >
                查询轨迹
              </el-button>
              <el-button
                :icon="Refresh"
                :loading="loading"
                :disabled="!trackInfo"
                @click="handleRefresh"
                title="刷新"
              />
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 物流信息 -->
        <el-card v-if="trackInfo" class="info-card">
          <template #header>
            <div class="card-header">
              <span>物流信息</span>
              <el-tag :type="getStatusType(trackInfo.status)">
                {{ trackInfo.statusText }}
              </el-tag>
            </div>
          </template>
          <el-descriptions :column="1" size="small">
            <el-descriptions-item label="运单号">
              {{ trackInfo.trackingNo }}
            </el-descriptions-item>
            <el-descriptions-item label="快递公司">
              {{ trackInfo.expressCompanyName || "-" }}
            </el-descriptions-item>
            <el-descriptions-item label="发货地">
              {{ trackInfo.origin }}
            </el-descriptions-item>
            <el-descriptions-item label="目的地">
              {{ trackInfo.destination }}
            </el-descriptions-item>
            <el-descriptions-item label="发货时间">
              {{ trackInfo.sendTime || "-" }}
            </el-descriptions-item>
            <el-descriptions-item label="预计送达">
              {{ trackInfo.estimatedTime || "-" }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 轨迹时间线 -->
        <el-card v-if="trackPoints.length > 0" class="timeline-card">
          <template #header>
            <span>物流轨迹</span>
          </template>
          <el-scrollbar max-height="300px">
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
                  <div class="timeline-title">{{ point.status }}</div>
                  <div class="timeline-desc">{{ point.location }}</div>
                </div>
              </el-timeline-item>
            </el-timeline>
          </el-scrollbar>
        </el-card>
      </div>

      <!-- 右侧：地图 -->
      <div class="track-map">
        <div id="amap-container" class="map-container"></div>
        <!-- 地图控制按钮 -->
        <div class="map-controls">
          <el-button-group>
            <el-button :icon="ZoomIn" @click="handleZoomIn" />
            <el-button :icon="ZoomOut" @click="handleZoomOut" />
            <el-button :icon="Aim" @click="handleFitView" />
          </el-button-group>
        </div>
      </div>
    </div>
  </page-container>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from "vue";
import { ElMessage } from "element-plus";
import {
  ZoomIn,
  ZoomOut,
  Aim,
  DocumentCopy,
  Refresh,
} from "@element-plus/icons-vue";
import PageContainer from "../../../components/layout/PageContainer/index.vue";
import { getOrder, getTrackPoints } from "../../../api/order";
import type { TrackInfo, TrackPoint } from "./types";

// 组件名称，用于 keep-alive 缓存
defineOptions({ name: "TransportTrack" });

export type { TrackInfo, TrackPoint } from "./types";

// 快递公司映射
const expressCompanyMap: Record<string, string> = {
  sf: "顺丰速运",
  zto: "中通快递",
  yto: "圆通速递",
  yd: "韵达快递",
  sto: "申通快递",
  jd: "京东物流",
  deppon: "德邦快递",
};

// 状态
const loading = ref(false);
const searchForm = reactive({ trackingNo: "" });
const trackInfo = ref<TrackInfo | null>(null);
const trackPoints = ref<TrackPoint[]>([]);

// 地图相关
let map: any = null;
let passedPolyline: any = null;
let pendingPolyline: any = null;
let markers: any[] = [];
let currentMarker: any = null;

// 状态类型映射
const getStatusType = (status: string) => {
  const map: Record<string, any> = {
    pending: "warning",
    shipping: "primary",
    completed: "success",
    cancelled: "info",
  };
  return map[status] || "info";
};

// 粘贴订单号
const pasteOrderNo = async () => {
  try {
    const text = await navigator.clipboard.readText();
    if (text) {
      searchForm.trackingNo = text.trim();
      ElMessage.success("已粘贴");
    }
  } catch {
    ElMessage.warning("无法访问剪贴板");
  }
};

// 刷新当前查询
const handleRefresh = () => {
  if (searchForm.trackingNo) {
    handleSearch();
  }
};

// 状态文本映射
const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    pending: "待发货",
    shipping: "运输中",
    completed: "已完成",
    cancelled: "已取消",
  };
  return map[status] || status;
};

// 初始化地图
const initMap = () => {
  if (typeof AMap === "undefined") {
    ElMessage.warning("高德地图 API 未加载，请配置 API Key");
    console.error("请在 index.html 中配置高德地图 API Key");
    return;
  }

  map = new AMap.Map("amap-container", {
    zoom: 5,
    zooms: [4, 12],
    center: [108.5, 34.5],
    mapStyle: "amap://styles/normal",
  });

  map.addControl(new AMap.Scale());
};

// 绘制轨迹
const drawTrack = (points: TrackPoint[]) => {
  if (!map || points.length === 0) return;

  clearTrack();

  const passedPoints = points.filter((p) => p.passed);
  const pendingPoints = points.filter((p) => !p.passed);
  const currentPoint = points.find((p) => p.isCurrent);

  // 绘制已通过的路线（实线）
  if (passedPoints.length > 1) {
    const passedPath = passedPoints.map((p) => [p.lng, p.lat]);
    passedPolyline = new AMap.Polyline({
      path: passedPath,
      strokeColor: "#6366f1",
      strokeWeight: 4,
      strokeOpacity: 1,
      lineJoin: "round",
      lineCap: "round",
    });
    map.add(passedPolyline);
  }

  // 绘制待通过的路线（虚线）
  if (pendingPoints.length > 0) {
    let pendingPath: number[][];
    if (currentPoint) {
      // 有当前点，从当前点开始
      pendingPath = [
        [currentPoint.lng, currentPoint.lat],
        ...pendingPoints.map((p) => [p.lng, p.lat]),
      ];
    } else if (passedPoints.length > 0) {
      // 没有当前点但有已通过的点，从最后一个已通过点开始
      const lastPassed = passedPoints[passedPoints.length - 1]!;
      pendingPath = [
        [lastPassed.lng, lastPassed.lat],
        ...pendingPoints.map((p) => [p.lng, p.lat]),
      ];
    } else {
      // 全部都是待通过（未发货状态），显示完整路线
      pendingPath = pendingPoints.map((p) => [p.lng, p.lat]);
    }

    pendingPolyline = new AMap.Polyline({
      path: pendingPath,
      strokeColor: "#9ca3af",
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
        content: `<div style="
          width: 18px; height: 18px;
          background: #6366f1;
          border: 3px solid #fff;
          border-radius: 50%;
          box-shadow: 0 2px 8px rgba(99,102,241,0.5);
          animation: pulse 1.5s infinite;
        "></div>`,
        offset: new AMap.Pixel(-9, -9),
      });
      map.add(currentMarker);
      markers.push(currentMarker);

      const label = new AMap.Marker({
        position: [point.lng, point.lat],
        content: `<div style="
          background: #6366f1; color: #fff;
          padding: 3px 8px; border-radius: 4px;
          font-size: 11px; white-space: nowrap;
          box-shadow: 0 2px 6px rgba(0,0,0,0.2);
        ">当前: ${point.location}</div>`,
        offset: new AMap.Pixel(-60, -35),
      });
      map.add(label);
      markers.push(label);
    } else if (isStart || isEnd) {
      const color = isStart ? "#10b981" : "#f59e0b";
      const marker = new AMap.Marker({
        position: [point.lng, point.lat],
        content: `<div style="
          width: 14px; height: 14px;
          background: ${color};
          border: 2px solid #fff;
          border-radius: 50%;
          box-shadow: 0 1px 4px rgba(0,0,0,0.2);
        "></div>`,
        offset: new AMap.Pixel(-7, -7),
      });
      map.add(marker);
      markers.push(marker);

      const labelPrefix = isStart ? "发货: " : "收货: ";
      const extraInfo =
        isEnd && point.estimatedTime
          ? `<br/><span style="font-size:9px;">预计${point.estimatedTime}</span>`
          : "";
      const labelMarker = new AMap.Marker({
        position: [point.lng, point.lat],
        content: `<div style="
          background: ${color}; color: #fff;
          padding: 4px 8px; border-radius: 4px;
          font-size: 11px; white-space: nowrap;
          text-align: center; line-height: 1.4;
          box-shadow: 0 2px 6px rgba(0,0,0,0.15);
        ">${labelPrefix}${point.location}${extraInfo}</div>`,
        offset: new AMap.Pixel(-50, -38),
      });
      map.add(labelMarker);
      markers.push(labelMarker);
    } else {
      const marker = new AMap.Marker({
        position: [point.lng, point.lat],
        content: `<div style="
          width: 8px; height: 8px;
          background: ${isPassed ? "#6366f1" : "#d1d5db"};
          border: 2px solid #fff;
          border-radius: 50%;
          box-shadow: 0 1px 3px rgba(0,0,0,0.2);
        "></div>`,
        offset: new AMap.Pixel(-4, -4),
      });
      map.add(marker);
      markers.push(marker);

      marker.on("click", () => {
        const infoWindow = new AMap.InfoWindow({
          content: `<div style="padding: 6px; font-size: 12px;">
            <div style="font-weight: 500; color: ${isPassed ? "#6366f1" : "#9ca3af"}">${point.status}</div>
            <div style="color: #666;">${point.location}</div>
            <div style="color: #999; font-size: 11px;">${point.time || "待到达"}</div>
          </div>`,
          offset: new AMap.Pixel(0, -5),
        });
        infoWindow.open(map, marker.getPosition());
      });
    }
  });

  map.setFitView(null, false, [50, 50, 50, 50]);
};

// 清除轨迹
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

// 查询轨迹
const handleSearch = async () => {
  if (!searchForm.trackingNo) {
    ElMessage.warning("请输入运单号");
    return;
  }

  loading.value = true;
  try {
    // 获取订单信息
    const order = await getOrder(searchForm.trackingNo);
    if (!order) {
      ElMessage.warning("未找到该订单");
      return;
    }

    const orderStatus = order.status || "pending";
    const duration = order.duration || 0;
    const createTime = order.createTime
      ? new Date(order.createTime.replace(" ", "T"))
      : new Date();

    // 根据订单状态计算预计送达时间
    const formatDateTime = (d: Date) => {
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")} ${String(d.getHours()).padStart(2, "0")}:${String(d.getMinutes()).padStart(2, "0")}`;
    };

    // 待发货状态没有发货时间，预计送达也无法计算
    const hasSent = orderStatus !== "pending";
    const sendTime = hasSent ? createTime : null;
    const estimatedArrival =
      sendTime && duration > 0
        ? new Date(sendTime.getTime() + duration * 1000)
        : null;

    trackInfo.value = {
      trackingNo: order.orderNo || searchForm.trackingNo,
      expressCompanyName:
        expressCompanyMap[order.expressCompany || ""] || order.expressCompany,
      origin: order.origin,
      destination: order.destination,
      status: orderStatus,
      statusText: getStatusText(orderStatus),
      sendTime: hasSent ? order.createTime || "" : "",
      estimatedTime: estimatedArrival ? formatDateTime(estimatedArrival) : "",
    };

    // 获取站点数据
    const points = await getTrackPoints(searchForm.trackingNo);

    if (points.length === 0) {
      ElMessage.warning("该订单暂无轨迹数据");
      trackPoints.value = [];
      clearTrack();
      return;
    }

    // 根据订单状态决定物流进度
    let progress = 0;
    if (orderStatus === "pending") {
      // 待发货：没有任何进度
      progress = -1;
    } else if (orderStatus === "completed") {
      // 已完成：全部到达
      progress = 1;
    } else if (orderStatus === "shipping" && sendTime && duration > 0) {
      // 运输中：根据时间计算进度
      const now = new Date();
      const elapsed = now.getTime() - sendTime.getTime();
      progress = Math.min(elapsed / (duration * 1000), 0.99); // 最多99%，未完成
    } else if (orderStatus === "cancelled") {
      // 已取消：显示到取消时的进度（简化为0）
      progress = -1;
    }

    // 根据进度确定当前站点
    const currentIdx =
      progress >= 0 ? Math.floor(progress * (points.length - 1)) : -1;

    // 转换为地图需要的格式
    const allPoints: TrackPoint[] = points.map((pt, idx) => {
      const isStart = idx === 0;
      const isEnd = idx === points.length - 1;

      // 根据站点索引计算该站点的预计到达时间
      const stationProgress = idx / (points.length - 1);
      const stationTime =
        sendTime && duration > 0
          ? new Date(sendTime.getTime() + stationProgress * duration * 1000)
          : null;
      const stationTimeStr = stationTime ? formatDateTime(stationTime) : "";

      // 判断是否已经过
      const passed = idx <= currentIdx;
      const isCurrent = idx === currentIdx && progress < 1;

      let status: string;
      if (isStart) {
        status = passed ? "已发货" : "待发货";
      } else if (isEnd) {
        status = passed ? "已送达" : "待到达";
      } else {
        status = passed ? "已到达" : "待到达";
      }

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
    if (error?.response?.status === 404) {
      ElMessage.warning("未找到该订单");
    } else {
      ElMessage.error("查询失败");
    }
  } finally {
    loading.value = false;
  }
};

// 重置
const handleReset = () => {
  searchForm.trackingNo = "";
  trackInfo.value = null;
  trackPoints.value = [];
  clearTrack();
  if (map) {
    map.setZoomAndCenter(5, [116.397428, 39.90923]);
  }
};

// 地图控制
const handleZoomIn = () => map?.zoomIn();
const handleZoomOut = () => map?.zoomOut();
const handleFitView = () => map?.setFitView();

// 生命周期
onMounted(() => {
  setTimeout(initMap, 100);
});

onUnmounted(() => {
  if (map) {
    map.destroy();
    map = null;
  }
});
</script>

<style scoped>
.track-container {
  display: flex;
  gap: 20px;
  height: calc(100vh - 220px);
  min-height: 500px;
}

.track-sidebar {
  width: 360px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
  overflow-y: auto;
}

.track-map {
  flex: 1;
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
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

.search-card :deep(.el-card__body) {
  padding-bottom: 0;
}

.info-card .card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.timeline-card :deep(.el-card__body) {
  padding: 16px;
}

.timeline-content {
  line-height: 1.5;
}

.timeline-title {
  font-weight: 500;
  color: var(--text-primary);
}

.timeline-desc {
  font-size: 12px;
  color: var(--text-secondary);
}

:deep(.el-timeline-item__timestamp) {
  font-size: 12px;
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(99, 102, 241, 0.5);
  }
  70% {
    box-shadow: 0 0 0 15px rgba(99, 102, 241, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(99, 102, 241, 0);
  }
}
</style>
