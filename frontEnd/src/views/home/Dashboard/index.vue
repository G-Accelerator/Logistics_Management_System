<template>
  <div class="dashboard">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div class="welcome-content">
        <div class="welcome-text">
          <h2>
            {{ getGreeting() }}，{{ userStore.userInfo?.nickname || "用户" }}
          </h2>
          <p>欢迎使用物流轨迹追踪系统，今天也是高效的一天</p>
        </div>
        <div class="welcome-illustration">
          <svg viewBox="0 0 200 120" fill="none">
            <path
              d="M20 80 Q60 40 100 60 T180 50"
              stroke="rgba(255,255,255,0.3)"
              stroke-width="2"
              fill="none"
              stroke-dasharray="4 4"
            />
            <circle cx="20" cy="80" r="6" fill="#38bdf8" />
            <circle cx="100" cy="60" r="6" fill="#22d3ee" />
            <circle cx="180" cy="50" r="6" fill="#38bdf8" />
            <rect
              x="85"
              y="75"
              width="30"
              height="20"
              rx="3"
              fill="rgba(255,255,255,0.2)"
            />
            <circle cx="90" cy="100" r="4" fill="rgba(255,255,255,0.3)" />
            <circle cx="110" cy="100" r="4" fill="rgba(255,255,255,0.3)" />
          </svg>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="12" :sm="6">
        <div class="stat-card" @click="handleViewMore">
          <div class="stat-icon primary">
            <el-icon :size="28"><Document /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.total }}</div>
            <div class="stat-label">总订单数</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card">
          <div class="stat-icon info">
            <el-icon :size="28"><Van /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.shipping }}</div>
            <div class="stat-label">运输中</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card">
          <div class="stat-icon warning">
            <el-icon :size="28"><Clock /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.pending }}</div>
            <div class="stat-label">待发货</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card">
          <div class="stat-icon success">
            <el-icon :size="28"><CircleCheck /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.completed }}</div>
            <div class="stat-label">已完成</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 内容区 -->
    <el-row :gutter="20" class="content-row">
      <el-col :xs="24" :lg="16">
        <el-card class="recent-orders-card">
          <template #header>
            <div class="card-header">
              <div class="header-title">
                <el-icon class="header-icon"><List /></el-icon>
                <span>最近订单</span>
              </div>
              <el-button type="primary" link @click="handleViewMore">
                查看更多 <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
          </template>
          <el-table
            :data="recentOrders"
            v-loading="loading"
            class="orders-table"
          >
            <el-table-column prop="orderNo" label="订单号" width="180">
              <template #default="{ row }">
                <div class="order-no-cell">
                  <span>{{ row.orderNo }}</span>
                  <el-button
                    size="small"
                    :icon="DocumentCopy"
                    link
                    @click.stop="copyOrderNo(row.orderNo)"
                  />
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="receiverName" label="收货人" width="100" />
            <el-table-column
              prop="destination"
              label="目的地"
              min-width="150"
              show-overflow-tooltip
            />
            <el-table-column
              prop="status"
              label="状态"
              width="100"
              align="center"
            >
              <template #default="{ row }">
                <el-tag
                  :type="getStatusType(row.status)"
                  size="small"
                  effect="light"
                >
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="160" />
          </el-table>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="8">
        <el-card class="quick-actions-card">
          <template #header>
            <div class="card-header">
              <div class="header-title">
                <el-icon class="header-icon"><Operation /></el-icon>
                <span>快捷操作</span>
              </div>
            </div>
          </template>
          <div class="quick-actions">
            <el-button
              v-if="!isBuyer && !isSeller"
              class="action-btn"
              @click="handleCreateOrder"
            >
              <el-icon><Plus /></el-icon>
              <span>创建订单</span>
            </el-button>
            <el-button
              v-if="isBuyer"
              class="action-btn"
              @click="handleMyOrders"
            >
              <el-icon><Document /></el-icon>
              <span>我的订单</span>
            </el-button>
            <el-button
              v-if="isSeller"
              class="action-btn"
              @click="handleMyShipment"
            >
              <el-icon><Van /></el-icon>
              <span>我的发货</span>
            </el-button>
            <el-button class="action-btn" @click="handleTrack">
              <el-icon><Location /></el-icon>
              <span>查询物流</span>
            </el-button>
            <el-button class="action-btn" @click="loadData">
              <el-icon><Refresh /></el-icon>
              <span>刷新数据</span>
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import {
  Plus,
  Document,
  Van,
  Clock,
  CircleCheck,
  Refresh,
  DocumentCopy,
  Location,
  List,
  Operation,
  ArrowRight,
} from "@element-plus/icons-vue";
import {
  getOrders,
  getOrderStats,
  getBuyerOrders,
  getBuyerStats,
  getSellerOrders,
  getSellerStats,
} from "../../../api/order";
import { useUserStore } from "../../../store/user";

const router = useRouter();
const userStore = useUserStore();
const loading = ref(false);

const isBuyer = computed(
  () => userStore.userInfo?.role === "buyer" || !!userStore.buyerPhone,
);
const isSeller = computed(
  () => userStore.userInfo?.role === "seller" || !!userStore.sellerPhone,
);

