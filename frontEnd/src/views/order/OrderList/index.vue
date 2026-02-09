<template>
  <page-container title="订单列表" description="管理所有物流订单">
    <data-table
      ref="tableRef"
      :search-config="searchConfig"
      :columns="columns"
      :load-data="loadData"
      :show-toolbar="true"
      :toolbar-left="renderToolbarLeft"
      :operations="operations"
      :operation-width="220"
      :show-selection="true"
      @selection-change="handleSelectionChange"
    />

    <!-- 导入对话框 -->
    <import-dialog
      v-model="importDialogVisible"
      @success="handleImportSuccess"
    />

    <!-- 发货抽屉 -->
    <ship-drawer
      v-model="shipDialogVisible"
      :order="shipOrder"
      @success="handleShipSuccess"
    />
  </page-container>
</template>

<script setup lang="tsx">
import { h, ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElButton, ElTag, ElMessage, ElMessageBox } from "element-plus";
import {
  Plus,
  DocumentCopy,
  Upload,
  Download,
  Delete,
} from "@element-plus/icons-vue";
import PageContainer from "../../../components/layout/PageContainer/index.vue";
import DataTable from "../../../components/business/DataTable/index.vue";
import ImportDialog from "../../../components/business/ImportDialog/index.vue";
import ShipDrawer from "../../../components/business/ShipDrawer/index.vue";
import {
  getOrders,
  deleteOrder,
  batchDeleteOrders,
  exportOrders,
} from "../../../api/order";
import type {
  Order,
  ImportResult,
  ExportRequest,
} from "../../../api/order/types";
import { getEnabledExpressCompanies } from "../../../api/system/expressCompany";
import { downloadOrderExport } from "../../../utils/file";

const router = useRouter();
const tableRef = ref<InstanceType<typeof DataTable> | null>(null);

// 导入对话框状态
const importDialogVisible = ref(false);

// 发货对话框状态
const shipDialogVisible = ref(false);
const shipOrder = ref<Order | null>(null);

// 选中的订单
const selectedOrders = ref<Order[]>([]);

// 当前筛选条件
const currentFilters = ref<Record<string, any>>({});

// 导出中状态
const exporting = ref(false);

// 快递公司选项
const expressCompanyOptions = ref<{ label: string; value: string }[]>([]);

// 加载快递公司列表
const loadExpressCompanies = async () => {
  try {
    const companies = await getEnabledExpressCompanies();
    expressCompanyOptions.value = companies.map((c) => ({
      label: c.name,
      value: c.code,
    }));
  } catch (error) {
    console.error("获取快递公司列表失败", error);
  }
};

// 打开发货弹窗
const openShipDialog = (order: Order) => {
  shipOrder.value = order;
  shipDialogVisible.value = true;
};

// 发货成功回调
const handleShipSuccess = () => {
  tableRef.value?.refresh();
};

// 复制
const copy = async (orderNo: string) => {
  try {
    await navigator.clipboard.writeText(orderNo);
    ElMessage.success("已复制");
  } catch {
    ElMessage.warning("复制失败");
  }
};

