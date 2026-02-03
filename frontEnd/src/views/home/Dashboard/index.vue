<template>
  <div class="dashboard">
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div
              class="stat-icon"
              style="
                background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
              "
            >
              <el-icon :size="30"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.total }}</div>
              <div class="stat-label">总订单数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div
              class="stat-icon"
              style="
                background: linear-gradient(135deg, #10b981 0%, #34d399 100%);
              "
            >
              <el-icon :size="30"><Van /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.shipping }}</div>
              <div class="stat-label">运输中</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div
              class="stat-icon"
              style="
                background: linear-gradient(135deg, #f59e0b 0%, #fbbf24 100%);
              "
            >
              <el-icon :size="30"><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.pending }}</div>
              <div class="stat-label">待发货</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div
              class="stat-icon"
              style="
                background: linear-gradient(135deg, #10b981 0%, #34d399 100%);
              "
            >
              <el-icon :size="30"><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.completed }}</div>
              <div class="stat-label">已完成</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="content-row">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最近订单</span>
              <el-button type="primary" link @click="handleViewMore"
                >查看更多</el-button
              >
            </div>
          </template>
          <el-table
            :data="recentOrders"
            style="width: 100%"
            v-loading="loading"
          >
            <el-table-column prop="orderNo" label="订单号" width="180" />
            <el-table-column prop="receiverName" label="收货人" width="100" />
            <el-table-column
              prop="destination"
              label="目的地"
              min-width="150"
              show-overflow-tooltip
            />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)" size="small">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="160" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header"><span>快捷操作</span></div>
          </template>
          <div class="quick-actions">
            <el-button
              v-if="!isBuyer"
              type="primary"
              :icon="Plus"
              class="action-btn"
              @click="handleCreateOrder"
            >
              创建订单
            </el-button>
            <el-button
              v-if="isBuyer"
              type="primary"
              :icon="Document"
              class="action-btn"
              @click="handleMyOrders"
            >
              我的订单
            </el-button>
            <el-button
              type="success"
              :icon="Search"
              class="action-btn"
              @click="handleTrack"
            >
              查询物流
            </el-button>
            <el-button :icon="Refresh" class="action-btn" @click="loadData">
              刷新数据
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
import {
  Plus,
  Search,
  Document,
  Van,
  Clock,
  CircleCheck,
  Refresh,
} from "@element-plus/icons-vue";
import {
  getOrders,
  getOrderStats,
  getBuyerOrders,
  getBuyerStats,
} from "../../../api/order";
import { useUserStore } from "../../../store/user";

const router = useRouter();
const userStore = useUserStore();
const loading = ref(false);

// 是否为买家
const isBuyer = computed(
  () => userStore.userInfo?.role === "buyer" || !!userStore.buyerPhone,
);

const stats = ref({
  total: 0,
  shipping: 0,
  pending: 0,
  completed: 0,
});

const recentOrders = ref<any[]>([]);

const statusMap: Record<string, { text: string; type: string }> = {
  pending: { text: "待发货", type: "warning" },
  shipping: { text: "运输中", type: "primary" },
  completed: { text: "已完成", type: "success" },
  cancelled: { text: "已取消", type: "info" },
};

const getStatusType = (status: string) => statusMap[status]?.type || "info";
const getStatusText = (status: string) => statusMap[status]?.text || status;

const loadData = async () => {
  loading.value = true;
  try {
    if (isBuyer.value) {
      // 买家使用专用接口
      const [statsData, ordersData] = await Promise.all([
        getBuyerStats(),
        getBuyerOrders({ page: 1, pageSize: 5 }),
      ]);
      stats.value = statsData;
      recentOrders.value = ordersData.data;
    } else {
      // 管理员/其他用户
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

const handleViewMore = () =>
  router.push(isBuyer.value ? "/buyer/orders" : "/order/list");
const handleCreateOrder = () => router.push("/order/create");
const handleMyOrders = () => router.push("/buyer/orders");
const handleTrack = () => router.push("/transport/track");

onMounted(() => {
  loadData();
});
</script>

<style scoped>
.dashboard {
  padding: 0;
}
.stats-row {
  margin-bottom: 20px;
}
.stat-card {
  cursor: pointer;
  transition: all 0.3s;
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  box-shadow: var(--card-shadow);
}
.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: var(--card-shadow-hover);
  border-color: var(--primary-light);
}
.stat-content {
  display: flex;
  align-items: center;
  gap: 20px;
}
.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}
.stat-info {
  flex: 1;
}
.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 5px;
}
.stat-label {
  font-size: 14px;
  color: var(--text-tertiary);
}
.content-row {
  margin-top: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  color: var(--text-primary);
}
.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 15px;
}
.action-btn {
  width: 100%;
  height: 50px;
  font-size: 16px;
  margin-left: 0 !important;
  transition: all 0.3s;
}
.action-btn:hover {
  transform: translateX(5px);
}
:deep(.el-card) {
  border: 1px solid var(--border-color);
  box-shadow: var(--card-shadow);
  transition: all 0.3s;
}
:deep(.el-card:hover) {
  box-shadow: var(--card-shadow-hover);
}
:deep(.el-card__header) {
  background: var(--border-color-light);
  border-bottom: 1px solid var(--border-color);
}
</style>
