<template>
  <page-container
    title="用户管理"
    description="根据订单中的收发货手机号自动汇总买家与卖家，展示各状态订单数量（数据来源于订单，只读）。"
  >
    <el-tabs v-model="activeRole" class="role-tabs" @tab-change="onRoleChange">
      <el-tab-pane label="买家" name="buyer" />
      <el-tab-pane label="卖家" name="seller" />
    </el-tabs>

    <data-table
      :key="activeRole"
      ref="tableRef"
      :search-config="searchConfig"
      :columns="columns"
      :load-data="loadData"
      :show-toolbar="false"
      :show-operation="false"
      :show-index="false"
      table-class="participant-table"
      :page-sizes="[10, 20, 50]"
    />
  </page-container>
</template>

<script setup lang="tsx">
import { computed, h, ref } from "vue";
import { ElMessage, ElTag } from "element-plus";
import PageContainer from "../../../components/layout/PageContainer/index.vue";
import DataTable from "../../../components/business/DataTable/index.vue";
import {
  getOrderParticipants,
  type ParticipantRole,
} from "../../../api/system/orderParticipant";

defineOptions({ name: "UserSettings" });

const tableRef = ref<InstanceType<typeof DataTable> | null>(null);
const activeRole = ref<ParticipantRole>("buyer");

const countCell =
  (prop: string, tagType: "warning" | "primary" | "success" | "info") =>
  (row: Record<string, number>) =>
    row[prop] > 0
      ? h(ElTag, { type: tagType, size: "small" }, () => String(row[prop]))
      : h("span", { class: "zero-count" }, "0");

const searchConfig = computed(() => [
  {
    prop: "keyword",
    label:
      activeRole.value === "buyer" ? "收货手机/姓名" : "发货手机/姓名",
    type: "input" as const,
    placeholder: "模糊搜索",
  },
  {
    prop: "status",
    label: "含订单状态",
    type: "select" as const,
    placeholder: "不限",
    options: [
      { label: "待发货", value: "pending" },
      { label: "运输中", value: "shipping" },
      { label: "已完成", value: "completed" },
      { label: "已取消", value: "cancelled" },
    ],
  },
]);

const columns = computed(() => [
  {
    prop: "phone",
    label: activeRole.value === "buyer" ? "收货手机" : "发货手机",
    showOverflowTooltip: true,
  },
  {
    prop: "displayName",
    label: activeRole.value === "buyer" ? "收货人" : "发货人",
    showOverflowTooltip: true,
  },
  {
    prop: "pendingCount",
    label: "待发货",
    align: "center" as const,
    render: countCell("pendingCount", "warning"),
  },
  {
    prop: "shippingCount",
    label: "在途",
    align: "center" as const,
    render: countCell("shippingCount", "primary"),
  },
  {
    prop: "completedCount",
    label: "已完成",
    align: "center" as const,
    render: countCell("completedCount", "success"),
  },
  {
    prop: "cancelledCount",
    label: "已取消",
    align: "center" as const,
    render: countCell("cancelledCount", "info"),
  },
  {
    prop: "totalCount",
    label: "订单总数",
    align: "center" as const,
  },
  {
    prop: "lastOrderTime",
    label: "最近订单时间",
    showOverflowTooltip: true,
  },
]);

const loadData = async (params: Record<string, unknown>) => {
  try {
    const result = await getOrderParticipants({
      role: activeRole.value,
      keyword: (params.keyword as string)?.trim() || undefined,
      status: (params.status as string) || undefined,
      page: params.page as number,
      pageSize: params.pageSize as number,
    });
    return { data: result.data, total: result.total };
  } catch {
    ElMessage.error("加载用户数据失败");
    return { data: [], total: 0 };
  }
};

const onRoleChange = () => {
  tableRef.value?.resetAndRefresh();
};
</script>

<style scoped>
.role-tabs {
  margin-bottom: 4px;
}

:deep(.participant-table) {
  width: 100%;
}

:deep(.participant-table colgroup col) {
  width: 12.5% !important;
}

:deep(.participant-table .el-table__header .cell),
:deep(.participant-table .el-table__body .cell) {
  padding-left: 8px;
  padding-right: 8px;
}

:deep(.zero-count) {
  color: var(--text-tertiary);
  font-size: 13px;
}
</style>