// 搜索配置（使用计算属性以支持动态快递公司选项）
const searchConfig = computed(() => [
  {
    prop: "orderNo",
    label: "订单号",
    type: "input" as const,
    placeholder: "请输入订单号",
  },
  {
    prop: "trackingNo",
    label: "运单号",
    type: "input" as const,
    placeholder: "请输入运单号",
  },
  {
    prop: "cargoName",
    label: "货物名称",
    type: "input" as const,
    placeholder: "请输入货物名称",
  },
  {
    prop: "status",
    label: "订单状态",
    type: "select" as const,
    options: [
      { label: "待发货", value: "pending" },
      { label: "运输中", value: "shipping" },
      { label: "已完成", value: "completed" },
      { label: "已取消", value: "cancelled" },
    ],
  },
  {
    prop: "cargoType",
    label: "货物类型",
    type: "select" as const,
    options: [
      { label: "普通货物", value: "normal" },
      { label: "易碎品", value: "fragile" },
      { label: "冷链货物", value: "cold" },
      { label: "危险品", value: "dangerous" },
    ],
  },
  {
    prop: "expressCompany",
    label: "快递公司",
    type: "select" as const,
    options: expressCompanyOptions.value,
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
]);

// 状态映射
const statusMap: Record<string, { label: string; type: string }> = {
  pending: { label: "待发货", type: "warning" },
  shipping: { label: "运输中", type: "primary" },
  completed: { label: "已完成", type: "success" },
  cancelled: { label: "已取消", type: "info" },
};

// 货物类型映射
const cargoTypeMap: Record<string, string> = {
  normal: "普通货物",
  fragile: "易碎品",
  cold: "冷链货物",
  dangerous: "危险品",
};

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
            copy(row.orderNo);
          },
        }),
      ]),
  },
  {
    prop: "expressCompany",
    label: "快递公司",
    width: 90,
    formatter: (row: any) =>
      expressCompanyMap[row.expressCompany] || row.expressCompany || "-",
  },
  {
    prop: "trackingNo",
    label: "运单号",
    width: 180,
    showOverflowTooltip: true,
    render: (row: any) => {
      if (!row.trackingNo) {
        return <span style="color: #999;">-</span>;
      }
      return (
        <div style="display: flex; align-items: center; gap: 4px;">
          <span style="overflow: hidden; text-overflow: ellipsis;">
            {row.trackingNo}
          </span>
          <ElButton
            size="small"
            icon={DocumentCopy}
            link
            onClick={(e: Event) => {
              e.stopPropagation();
              copy(row.trackingNo);
            }}
          />
        </div>
      );
    },
  },
  { prop: "cargoName", label: "货物名称", minWidth: 100 },
  {
    prop: "cargoType",
    label: "货物类型",
    width: 90,
    formatter: (row: any) => cargoTypeMap[row.cargoType] || row.cargoType,
  },

  {
    prop: "cargoWeight",
    label: "重量(kg)",
    width: 90,
    align: "right" as const,
    formatter: (row: any) => row.cargoWeight ?? "-",
  },
  {
    prop: "cargoVolume",
    label: "体积(m³)",
    width: 90,
    align: "right" as const,
    formatter: (row: any) => row.cargoVolume ?? "-",
  },
  {
    prop: "cargoQuantity",
    label: "数量(件)",
    width: 80,
    align: "right" as const,
    formatter: (row: any) => row.cargoQuantity ?? "-",
  },
  { prop: "origin", label: "起始地", minWidth: 200, showOverflowTooltip: true },
  {
    prop: "destination",
    label: "目的地",
    minWidth: 200,
    showOverflowTooltip: true,
  },
  { prop: "senderName", label: "发货人", width: 80 },
  { prop: "senderPhone", label: "发货人电话", width: 120 },
  { prop: "receiverName", label: "收货人", width: 80 },
  { prop: "receiverPhone", label: "收货人电话", width: 120 },
  {
    prop: "status",
    label: "状态",
    width: 90,
    align: "center" as const,
    render: (row: any) => {
      const status = statusMap[row.status] || {
        label: row.status,
        type: "info",
      };
      return h(
        ElTag,
        { type: status.type as any, size: "small" },
        () => status.label,
      );
    },
  },
  { prop: "remark", label: "备注", minWidth: 100, showOverflowTooltip: true },
  { prop: "createTime", label: "创建时间", width: 160 },
];

// 查看物流
const viewTrack = (row: any) => {
  router.push({ path: "/transport/track", query: { orderNo: row.orderNo } });
};

// 操作按钮
const operations = [
  {
    label: "发货",
    type: "success" as const,
    show: (row: any) => row.status === "pending",
    handler: (row: any) => openShipDialog(row),
  },
  { label: "查看物流", type: "primary" as const, handler: viewTrack },
  {
    label: "删除",
    type: "danger" as const,
    handler: async (row: any) => {
      try {
        await ElMessageBox.confirm(
          `确定要删除订单 ${row.orderNo} 吗？`,
          "提示",
          { type: "warning" },
        );
        await deleteOrder(row.orderNo);
        ElMessage.success("删除成功");
        tableRef.value?.refresh();
      } catch {}
    },
  },
];

