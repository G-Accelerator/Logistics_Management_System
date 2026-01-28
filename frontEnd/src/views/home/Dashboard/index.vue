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
              <div class="stat-value">1,234</div>
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
              <div class="stat-value">89</div>
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
              <el-icon :size="30"><Box /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">456</div>
              <div class="stat-label">库存总量</div>
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
                background: linear-gradient(135deg, #ef4444 0%, #f87171 100%);
              "
            >
              <el-icon :size="30"><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">12</div>
              <div class="stat-label">待处理</div>
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
          <el-table :data="recentOrders" style="width: 100%">
            <el-table-column prop="orderNo" label="订单号" width="150" />
            <el-table-column prop="customer" label="客户" width="120" />
            <el-table-column prop="destination" label="目的地" width="150" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">{{
                  row.status
                }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header
            ><div class="card-header"><span>快捷操作</span></div></template
          >
          <div class="quick-actions">
            <el-button
              type="primary"
              :icon="Plus"
              class="action-btn"
              @click="handleCreateOrder"
              >创建订单</el-button
            >
            <el-button
              type="success"
              :icon="Search"
              class="action-btn"
              @click="handleTrack"
              >查询物流</el-button
            >
            <el-button
              type="warning"
              :icon="Box"
              class="action-btn"
              @click="handleInventory"
              >库存盘点</el-button
            >
            <el-button
              type="info"
              :icon="Document"
              class="action-btn"
              @click="handleReport"
              >生成报表</el-button
            >
          </div>
        </el-card>
        <el-card style="margin-top: 20px">
          <template #header
            ><div class="card-header"><span>系统通知</span></div></template
          >
          <el-timeline>
            <el-timeline-item
              v-for="(item, index) in notifications"
              :key="index"
              :timestamp="item.time"
              placement="top"
            >
              {{ item.content }}
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import {
  Plus,
  Search,
  Box,
  Document,
  Warning,
  Van,
} from "@element-plus/icons-vue";

const router = useRouter();

const recentOrders = ref([
  {
    orderNo: "ORD20240119001",
    customer: "张三",
    destination: "北京市朝阳区",
    status: "运输中",
    createTime: "2024-01-19 10:30",
  },
  {
    orderNo: "ORD20240119002",
    customer: "李四",
    destination: "上海市浦东新区",
    status: "已完成",
    createTime: "2024-01-19 09:15",
  },
  {
    orderNo: "ORD20240119003",
    customer: "王五",
    destination: "广州市天河区",
    status: "待发货",
    createTime: "2024-01-19 08:45",
  },
  {
    orderNo: "ORD20240118001",
    customer: "赵六",
    destination: "深圳市南山区",
    status: "运输中",
    createTime: "2024-01-18 16:20",
  },
]);

const notifications = ref([
  { time: "2024-01-19 11:30", content: "订单 ORD20240119001 已发货" },
  { time: "2024-01-19 10:15", content: "库存预警：A区库存不足" },
  { time: "2024-01-19 09:00", content: "系统维护通知：今晚22:00-23:00" },
]);

const getStatusType = (status: string) => {
  const statusMap: Record<string, any> = {
    运输中: "primary",
    已完成: "success",
    待发货: "warning",
    已取消: "info",
  };
  return statusMap[status] || "info";
};

const handleViewMore = () => router.push("/order/list");
const handleCreateOrder = () => router.push("/order/create");
const handleTrack = () => router.push("/transport/track");
const handleInventory = () => router.push("/warehouse/inventory");
const handleReport = () => ElMessage.info("报表功能开发中");
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
