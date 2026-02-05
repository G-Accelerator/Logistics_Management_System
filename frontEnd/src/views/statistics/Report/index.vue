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
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon total">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overview.totalOrders }}</div>
              <div class="stat-label">总订单数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon month">
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
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon pending">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overview.pendingOrders }}</div>
              <div class="stat-label">待处理订单</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon rate">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">
                {{ overview.completionRate.toFixed(1) }}%
              </div>
              <div class="stat-label">完成率</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 - 第一行 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :xs="24" :lg="14">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <span>订单趋势</span>
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
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <span>订单状态分布</span>
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
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="chart-header">
              <span>热门城市TOP10</span>
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
            bar-color="#67C23A"
            :loading="loading.cities"
          />
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <span>快递公司分布</span>
          </template>
          <BarChart
            :x-data="expressCompaniesData.labels"
            :y-data="expressCompaniesData.values"
            series-name="订单数"
            height="320px"
            bar-color="#E6A23C"
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

// 时间范围
const timeRange = ref(7);

// 城市类型
const cityType = ref<"origin" | "destination">("origin");

// 加载状态
const loading = reactive({
  overview: false,
  trend: false,
  status: false,
  cities: false,
  express: false,
});

// 概览数据
const overview = ref<StatisticsOverview>({
  totalOrders: 0,
  monthOrders: 0,
  pendingOrders: 0,
  completionRate: 0,
  monthGrowth: 0,
});

// 趋势数据
const trendData = ref<TrendData>({
  dates: [],
  counts: [],
});

// 状态分布数据（转换为饼图格式）
const statusDistributionData = ref<{ name: string; value: number }[]>([]);

// 热门城市数据
const topCitiesData = ref<DistributionData>({
  labels: [],
  values: [],
});

// 快递公司数据
const expressCompaniesData = ref<DistributionData>({
  labels: [],
  values: [],
});

// 状态名称映射
const statusNameMap: Record<string, string> = {
  pending: "待发货",
  shipping: "运输中",
  completed: "已完成",
  cancelled: "已取消",
};

// 获取概览数据
async function fetchOverview() {
  loading.overview = true;
  try {
    const data = await getStatisticsOverview();
    overview.value = data;
  } catch (error) {
    ElMessage.error("获取概览数据失败");
  } finally {
    loading.overview = false;
  }
}

// 获取趋势数据
async function fetchTrend() {
  loading.trend = true;
  try {
    const data = await getOrderTrend({ days: timeRange.value });
    trendData.value = data;
  } catch (error) {
    ElMessage.error("获取趋势数据失败");
  } finally {
    loading.trend = false;
  }
}

// 获取状态分布
async function fetchStatusDistribution() {
  loading.status = true;
  try {
    const data: StatusDistribution = await getStatusDistribution();
    statusDistributionData.value = Object.entries(data).map(([key, value]) => ({
      name: statusNameMap[key] || key,
      value: value as number,
    }));
  } catch (error) {
    ElMessage.error("获取状态分布失败");
  } finally {
    loading.status = false;
  }
}

// 获取热门城市
async function fetchTopCities() {
  loading.cities = true;
  try {
    const data = await getTopCities({ type: cityType.value, limit: 10 });
    topCitiesData.value = data;
  } catch (error) {
    ElMessage.error("获取热门城市失败");
  } finally {
    loading.cities = false;
  }
}

// 获取快递公司统计
async function fetchExpressCompanies() {
  loading.express = true;
  try {
    const data = await getExpressCompanies();
    expressCompaniesData.value = data;
  } catch (error) {
    ElMessage.error("获取快递公司统计失败");
  } finally {
    loading.express = false;
  }
}

// 时间范围变化
function handleTimeRangeChange() {
  fetchTrend();
}

// 状态饼图点击
function handleStatusClick(data: { name: string; value: number }) {
  ElMessage.info(`${data.name}: ${data.value} 单`);
}

// 初始化加载所有数据
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
  padding: 20px;
}

.filter-bar {
  margin-bottom: 20px;
}

.overview-cards {
  margin-bottom: 20px;
}

.stat-card {
  margin-bottom: 20px;
}

.stat-card :deep(.el-card__body) {
  padding: 20px;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #fff;
}

.stat-icon.total {
  background: linear-gradient(135deg, #409eff, #66b1ff);
}

.stat-icon.month {
  background: linear-gradient(135deg, #67c23a, #85ce61);
}

.stat-icon.pending {
  background: linear-gradient(135deg, #e6a23c, #ebb563);
}

.stat-icon.rate {
  background: linear-gradient(135deg, #909399, #a6a9ad);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.growth {
  font-size: 14px;
  font-weight: normal;
  margin-left: 8px;
  display: inline-flex;
  align-items: center;
  gap: 2px;
}

.growth.up {
  color: #67c23a;
}

.growth.down {
  color: #f56c6c;
}

.chart-row {
  margin-bottom: 20px;
}

.chart-card {
  margin-bottom: 20px;
}

.chart-card :deep(.el-card__header) {
  padding: 16px 20px;
  font-weight: 500;
  color: #303133;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

@media (max-width: 768px) {
  .stat-value {
    font-size: 22px;
  }

  .stat-icon {
    width: 48px;
    height: 48px;
    font-size: 20px;
  }

  .chart-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
}
</style>