// 工具栏左侧
const renderToolbarLeft = () =>
  h("div", { style: "display: flex; gap: 12px;" }, [
    h(
      ElButton,
      {
        type: "primary",
        icon: Plus,
        onClick: () => router.push("/order/create"),
      },
      () => "新建订单",
    ),
    h(
      ElButton,
      {
        type: "success",
        icon: Upload,
        onClick: () => {
          importDialogVisible.value = true;
        },
      },
      () => "导入订单",
    ),
    h(
      ElButton,
      {
        type: "warning",
        icon: Download,
        loading: exporting.value,
        onClick: handleExport,
      },
      () =>
        selectedOrders.value.length > 0
          ? `导出选中(${selectedOrders.value.length})`
          : "导出全部",
    ),
    selectedOrders.value.length > 0
      ? h(
          ElButton,
          {
            type: "danger",
            icon: Delete,
            onClick: handleBatchDelete,
          },
          () => `批量删除(${selectedOrders.value.length})`,
        )
      : null,
  ]);

// 批量删除
const handleBatchDelete = async () => {
  if (selectedOrders.value.length === 0) {
    ElMessage.warning("请选择要删除的订单");
    return;
  }

  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedOrders.value.length} 条订单吗？`,
      "批量删除",
      { type: "warning" },
    );

    const orderNos = selectedOrders.value
      .map((o) => o.orderNo)
      .filter((no): no is string => !!no);
    const result = await batchDeleteOrders(orderNos);
    ElMessage.success(`成功删除 ${result.deleted} 条订单`);
    selectedOrders.value = [];
    tableRef.value?.refresh();
  } catch {}
};

// 处理选择变化
const handleSelectionChange = (selection: Order[]) => {
  selectedOrders.value = selection;
};

// 导入成功回调
const handleImportSuccess = (result: ImportResult) => {
  if (result.success > 0) {
    // 刷新列表
    tableRef.value?.refresh();
  }
};

// 导出订单
const handleExport = async () => {
  exporting.value = true;
  try {
    const request: ExportRequest = {};

    if (selectedOrders.value.length > 0) {
      // 导出选中订单
      request.ids = selectedOrders.value
        .filter((order) => order.id !== undefined)
        .map((order) => order.id as number);
    } else {
      // 导出当前筛选条件下的全部订单
      request.filters = {
        page: 1,
        pageSize: 10000, // 导出时不分页
        ...currentFilters.value,
      };
    }

    const blob = await exportOrders(request);
    downloadOrderExport(blob);
    ElMessage.success("导出成功");
  } catch {
    ElMessage.error("导出失败，请重试");
  } finally {
    exporting.value = false;
  }
};

// 从后端API加载数据
const loadData = async (params: any) => {
  try {
    // 保存当前筛选条件（用于导出）
    currentFilters.value = {
      orderNo: params.orderNo,
      trackingNo: params.trackingNo,
      status: params.status,
      cargoType: params.cargoType,
      cargoName: params.cargoName,
      expressCompany: params.expressCompany,
      senderName: params.senderName,
      receiverName: params.receiverName,
    };

    const result = await getOrders({
      page: params.page,
      pageSize: params.pageSize,
      orderNo: params.orderNo,
      trackingNo: params.trackingNo,
      status: params.status,
      cargoType: params.cargoType,
      cargoName: params.cargoName,
      expressCompany: params.expressCompany,
      senderName: params.senderName,
      receiverName: params.receiverName,
    });
    return { data: result.data, total: result.total };
  } catch (error) {
    ElMessage.error("获取订单列表失败");
    return { data: [], total: 0 };
  }
};

// 组件挂载时加载快递公司列表
onMounted(() => {
  loadExpressCompanies();
});
</script>
