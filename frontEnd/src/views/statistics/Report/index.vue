<template>
  <div class="statistics-report">
    <!-- 时间范围选择 -->
    <div class="filter-bar">
      <el-radio-group v-model="timeRange" @change="handleTimeRangeChange">
        <el-radio-button :value="7">近7天</el-radio-button>
        <el-radio-button :value="30">近30天</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 概览卡片区域 -->
    <el-row :gutter="20" class="overview-cards">
      <el-col :xs="12" :sm="6">
        <div class="stat-card">
          <div class="stat-icon primary">
            <el-icon><Document /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ overview.totalOrders }}</div>
            <div class="stat-label">总订单数</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card">
          <div class="stat-icon success">
            <el-icon><Calendar /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">
              {{ overview.monthOrders }}
              <span
                class="growth"
                :class="overview.monthGrowth >= 0 ? 'up' : 'down'"
              >
                <el-icon v-if="overview.monthGrowth >= 0"><Top /></el-icon>
                <el-icon v-else><Bottom /></el-icon>
                {{ Math.abs(overview.monthGrowth).toFixed(1) }}%
              </span>
            </div>
            <div class="stat-label">本月订单</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card">
          <div class="stat-icon warning">
            <el-icon><Clock /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ overview.pendingOrders }}</div>
            <div class="stat-label">待处理订单</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card">
          <div class="stat-icon info">
            <el-icon><CircleCheck /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">
              {{ overview.completionRate.toFixed(1) }}%
            </div>
            <div class="stat-label">完成率</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区域 - 第一行 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :xs="24" :lg="14">
        <el-card class="chart-card">
          <template #header>
            <div class="chart-header">
              <div class="header-title">
                <el-icon class="header-icon"><TrendCharts /></el-icon>
                <span>订单趋势</span>
              </div>
            </div>
          </template>
          <LineChart
            :x-data="trendData.dates"
            :y-data="trendData.counts"
            series-name="订单数"
            height="320px"
            :loading="loading.trend"
          />
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="10">
        <el-card class="chart-card">
          <template #header>
            <div class="chart-header">
              <div class="header-title">
                <el-icon class="header-icon"><PieChart as any /></el-icon>
                <span>订单状态分布</span>
              </div>
            </div>
          </template>
          <PieChart
            :data="statusDistributionData"
            height="320px"
            ring
            :loading="loading.status"
            @click="handleStatusClick"
          />
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 - 第二行 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :xs="24" :lg="12">
        <el-card class="chart-card">
          <template #header>
            <div class="chart-header">
              <div class="header-title">
                <el-icon class="header-icon"><Location /></el-icon>
                <span>热门城市TOP10</span>
              </div>
              <el-radio-group
                v-model="cityType"
                size="small"
                @change="fetchTopCities"
              >
                <el-radio-button value="origin">发货城市</el-radio-button>
                <el-radio-button value="destination">收货城市</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <BarChart
            :x-data="topCitiesData.labels"
            :y-data="topCitiesData.values"
            series-name="订单数"
            height="320px"
            bar-color="#10b981"
            :loading="loading.cities"
          />
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card class="chart-card">
          <template #header>
            <div class="chart-header">
              <div class="header-title">
                <el-icon class="header-icon"><Van /></el-icon>
                <span>快递公司分布</span>
              </div>
            </div>
          </template>
          <BarChart
            :x-data="expressCompaniesData.labels"
            :y-data="expressCompaniesData.values"
            series-name="订单数"
            height="320px"
            bar-color="#0ea5e9"
            :loading="loading.express"
          />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";
import { ElMessage } from "element-plus";
import {
  Document,
  Calendar,
  Clock,
  CircleCheck,
  Top,
  Bottom,
  TrendCharts,
  Location,
  Van,
  PieChart as PieChartIcon,
} from "@element-plus/icons-vue";
import LineChart from "../../../components/charts/LineChart.vue";
import PieChart from "../../../components/charts/PieChart.vue";
import BarChart from "../../../components/charts/BarChart.vue";
import {
  getStatisticsOverview,
  getOrderTrend,
  getStatusDistribution,
  getTopCities,
  getExpressCompanies,
  type StatisticsOverview,
  type TrendData,
  type DistributionData,
  type StatusDistribution,
} from "../../../api/statistics";

