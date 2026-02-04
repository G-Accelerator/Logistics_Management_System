<template>
  <page-container title="我的订单" description="查看订单状态，确认收货">
    <data-table
      ref="tableRef"
      :search-config="searchConfig"
      :columns="columns"
      :load-data="loadData"
      :show-toolbar="false"
      :show-selection="false"
      :operation-width="180"
    >
      <template #operation="{ row }">
        <el-button type="primary" link @click="handleViewTrack(row)">
          查看物流
        </el-button>
        <el-button
          v-if="row.status === 'shipping'"
          type="success"
          link
          @click="handleReceive(row)"
        >
          确认收货
        </el-button>
      </template>
    </data-table>
  </page-container>
</template>

<script setup lang="ts">
import { h, ref } from "vue";
import { useRouter } from "vue-router";
import { ElButton, ElTag, ElMessage, ElMessageBox } from "element-plus";
import { DocumentCopy } from "@element-plus/icons-vue";
import PageContainer from "../../../components/layout/PageContainer/index.vue";
import DataTable from "../../../components/business/DataTable/index.vue";
import { getBuyerOrders, receiveOrder } from "../../../api/order";

const router = useRouter();
const tableRef = ref<InstanceType<typeof DataTable> | null>(null);

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
  { prop: "senderName", label: "发货人", width: 100 },
  {
    prop: "origin",
    label: "发货地址",
    minWidth: 180,
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

// 查看物流
const handleViewTrack = (row: any) => {
  router.push({
    path: "/transport/track",
    query: { orderNo: row.orderNo },
  });
};

// 确认收货
const handleReceive = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要确认收货订单 ${row.orderNo} 吗？`,
      "确认收货",
      { type: "warning" },
    );
    await receiveOrder(row.orderNo);
    ElMessage.success("收货成功");
    tableRef.value?.refresh();
  } catch (error: any) {
    if (error !== "cancel") {
      ElMessage.error(error?.message || "收货失败");
    }
  }
};

// 从后端API加载数据（买家专用接口，自动根据token过滤）
const loadData = async (params: any) => {
  try {
    const result = await getBuyerOrders({
      page: params.page,
      pageSize: params.pageSize,
      orderNo: params.orderNo,
      status: params.status || undefined,
      cargoName: params.cargoName,
    });
    return { data: result.data, total: result.total };
  } catch (error) {
    ElMessage.error("获取订单列表失败");
    return { data: [], total: 0 };
  }
};
</script>
