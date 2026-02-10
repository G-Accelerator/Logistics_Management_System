<template>
  <page-container title="我的发货" description="管理待发货订单，确认发货">
    <data-table
      ref="tableRef"
      :search-config="searchConfig"
      :columns="columns"
      :load-data="loadData"
      :show-toolbar="true"
      :show-selection="false"
      :operations="operations"
      :operation-width="120"
    />

    <!-- 发货抽屉 -->
    <ship-drawer
      v-model="shipDialogVisible"
      :order="currentOrder"
      @success="handleShipSuccess"
    />
  </page-container>
</template>

<script setup lang="ts">
import { h, ref, onMounted } from "vue";
import { ElButton, ElTag, ElMessage } from "element-plus";
import { DocumentCopy } from "@element-plus/icons-vue";
import PageContainer from "../../../components/layout/PageContainer/index.vue";
import DataTable from "../../../components/business/DataTable/index.vue";
import ShipDrawer from "../../../components/business/ShipDrawer/index.vue";
import { getSellerOrders } from "../../../api/order";
import type { Order } from "../../../api/order/types";
import { useExpressCompanyStore } from "../../../store/expressCompany";

const tableRef = ref<InstanceType<typeof DataTable> | null>(null);
const expressCompanyStore = useExpressCompanyStore();

// 发货弹窗状态
const shipDialogVisible = ref(false);
const currentOrder = ref<Order | null>(null);

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
    width: 180,
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
  {
    prop: "trackingNo",
    label: "运单号",
    width: 180,
    render: (row: any) =>
      row.trackingNo
        ? h("div", { style: "display: flex; align-items: center; gap: 4px;" }, [
            h(
              "span",
              { style: "overflow: hidden; text-overflow: ellipsis;" },
              row.trackingNo,
            ),
            h(ElButton, {
              size: "small",
              icon: DocumentCopy,
              link: true,
              onClick: (e: Event) => {
                e.stopPropagation();
                copyOrderNo(row.trackingNo);
              },
            }),
          ])
        : h("span", { style: "color: #999;" }, "-"),
  },
  { prop: "cargoName", label: "货物名称", minWidth: 120 },
  {
    prop: "expressCompany",
    label: "快递公司",
    width: 100,
    formatter: (row: any) =>
      expressCompanyStore.companyMap[row.expressCompany] ||
      row.expressCompany ||
      "-",
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

// 打开发货弹窗
const openShipDialog = (row: any) => {
  currentOrder.value = row;
  shipDialogVisible.value = true;
};

// 发货成功回调
const handleShipSuccess = () => {
  tableRef.value?.refresh();
};

// 操作按钮
const operations = [
  {
    label: "确认发货",
    type: "primary" as const,
    handler: openShipDialog,
  },
];

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

// 初始化快递公司数据
onMounted(() => {
  // 数据由 ExpressCompanySelect 组件自动加载
});
</script>