const timeRange = ref(7);
const cityType = ref<"origin" | "destination">("origin");

const loading = reactive({
  overview: false,
  trend: false,
  status: false,
  cities: false,
  express: false,
});

const overview = ref<StatisticsOverview>({
  totalOrders: 0,
  monthOrders: 0,
  pendingOrders: 0,
  completionRate: 0,
  monthGrowth: 0,
});

const trendData = ref<TrendData>({ dates: [], counts: [] });
const statusDistributionData = ref<{ name: string; value: number }[]>([]);
const topCitiesData = ref<DistributionData>({ labels: [], values: [] });
const expressCompaniesData = ref<DistributionData>({ labels: [], values: [] });

const statusNameMap: Record<string, string> = {
  pending: "待发货",
  shipping: "运输中",
  completed: "已完成",
  cancelled: "已取消",
};

async function fetchOverview() {
  loading.overview = true;
  try {
    overview.value = await getStatisticsOverview();
  } catch {
    ElMessage.error("获取概览数据失败");
  } finally {
    loading.overview = false;
  }
}

async function fetchTrend() {
  loading.trend = true;
  try {
    trendData.value = await getOrderTrend({ days: timeRange.value });
  } catch {
    ElMessage.error("获取趋势数据失败");
  } finally {
    loading.trend = false;
  }
}

async function fetchStatusDistribution() {
  loading.status = true;
  try {
    const data: StatusDistribution = await getStatusDistribution();
    statusDistributionData.value = Object.entries(data).map(([key, value]) => ({
      name: statusNameMap[key] || key,
      value: value as number,
    }));
  } catch {
    ElMessage.error("获取状态分布失败");
  } finally {
    loading.status = false;
  }
}

async function fetchTopCities() {
  loading.cities = true;
  try {
    topCitiesData.value = await getTopCities({
      type: cityType.value,
      limit: 10,
    });
  } catch {
    ElMessage.error("获取热门城市失败");
  } finally {
    loading.cities = false;
  }
}

async function fetchExpressCompanies() {
  loading.express = true;
  try {
    expressCompaniesData.value = await getExpressCompanies();
  } catch {
    ElMessage.error("获取快递公司统计失败");
  } finally {
    loading.express = false;
  }
}

function handleTimeRangeChange() {
  fetchTrend();
}
function handleStatusClick(data: { name: string; value: number }) {
  ElMessage.info(`${data.name}: ${data.value} 单`);
}

onMounted(() => {
  fetchOverview();
  fetchTrend();
  fetchStatusDistribution();
  fetchTopCities();
  fetchExpressCompanies();
});
</script>

<style scoped>
.statistics-report {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.filter-bar {
  display: flex;
  justify-content: flex-end;
}

.overview-cards {
  margin-bottom: 0;
}

.stat-card {
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  border: 1px solid var(--border-color);
  transition: all var(--transition-normal);
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
  border-color: var(--primary-light);
}

.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #fff;
}

.stat-icon.primary {
  background: var(--gradient-primary);
}
.stat-icon.success {
  background: linear-gradient(
    135deg,
    var(--success-color) 0%,
    var(--success-light) 100%
  );
}
.stat-icon.warning {
  background: linear-gradient(
    135deg,
    var(--warning-color) 0%,
    var(--warning-light) 100%
  );
}
.stat-icon.info {
  background: linear-gradient(
    135deg,
    var(--info-color) 0%,
    var(--info-light) 100%
  );
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 26px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: var(--text-tertiary);
  margin-top: 4px;
}

.growth {
  font-size: 13px;
  font-weight: 500;
  margin-left: 8px;
  display: inline-flex;
  align-items: center;
  gap: 2px;
}

.growth.up {
  color: var(--success-color);
}
.growth.down {
  color: var(--danger-color);
}

.chart-row {
  margin-bottom: 0;
}

.chart-card {
  border-radius: var(--radius-lg);
  margin-bottom: 20px;
}

.chart-header {
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

@media (max-width: 768px) {
  .stat-card {
    padding: 16px;
  }
  .stat-value {
    font-size: 22px;
  }
  .stat-icon {
    width: 44px;
    height: 44px;
    font-size: 20px;
  }
  .chart-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
}
</style>
