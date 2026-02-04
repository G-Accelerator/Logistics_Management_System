<template>
  <page-container title="我的发货" description="管理待发货订单，确认发货">
    <data-table
      ref="tableRef"
      :search-config="searchConfig"
      :columns="columns"
      :load-data="loadData"
      :show-toolbar="true"
      :show-selection="true"
      :toolbar-left="renderToolbarLeft"
      :operations="operations"
      :operation-width="120"
      @selection-change="handleSelectionChange"
    />
  </page-container>
</template>

<script setup lang="ts">
import { h, ref, computed } from "vue";
import { ElButton, ElTag, ElMessage, ElMessageBox } from "element-plus";
import { Van, DocumentCopy } from "@element-plus/icons-vue";
import PageContainer from "../../../components/layout/PageContainer/index.vue";
import DataTable from "../../../components/business/DataTable/index.vue";
import { getSellerOrders, shipOrder, batchShip } from "../../../api/order";

const tableRef = ref<InstanceType<typeof DataTable> | null>(null);
const selectedOrders = ref<any[]>([]);

// 搜索配置
const searchConfig = [
  {
    prop: "orderNo",
    label: "订单号",
    type: "input" as const,
    placeholder: "请输入订单号",
  },
  {
    prop: "receiverName",
    label: "收货人",
    type: "input" as const,
    placeholder: "请输入收货人",
  },
  {
    prop: "cargoName",
    label: "货物名称",
    type: "input" as const,
    placeholder: "请输入货物名称",
  },
];

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
      expressCompanyMap[row.expressCompany] || row.expressCompany || "-",
  },
  { prop: "receiverName", label: "收货人", width: 100 },
  { prop: "receiverPhone", label: "收货人电话", width: 130 },
  {
    prop: "destination",
    label: "收货地址",
    minWidth: 200,
    showOverflowTooltip: true,
  },
  {
    prop: "status",
    label: "状态",
    width: 90,
    align: "center" as const,
    render: () => h(ElTag, { type: "warning", size: "small" }, () => "待发货"),
  },
  { prop: "createTime", label: "创建时间", width: 160 },
];

// 单个发货
const handleShip = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要发货订单 ${row.orderNo} 吗？`,
      "确认发货",
      { type: "warning" },
    );
    await shipOrder(row.orderNo);
    ElMessage.success("发货成功");
    tableRef.value?.refresh();
  } catch (error: any) {
    if (error !== "cancel") {
      ElMessage.error(error?.message || "发货失败");
    }
  }
};

// 操作按钮
const operations = [
  {
    label: "确认发货",
    type: "primary" as const,
    handler: handleShip,
  },
];

// 选择变化
const handleSelectionChange = (selection: any[]) => {
  selectedOrders.value = selection;
};

// 批量发货
const handleBatchShip = async () => {
  if (selectedOrders.value.length === 0) {
    ElMessage.warning("请选择要发货的订单");
    return;
  }

  try {
    await ElMessageBox.confirm(
      `确定要批量发货选中的 ${selectedOrders.value.length} 个订单吗？`,
      "批量发货",
      { type: "warning" },
    );

    const orderNos = selectedOrders.value.map((o) => o.orderNo);
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

// 是否有选中订单
const hasSelection = computed(() => selectedOrders.value.length > 0);

// 工具栏左侧
const renderToolbarLeft = () =>
  h(
    ElButton,
    {
      type: "primary",
      icon: Van,
      disabled: !hasSelection.value,
      onClick: handleBatchShip,
    },
    () =>
      `批量发货${hasSelection.value ? ` (${selectedOrders.value.length})` : ""}`,
  );

// 从后端API加载数据 - 只加载卖家的待发货订单
const loadData = async (params: any) => {
  try {
    const result = await getSellerOrders({
      page: params.page,
      pageSize: params.pageSize,
      orderNo: params.orderNo,
      status: "pending", // 只查询待发货订单
      cargoName: params.cargoName,
      receiverName: params.receiverName,
    });
    return { data: result.data, total: result.total };
  } catch (error) {
    ElMessage.error("获取订单列表失败");
    return { data: [], total: 0 };
  }
};
</script>
