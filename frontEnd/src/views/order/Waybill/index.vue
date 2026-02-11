<template>
  <page-container title="运单管理" description="管理所有订单的全流程状态">
    <data-table
      ref="tableRef"
      :search-config="searchConfig"
      :columns="columns"
      :load-data="loadData"
      :show-toolbar="true"
      :toolbar-left="renderToolbarLeft"
      :operation-width="280"
      @selection-change="handleSelectionChange"
    >
      <template #operation="{ row }">
        <el-button
          v-if="row.status === 'pending'"
          type="primary"
          link
          @click="handleShip(row)"
        >
          发货
        </el-button>
        <el-button
          v-if="row.status === 'shipping'"
          type="success"
          link
          @click="handleReceive(row)"
        >
          签收
        </el-button>
        <el-button
          v-if="row.status === 'shipping'"
          type="warning"
          link
          @click="handleStationManage(row)"
        >
          站点管理
        </el-button>
        <el-button
          v-if="row.status === 'pending' || row.status === 'shipping'"
          type="danger"
          link
          @click="handleCancel(row)"
        >
          取消
        </el-button>
        <el-button type="info" link @click="handleViewLogs(row)">
          日志
        </el-button>
      </template>
    </data-table>

    <!-- 操作日志弹窗 -->
    <el-dialog
      v-model="logDialogVisible"
      title="操作日志"
      width="700px"
      destroy-on-close
    >
      <el-table :data="operationLogs" v-loading="logLoading" stripe>
        <el-table-column prop="operateTime" label="操作时间" width="170" />
        <el-table-column prop="action" label="操作类型" width="100">
          <template #default="{ row }">
            <el-tag :type="actionTagType[row.action]" size="small">
              {{ actionTextMap[row.action] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="fromStatus" label="原状态" width="100">
          <template #default="{ row }">
            {{ statusTextMap[row.fromStatus] || row.fromStatus }}
          </template>
        </el-table-column>
        <el-table-column prop="toStatus" label="新状态" width="100">
          <template #default="{ row }">
            {{ statusTextMap[row.toStatus] || row.toStatus }}
          </template>
        </el-table-column>
        <el-table-column prop="operator" label="操作人" width="100" />
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
      </el-table>
      <template #footer>
        <el-button @click="logDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 站点管理弹窗 -->
    <el-dialog
      v-model="stationDialogVisible"
      :title="`站点管理 - ${currentOrderNo}`"
      width="800px"
      destroy-on-close
    >
      <div
        class="station-toolbar"
        style="
          margin-bottom: 16px;
          display: flex;
          gap: 12px;
          align-items: center;
        "
      >
        <el-button
          type="primary"
          :disabled="allStationsArrived"
          :loading="batchLoading"
          @click="handleMarkAllArrived"
        >
          全部到达
        </el-button>
        <el-select
          v-model="targetStationIndex"
          placeholder="到达至指定站点"
          style="width: 200px"
          :disabled="allStationsArrived"
          @change="handleMarkArrivedTo"
        >
          <el-option
            v-for="station in pendingStations"
            :key="station.index"
            :label="`${station.index + 1}. ${station.location}`"
            :value="station.index"
          />
        </el-select>
      </div>
      <el-table :data="stationList" v-loading="stationLoading" stripe>
        <el-table-column label="序号" width="70" align="center">
          <template #default="{ row }">
            {{ row.index + 1 }}
          </template>
        </el-table-column>
        <el-table-column
          prop="location"
          label="站点位置"
          min-width="200"
          show-overflow-tooltip
        />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag
              :type="row.status === 'arrived' ? 'success' : 'warning'"
              size="small"
            >
              {{ row.status === "arrived" ? "已到达" : "待到达" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="arrivalTime" label="到达时间" width="170">
          <template #default="{ row }">
            {{ row.arrivalTime || "-" }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'pending' && canMarkStation(row.index)"
              type="primary"
              size="small"
              :loading="markingIndex === row.index"
              @click="handleMarkStationArrived(row)"
            >
              标记到达
            </el-button>
            <span v-else-if="row.status === 'arrived'" style="color: #67c23a"
              >✓</span
            >
            <span v-else style="color: #909399">-</span>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="stationDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 发货抽屉 -->
    <ship-drawer
      v-model="shipDrawerVisible"
      :order="currentShipOrder"
      @success="handleShipSuccess"
    />
  </page-container>
</template>

<script setup lang="ts">
import { h, ref, computed, onMounted } from "vue";
import { ElButton, ElTag, ElMessage, ElMessageBox } from "element-plus";
import { Van, Check, DocumentCopy } from "@element-plus/icons-vue";
import PageContainer from "../../../components/layout/PageContainer/index.vue";
import DataTable from "../../../components/business/DataTable/index.vue";
import ShipDrawer from "../../../components/business/ShipDrawer/index.vue";
import {
  getOrders,
  receiveOrder,
  cancelOrder,
  batchShip,
  batchReceive,
  getOrderLogs,
  getStationStatus,
  markStationArrived,
  markAllStationsArrived,
  markStationsArrivedTo,
} from "../../../api/order";
import type {
  OperationLog,
  StationInfo,
  Order,
} from "../../../api/order/types";

const tableRef = ref<InstanceType<typeof DataTable> | null>(null);
const selectedOrders = ref<any[]>([]);
const logDialogVisible = ref(false);
const logLoading = ref(false);
const operationLogs = ref<OperationLog[]>([]);

// 站点管理状态
const stationDialogVisible = ref(false);
const stationLoading = ref(false);
const stationList = ref<StationInfo[]>([]);
const currentOrderNo = ref("");
const markingIndex = ref<number | null>(null);
const batchLoading = ref(false);
const targetStationIndex = ref<number | null>(null);

// 发货抽屉状态
const shipDrawerVisible = ref(false);
const currentShipOrder = ref<Order | null>(null);

// 状态选项
const statusOptions = [
  { label: "全部", value: "" },
  { label: "待发货", value: "pending" },
  { label: "运输中", value: "shipping" },
  { label: "已完成", value: "completed" },
  { label: "已取消", value: "cancelled" },
];

// 搜索配置
const searchConfig = [
  {
    prop: "orderNo",
    label: "订单号",
    type: "input" as const,
    placeholder: "请输入订单号",
  },
  {
    prop: "status",
    label: "订单状态",
    type: "select" as const,
    placeholder: "请选择状态",
    options: statusOptions,
  },
  {
    prop: "cargoName",
    label: "货物名称",
    type: "input" as const,
    placeholder: "请输入货物名称",
  },
  {
    prop: "senderName",
    label: "发货人",
    type: "input" as const,
    placeholder: "请输入发货人",
  },
  {
    prop: "receiverName",
    label: "收货人",
    type: "input" as const,
    placeholder: "请输入收货人",
  },
];

// 状态标签类型映射
const statusTagType: Record<
  string,
  "warning" | "primary" | "success" | "info"
> = {
  pending: "warning",
  shipping: "primary",
  completed: "success",
  cancelled: "info",
};

// 状态文本映射
const statusTextMap: Record<string, string> = {
  pending: "待发货",
  shipping: "运输中",
  completed: "已完成",
  cancelled: "已取消",
  null: "新建",
  "": "新建",
};

// 操作类型标签映射
const actionTagType: Record<
  string,
  "primary" | "success" | "danger" | "warning" | "info"
> = {
  ship: "primary",
  receive: "success",
  cancel: "danger",
  station_arrive: "warning",
  create: "info",
};

// 操作类型文本映射
const actionTextMap: Record<string, string> = {
  ship: "发货",
  receive: "签收",
  cancel: "取消",
  station_arrive: "站点到达",
  create: "新建",
};

// 复制订单号
const copyOrderNo = async (orderNo: string) => {
  try {
    await navigator.clipboard.writeText(orderNo);
    ElMessage.success("已复制");
  } catch {
    ElMessage.warning("复制失败");
  }
};

// 表格列配置
const columns = [
  {
    prop: "orderNo",
    label: "订单号",
    width: 200,
    render: (row: any) =>
      h("div", { style: "display: flex; align-items: center; gap: 4px;" }, [
        h(
          "span",
          { style: "overflow: hidden; text-overflow: ellipsis;" },
          row.orderNo,
        ),
        h(ElButton, {
          size: "small",
          icon: DocumentCopy,
          link: true,
          onClick: (e: Event) => {
            e.stopPropagation();
            copyOrderNo(row.orderNo);
          },
        }),
      ]),
  },
  { prop: "cargoName", label: "货物名称", minWidth: 120 },
  {
    prop: "expressCompany",
    label: "快递公司",
    width: 100,
    formatter: (row: any) =>
      row.expressCompanyName || row.expressCompany || "-",
  },
  { prop: "senderName", label: "发货人", width: 80 },
  { prop: "senderPhone", label: "发货人电话", width: 120 },
  { prop: "receiverName", label: "收货人", width: 80 },
  { prop: "receiverPhone", label: "收货人电话", width: 120 },
  {
    prop: "origin",
    label: "发货地址",
    minWidth: 150,
    showOverflowTooltip: true,
  },
  {
    prop: "destination",
    label: "收货地址",
    minWidth: 150,
    showOverflowTooltip: true,
  },
  {
    prop: "status",
    label: "状态",
    width: 90,
    align: "center" as const,
    render: (row: any) =>
      h(
        ElTag,
        { type: statusTagType[row.status] || "info", size: "small" },
        () => statusTextMap[row.status] || row.status,
      ),
  },
  { prop: "createTime", label: "创建时间", width: 160 },
];

// 单个发货
const handleShip = (row: any) => {
  currentShipOrder.value = row as Order;
  shipDrawerVisible.value = true;
};

// 发货成功回调
const handleShipSuccess = () => {
  tableRef.value?.refresh();
};

// 单个签收
const handleReceive = async (row: any) => {
  try {
    // 检查最后一个站点是否已到达
    const stations = await getStationStatus(row.orderNo);
    if (stations.length === 0) {
      ElMessage.warning("无法获取站点信息");
      return;
    }

    const lastStation = stations[stations.length - 1];
    if (lastStation?.status !== "arrived") {
      ElMessage.warning("货物尚未到达目的地，无法签收");
      return;
    }

    await ElMessageBox.confirm(
      `确定要签收订单 ${row.orderNo} 吗？`,
      "确认签收",
      { type: "warning" },
    );
    await receiveOrder(row.orderNo);
    ElMessage.success("签收成功");
    tableRef.value?.refresh();
  } catch (error: any) {
    if (error !== "cancel") {
      ElMessage.error(error?.message || "签收失败");
    }
  }
};

// 单个取消
const handleCancel = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要取消订单 ${row.orderNo} 吗？此操作不可恢复！`,
      "确认取消",
      { type: "warning" },
    );
    await cancelOrder(row.orderNo);
    ElMessage.success("取消成功");
    tableRef.value?.refresh();
  } catch (error: any) {
    if (error !== "cancel") {
      ElMessage.error(error?.message || "取消失败");
    }
  }
};

// 查看操作日志
const handleViewLogs = async (row: any) => {
  logDialogVisible.value = true;
  logLoading.value = true;
  try {
    operationLogs.value = await getOrderLogs(row.orderNo);
  } catch (error: any) {
    ElMessage.error(error?.message || "获取操作日志失败");
    operationLogs.value = [];
  } finally {
    logLoading.value = false;
  }
};

// ==================== 站点管理功能 ====================

// 待到达站点列表
const pendingStations = computed(() =>
  stationList.value.filter((s) => s.status === "pending"),
);

// 是否所有站点都已到达
const allStationsArrived = computed(
  () =>
    stationList.value.length > 0 &&
    stationList.value.every((s) => s.status === "arrived"),
);

// 判断是否可以标记该站点（前一个站点必须已到达）
const canMarkStation = (index: number): boolean => {
  if (index === 0) return true;
  const prevStation = stationList.value.find((s) => s.index === index - 1);
  return prevStation?.status === "arrived";
};

// 打开站点管理弹窗
const handleStationManage = async (row: any) => {
  currentOrderNo.value = row.orderNo;
  stationDialogVisible.value = true;
  targetStationIndex.value = null;
  await loadStationStatus();
};

// 加载站点状态
const loadStationStatus = async () => {
  stationLoading.value = true;
  try {
    stationList.value = await getStationStatus(currentOrderNo.value);
  } catch (error: any) {
    ElMessage.error(error?.message || "获取站点状态失败");
    stationList.value = [];
  } finally {
    stationLoading.value = false;
  }
};

// 标记单个站点到达
const handleMarkStationArrived = async (station: StationInfo) => {
  markingIndex.value = station.index;
  try {
    const result = await markStationArrived(
      currentOrderNo.value,
      station.index,
    );
    if (result.success) {
      ElMessage.success("标记到达成功");
      await loadStationStatus();
    } else {
      ElMessage.error(result.message || "标记到达失败");
    }
  } catch (error: any) {
    ElMessage.error(error?.message || "标记到达失败");
  } finally {
    markingIndex.value = null;
  }
};

// 标记全部站点到达
const handleMarkAllArrived = async () => {
  try {
    await ElMessageBox.confirm("确定要将所有站点标记为已到达吗？", "全部到达", {
      type: "warning",
    });
    batchLoading.value = true;
    const result = await markAllStationsArrived(currentOrderNo.value);
    if (result.success) {
      ElMessage.success(`成功标记 ${result.arrivedCount} 个站点到达`);
      await loadStationStatus();
    } else {
      ElMessage.error(result.message || "批量标记失败");
    }
  } catch (error: any) {
    if (error !== "cancel") {
      ElMessage.error(error?.message || "批量标记失败");
    }
  } finally {
    batchLoading.value = false;
  }
};

// 标记到达至指定站点
const handleMarkArrivedTo = async (targetIndex: number) => {
  if (targetIndex === null) return;

  const targetStation = stationList.value.find((s) => s.index === targetIndex);
  try {
    await ElMessageBox.confirm(
      `确定要将从起点到"${targetStation?.location}"的所有站点标记为已到达吗？`,
      "到达至指定站点",
      { type: "warning" },
    );
    batchLoading.value = true;
    const result = await markStationsArrivedTo(
      currentOrderNo.value,
      targetIndex,
    );
    if (result.success) {
      ElMessage.success(`成功标记 ${result.arrivedCount} 个站点到达`);
      await loadStationStatus();
    } else {
      ElMessage.error(result.message || "批量标记失败");
    }
  } catch (error: any) {
    if (error !== "cancel") {
      ElMessage.error(error?.message || "批量标记失败");
    }
  } finally {
    batchLoading.value = false;
    targetStationIndex.value = null;
  }
};

// 选择变化
const handleSelectionChange = (selection: any[]) => {
  selectedOrders.value = selection;
};

// 批量发货
const handleBatchShip = async () => {
  const pendingOrders = selectedOrders.value.filter(
    (o) => o.status === "pending",
  );
  if (pendingOrders.length === 0) {
    ElMessage.warning("请选择待发货的订单");
    return;
  }

  try {
    await ElMessageBox.confirm(
      `确定要批量发货选中的 ${pendingOrders.length} 个订单吗？`,
      "批量发货",
      { type: "warning" },
    );

    const orderNos = pendingOrders.map((o) => o.orderNo);
    const result = await batchShip(orderNos);

    if (result.successCount === orderNos.length) {
      ElMessage.success(`成功发货 ${result.successCount} 个订单`);
    } else if (result.successCount > 0) {
      ElMessage.warning(
        `成功发货 ${result.successCount} 个订单，${result.failedOrders?.length || 0} 个订单发货失败`,
      );
    } else {
      ElMessage.error("批量发货失败");
    }

    tableRef.value?.refresh();
  } catch (error: any) {
    if (error !== "cancel") {
      ElMessage.error(error?.message || "批量发货失败");
    }
  }
};

// 批量签收
const handleBatchReceive = async () => {
  const shippingOrders = selectedOrders.value.filter(
    (o) => o.status === "shipping",
  );
  if (shippingOrders.length === 0) {
    ElMessage.warning("请选择运输中的订单");
    return;
  }

  try {
    await ElMessageBox.confirm(
      `确定要批量签收选中的 ${shippingOrders.length} 个订单吗？`,
      "批量签收",
      { type: "warning" },
    );

    const orderNos = shippingOrders.map((o) => o.orderNo);
    const result = await batchReceive(orderNos);

    if (result.successCount === orderNos.length) {
      ElMessage.success(`成功签收 ${result.successCount} 个订单`);
    } else if (result.successCount > 0) {
      ElMessage.warning(
        `成功签收 ${result.successCount} 个订单，${result.failedOrders?.length || 0} 个订单签收失败`,
      );
    } else {
      ElMessage.error("批量签收失败");
    }

    tableRef.value?.refresh();
  } catch (error: any) {
    if (error !== "cancel") {
      ElMessage.error(error?.message || "批量签收失败");
    }
  }
};

// 待发货订单数量
const pendingCount = computed(
  () => selectedOrders.value.filter((o) => o.status === "pending").length,
);

// 运输中订单数量
const shippingCount = computed(
  () => selectedOrders.value.filter((o) => o.status === "shipping").length,
);

// 工具栏左侧
const renderToolbarLeft = () => [
  h(
    ElButton,
    {
      type: "primary",
      icon: Van,
      disabled: pendingCount.value === 0,
      onClick: handleBatchShip,
    },
    () => `批量发货${pendingCount.value > 0 ? ` (${pendingCount.value})` : ""}`,
  ),
  h(
    ElButton,
    {
      type: "success",
      icon: Check,
      disabled: shippingCount.value === 0,
      onClick: handleBatchReceive,
    },
    () =>
      `批量签收${shippingCount.value > 0 ? ` (${shippingCount.value})` : ""}`,
  ),
];

// 从后端API加载数据
const loadData = async (params: any) => {
  try {
    const result = await getOrders({
      page: params.page,
      pageSize: params.pageSize,
      orderNo: params.orderNo,
      status: params.status || undefined,
      cargoName: params.cargoName,
      senderName: params.senderName,
      receiverName: params.receiverName,
    });

    return { data: result.data, total: result.total };
  } catch (error) {
    ElMessage.error("获取订单列表失败");
    return { data: [], total: 0 };
  }
};

// 初始化快递公司数据
onMounted(() => {
  // 数据由 ExpressCompanySelect 组件自动加载
});
</script>
