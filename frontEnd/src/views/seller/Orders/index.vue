<template>
  <page-container title="运输列表" description="查看订单完成情况">
    <data-table
      ref="tableRef"
      :search-config="searchConfig"
      :columns="columns"
      :load-data="loadData"
      :show-toolbar="true"
      :operations="operations"
      :operation-width="100"
    />
  </page-container>
</template>

<script setup lang="tsx">
import { h, ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElButton, ElTag, ElMessage } from "element-plus";
import { DocumentCopy } from "@element-plus/icons-vue";
import PageContainer from "../../../components/layout/PageContainer/index.vue";
import DataTable from "../../../components/business/DataTable/index.vue";
import { getSellerOrders } from "../../../api/order";

const router = useRouter();
const tableRef = ref<InstanceType<typeof DataTable> | null>(null);

// 复制订单号
const copy = async (orderNo: string) => {
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
];

// 状态映射
const statusMap: Record<string, { label: string; type: string }> = {
  pending: { label: "待发货", type: "warning" },
  shipping: { label: "运输中", type: "primary" },
  completed: { label: "已完成", type: "success" },
  cancelled: { label: "已取消", type: "info" },
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
  { prop: "cargoName", label: "货物名称", minWidth: 120 },
  {
    prop: "expressCompany",
    label: "快递公司",
    width: 90,
    formatter: (row: any) =>
      row.expressCompanyName || row.expressCompany || "-",
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
  { prop: "receiverName", label: "收货人", width: 100 },
  { prop: "receiverPhone", label: "收货人电话", width: 130 },
  {
    prop: "destination",
    label: "目的地",
    minWidth: 200,
    showOverflowTooltip: true,
  },
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
  { prop: "createTime", label: "创建时间", width: 160 },
];

// 查看物流
const viewTrack = (row: any) => {
  router.push({ path: "/transport/track", query: { orderNo: row.orderNo } });
};

// 操作按钮
const operations = [
  { label: "查看物流", type: "primary" as const, handler: viewTrack },
];

// 从后端API加载数据（使用卖家专用接口）
const loadData = async (params: any) => {
  try {
    const result = await getSellerOrders({
      page: params.page,
      pageSize: params.pageSize,
      orderNo: params.orderNo,
      status: params.status,
      cargoName: params.cargoName,
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
