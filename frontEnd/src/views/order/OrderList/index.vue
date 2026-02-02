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
      :operation-width="180"
    />

    <!-- 地图弹窗 -->
    <MapDialog
      v-model="showMapDialog"
      :title="`订单 ${currentOrderNo} 物流路线`"
      :points="mapPoints"
    />
  </page-container>
</template>

<script setup lang="ts">
import { h, ref } from "vue";
import { useRouter } from "vue-router";
import { ElButton, ElTag, ElMessage, ElMessageBox } from "element-plus";
import { Plus, DocumentCopy } from "@element-plus/icons-vue";
import PageContainer from "../../../components/layout/PageContainer/index.vue";
import DataTable from "../../../components/business/DataTable/index.vue";
import MapDialog from "../../../components/business/MapDialog/index.vue";
import type { MapPoint } from "../../../components/business/MapDialog/types";
import { getOrders, getTrackPoints, deleteOrder } from "../../../api/order";

const router = useRouter();
const tableRef = ref<InstanceType<typeof DataTable> | null>(null);
const showMapDialog = ref(false);
const currentOrderNo = ref("");
const mapPoints = ref<MapPoint[]>([]);

// 复制订单号
const copyOrderNo = async (orderNo: string) => {
  try {
    await navigator.clipboard.writeText(orderNo);
    ElMessage.success("已复制");
  } catch {
    ElMessage.warning("复制失败");
  }
};

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
  { prop: "createTime", label: "创建时间", type: "daterange" as const },
];

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
            copyOrderNo(row.orderNo);
          },
        }),
      ]),
  },
  { prop: "cargoName", label: "货物名称", minWidth: 100 },
  {
    prop: "cargoType",
    label: "货物类型",
    width: 90,
    formatter: (row: any) => cargoTypeMap[row.cargoType] || row.cargoType,
  },
  {
    prop: "expressCompany",
    label: "快递公司",
    width: 90,
    formatter: (row: any) =>
      expressCompanyMap[row.expressCompany] || row.expressCompany || "-",
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
  { prop: "receiverName", label: "收货人", width: 80 },
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

// 查看地图
const viewMap = async (row: any) => {
  try {
    currentOrderNo.value = row.orderNo;
    const points = await getTrackPoints(row.orderNo);
    mapPoints.value = points.map((pt, idx) => ({
      lng: pt.lng,
      lat: pt.lat,
      name: pt.status,
      address: pt.location,
      isStart: idx === 0,
      isEnd: idx === points.length - 1,
    }));
    showMapDialog.value = true;
  } catch (e) {
    ElMessage.error("获取站点数据失败");
  }
};

// 操作按钮
const operations = [
  { label: "查看地图", type: "primary" as const, handler: viewMap },
  {
    label: "编辑",
    type: "warning" as const,
    handler: (row: any) => ElMessage.info(`编辑订单: ${row.orderNo}`),
  },
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
  h(
    ElButton,
    {
      type: "primary",
      icon: Plus,
      onClick: () => router.push("/order/create"),
    },
    () => "新建订单",
  );

// 从后端API加载数据
const loadData = async (params: any) => {
  try {
    const result = await getOrders({
      page: params.page,
      pageSize: params.pageSize,
      orderNo: params.orderNo,
      status: params.status,
      cargoType: params.cargoType,
    });
    return { data: result.data, total: result.total };
  } catch (error) {
    ElMessage.error("获取订单列表失败");
    return { data: [], total: 0 };
  }
};
</script>