const stats = ref({ total: 0, shipping: 0, pending: 0, completed: 0 });
const recentOrders = ref<any[]>([]);

const statusMap: Record<string, { text: string; type: string }> = {
  pending: { text: "待发货", type: "warning" },
  shipping: { text: "运输中", type: "primary" },
  completed: { text: "已完成", type: "success" },
  cancelled: { text: "已取消", type: "info" },
};

const getStatusType = (status: string) => statusMap[status]?.type || "info";
const getStatusText = (status: string) => statusMap[status]?.text || status;

const getGreeting = () => {
  const hour = new Date().getHours();
  if (hour < 12) return "早上好";
  if (hour < 18) return "下午好";
  return "晚上好";
};

const copyOrderNo = async (orderNo: string) => {
  try {
    await navigator.clipboard.writeText(orderNo);
    ElMessage.success("已复制");
  } catch {
    ElMessage.warning("复制失败");
  }
};

const loadData = async () => {
  loading.value = true;
  try {
    if (isBuyer.value) {
      const [statsData, ordersData] = await Promise.all([
        getBuyerStats(),
        getBuyerOrders({ page: 1, pageSize: 5 }),
      ]);
      stats.value = statsData;
      recentOrders.value = ordersData.data;
    } else if (isSeller.value) {
      const [statsData, ordersData] = await Promise.all([
        getSellerStats(),
        getSellerOrders({ page: 1, pageSize: 5 }),
      ]);
      stats.value = statsData;
      recentOrders.value = ordersData.data;
    } else {
      const [statsData, ordersData] = await Promise.all([
        getOrderStats(),
        getOrders({ page: 1, pageSize: 5 }),
      ]);
      stats.value = statsData;
      recentOrders.value = ordersData.data;
    }
  } catch (e) {
    console.error("加载数据失败", e);
  } finally {
    loading.value = false;
  }
};

const handleViewMore = () => {
  if (isBuyer.value) return router.push("/buyer/orders");
  if (isSeller.value) return router.push("/seller/shipment");
  return router.push("/order/list");
};

const handleCreateOrder = () => router.push("/order/create");
const handleMyOrders = () => router.push("/buyer/orders");
const handleMyShipment = () => router.push("/seller/shipment");
const handleTrack = () => router.push("/transport/track");

onMounted(() => loadData());
</script>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 欢迎横幅 */
.welcome-banner {
  background: var(--gradient-bg);
  border-radius: var(--radius-xl);
  padding: 24px 32px;
  color: #ffffff;
  position: relative;
  overflow: hidden;
}

.welcome-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  z-index: 1;
}

.welcome-text h2 {
  margin: 0 0 8px;
  font-size: 22px;
  font-weight: 600;
}

.welcome-text p {
  margin: 0;
  font-size: 14px;
  opacity: 0.8;
}

.welcome-illustration {
  width: 200px;
  height: 120px;
  opacity: 0.9;
}

.welcome-illustration svg {
  width: 100%;
  height: 100%;
}

/* 统计卡片 */
.stats-row {
  margin-bottom: 0;
}

.stat-card {
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  transition: all var(--transition-normal);
  border: 1px solid var(--border-color);
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
  border-color: var(--primary-light);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
}

.stat-icon.primary {
  background: var(--gradient-primary);
}
.stat-icon.info {
  background: linear-gradient(
    135deg,
    var(--info-color) 0%,
    var(--info-light) 100%
  );
}
.stat-icon.warning {
  background: linear-gradient(
    135deg,
    var(--warning-color) 0%,
    var(--warning-light) 100%
  );
}
.stat-icon.success {
  background: linear-gradient(
    135deg,
    var(--success-color) 0%,
    var(--success-light) 100%
  );
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: var(--text-tertiary);
  margin-top: 4px;
}

/* 内容卡片 */
.content-row {
  margin-top: 0;
}

.recent-orders-card,
.quick-actions-card {
  height: 100%;
  border-radius: var(--radius-lg);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: var(--text-primary);
}

.header-icon {
  color: var(--primary-color);
}

.order-no-cell {
  display: flex;
  align-items: center;
  gap: 4px;
}

.orders-table {
  --el-table-border-color: var(--border-color);
}

/* 快捷操作 */
.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.quick-actions .el-button + .el-button {
  margin-left: 0;
}

.action-btn {
  width: 100%;
  height: 52px;
  justify-content: flex-start;
  padding: 0 20px !important;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  background: var(--bg-primary);
  color: var(--text-primary);
  font-size: 14px;
  transition: all var(--transition-normal);
}

.action-btn .el-icon {
  margin-right: 12px !important;
  font-size: 18px;
}

.action-btn .el-icon + span {
  margin-left: 0 !important;
}

.action-btn:hover {
  border-color: var(--primary-color);
  color: var(--primary-color);
  background: rgba(14, 165, 233, 0.05);
  transform: translateX(4px);
}

@media (max-width: 768px) {
  .welcome-banner {
    padding: 20px;
  }

  .welcome-illustration {
    display: none;
  }

  .stat-card {
    padding: 16px;
  }

  .stat-value {
    font-size: 22px;
  }
}
</style>
