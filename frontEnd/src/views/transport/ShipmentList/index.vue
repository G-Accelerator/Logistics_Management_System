<template>
  <page-container title="货运管理" description="管理所有货运单据">
    <data-table
      ref="tableRef"
      :search-config="searchConfig"
      :columns="columns"
      :load-data="loadData"
      :operations="operations"
      :toolbar-left="renderToolbarLeft"
      :toolbar-right="renderToolbarRight"
      show-selection
      show-toolbar
      @selection-change="handleSelectionChange"
    />

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form :model="formData" label-width="100px">
        <el-form-item label="货运单号">
          <el-input
            v-model="formData.shipmentNo"
            placeholder="自动生成"
            disabled
          />
        </el-form-item>
        <el-form-item label="客户名称">
          <el-input v-model="formData.customer" placeholder="请输入客户名称" />
        </el-form-item>
        <el-form-item label="起始地">
          <el-input v-model="formData.origin" placeholder="请输入起始地" />
        </el-form-item>
        <el-form-item label="目的地">
          <el-input v-model="formData.destination" placeholder="请输入目的地" />
        </el-form-item>
        <el-form-item label="货物类型">
          <el-select v-model="formData.cargoType" placeholder="请选择货物类型">
            <el-option label="普通货物" value="normal" />
            <el-option label="易碎品" value="fragile" />
            <el-option label="危险品" value="dangerous" />
            <el-option label="冷链" value="cold" />
          </el-select>
        </el-form-item>
        <el-form-item label="运费">
          <el-input-number v-model="formData.freight" :min="0" :precision="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </page-container>
</template>

<script setup lang="tsx">
import { ref, reactive } from "vue";
import {
  ElMessage,
  ElMessageBox,
  ElInputNumber,
  ElTag,
  ElProgress,
  ElButton,
} from "element-plus";
import {
  Plus,
  Delete,
  Upload,
  Download,
  Refresh,
} from "@element-plus/icons-vue";
import DataTable from "../../../components/business/DataTable/index.vue";
import PageContainer from "../../../components/layout/PageContainer/index.vue";

const tableRef = ref();
const selectedRows = ref<any[]>([]);
const dialogVisible = ref(false);
const dialogTitle = ref("新建货运单");
const formData = reactive({
  shipmentNo: "",
  customer: "",
  origin: "",
  destination: "",
  cargoType: "normal",
  freight: 0,
});

const handleCreate = () => {
  dialogTitle.value = "新建货运单";
  Object.assign(formData, {
    shipmentNo: `SHP${new Date().getFullYear()}${String(Date.now()).slice(-6)}`,
    customer: "",
    origin: "",
    destination: "",
    cargoType: "normal",
    freight: 0,
  });
  dialogVisible.value = true;
};

const handleEdit = (row: any) => {
  dialogTitle.value = "编辑货运单";
  Object.assign(formData, row);
  dialogVisible.value = true;
};
const handleSubmit = () => {
  ElMessage.success("保存成功");
  dialogVisible.value = false;
  tableRef.value?.refresh();
};
const handleDialogClose = () => {};

const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning("请选择要删除的数据");
    return;
  }
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedRows.value.length} 条数据吗？`,
      "提示",
      { type: "warning" },
    );
    ElMessage.success("批量删除成功");
    tableRef.value?.refresh();
  } catch {}
};

const handleImport = () => ElMessage.info("导入功能开发中");
const handleExport = () => ElMessage.success("导出成功");
const handleRefresh = () => {
  tableRef.value?.refresh();
  ElMessage.success("刷新成功");
};

const renderToolbarLeft = () => (
  <>
    <ElButton type="primary" onClick={handleCreate}>
      {{ icon: () => <Plus />, default: () => "新建货运单" }}
    </ElButton>
    <ElButton
      type="danger"
      disabled={selectedRows.value.length === 0}
      onClick={handleBatchDelete}
    >
      {{
        icon: () => <Delete />,
        default: () => `批量删除 (${selectedRows.value.length})`,
      }}
    </ElButton>
    <ElButton type="success" onClick={handleImport}>
      {{ icon: () => <Upload />, default: () => "导入" }}
    </ElButton>
  </>
);

const renderToolbarRight = () => (
  <>
    <ElButton onClick={handleExport}>
      {{ icon: () => <Download />, default: () => "导出" }}
    </ElButton>
    <ElButton onClick={handleRefresh}>
      {{ icon: () => <Refresh />, default: () => "刷新" }}
    </ElButton>
  </>
);

const searchConfig = [
  { prop: "shipmentNo", label: "货运单号", placeholder: "请输入货运单号" },
  { prop: "customer", label: "客户名称" },
  {
    prop: "status",
    label: "状态",
    type: "select" as const,
    options: [
      { label: "全部", value: "" },
      { label: "待发货", value: "pending" },
      { label: "运输中", value: "shipping" },
      { label: "已送达", value: "delivered" },
      { label: "已完成", value: "completed" },
      { label: "已取消", value: "cancelled" },
    ],
  },
  {
    prop: "cargoType",
    label: "货物类型",
    type: "select" as const,
    options: [
      { label: "全部", value: "" },
      { label: "普通货物", value: "normal" },
      { label: "易碎品", value: "fragile" },
      { label: "危险品", value: "dangerous" },
      { label: "冷链", value: "cold" },
    ],
  },
  { prop: "createTime", label: "创建时间", type: "daterange" as const },
  {
    prop: ["minPrice", "maxPrice"],
    label: "运费范围",
    render: (model: Record<string, any>) => (
      <div style="display: flex; gap: 8px; align-items: center;">
        <ElInputNumber
          v-model={model.minPrice}
          min={0}
          precision={2}
          placeholder="最低价"
          controls-position="right"
          style="width: 130px"
        />
        <span style="color: #909399">-</span>
        <ElInputNumber
          v-model={model.maxPrice}
          min={0}
          precision={2}
          placeholder="最高价"
          controls-position="right"
          style="width: 130px"
        />
      </div>
    ),
  },
  {
    prop: ["origin", "destination"],
    label: "路线",
    components: [
      { prop: "origin", placeholder: "起始地", style: { width: "130px" } },
      { prop: "destination", placeholder: "目的地", style: { width: "130px" } },
    ],
  },
];

const getStatusType = (status: string) =>
  ({
    pending: "warning",
    shipping: "primary",
    delivered: "success",
    completed: "success",
    cancelled: "info",
  })[status] || "info";
const getStatusText = (status: string) =>
  ({
    pending: "待发货",
    shipping: "运输中",
    delivered: "已送达",
    completed: "已完成",
    cancelled: "已取消",
  })[status] || status;
const getPriorityType = (priority: string) =>
  ({ low: "info", normal: "info", high: "warning", urgent: "danger" })[
    priority
  ] || "info";
const getPriorityText = (priority: string) =>
  ({ low: "低", normal: "普通", high: "高", urgent: "紧急" })[priority] ||
  priority;

const columns = [
  { prop: "shipmentNo", label: "货运单号", width: 160, fixed: "left" as const },
  { prop: "customer", label: "客户", width: 120 },
  { prop: "origin", label: "起始地", width: 150 },
  { prop: "destination", label: "目的地", width: 150 },
  {
    prop: "cargoType",
    label: "货物类型",
    width: 100,
    formatter: (row: any) =>
      ({
        normal: "普通货物",
        fragile: "易碎品",
        dangerous: "危险品",
        cold: "冷链",
      })[row.cargoType] || row.cargoType,
  },
  {
    prop: "status",
    label: "状态",
    width: 100,
    render: (row: any) => (
      <ElTag type={getStatusType(row.status)} effect="dark">
        {getStatusText(row.status)}
      </ElTag>
    ),
  },
  {
    prop: "priority",
    label: "优先级",
    width: 90,
    render: (row: any) => (
      <ElTag type={getPriorityType(row.priority)} effect="plain" size="small">
        {getPriorityText(row.priority)}
      </ElTag>
    ),
  },
  {
    prop: "progress",
    label: "进度",
    width: 150,
    render: (row: any) => (
      <ElProgress
        percentage={row.progress}
        status={row.progress === 100 ? "success" : undefined}
        stroke-width={8}
      />
    ),
  },
  {
    prop: "freight",
    label: "运费",
    width: 120,
    align: "right" as const,
    formatter: (row: any) => `¥${row.freight.toFixed(2)}`,
  },
  { prop: "createTime", label: "创建时间", width: 180 },
  {
    prop: "remark",
    label: "备注",
    minWidth: 150,
    showOverflowTooltip: true,
    emptyText: "暂无备注",
  },
];

const operations = [
  {
    label: "查看",
    type: "primary" as const,
    handler: (row: any) => ElMessage.info(`查看货运单：${row.shipmentNo}`),
  },
  {
    label: "编辑",
    type: "warning" as const,
    handler: (row: any) => handleEdit(row),
  },
  {
    label: "追踪",
    type: "success" as const,
    handler: (row: any) => ElMessage.success(`追踪货运单：${row.shipmentNo}`),
  },
  {
    label: "删除",
    type: "danger" as const,
    handler: async (row: any) => {
      try {
        await ElMessageBox.confirm(
          `确定要删除货运单 ${row.shipmentNo} 吗？`,
          "提示",
          { type: "warning" },
        );
        ElMessage.success("删除成功");
        tableRef.value?.refresh();
      } catch {}
    },
  },
];

const loadData = async (params: any) => {
  await new Promise((resolve) => setTimeout(resolve, 800));
  const remarks = [
    "客户要求加急处理",
    "易碎品，请轻拿轻放",
    "冷链运输",
    null,
    "",
    "周末不收货",
    "需要提前电话联系",
    null,
    "货物较重",
    "",
  ];
  const mockData = Array.from({ length: 88 }, (_, i) => ({
    id: i + 1,
    shipmentNo: `SHP${new Date().getFullYear()}${String(i + 1).padStart(6, "0")}`,
    customer: ["顺丰速运", "京东物流", "中通快递", "圆通速递", "韵达快递"][
      i % 5
    ],
    origin: ["上海市", "北京市", "广州市", "深圳市", "杭州市"][i % 5],
    destination: ["成都市", "重庆市", "武汉市", "西安市", "南京市"][i % 5],
    cargoType: ["normal", "fragile", "dangerous", "cold"][i % 4],
    status: ["pending", "shipping", "delivered", "completed", "cancelled"][
      i % 5
    ],
    priority: ["low", "normal", "high", "urgent"][i % 4],
    progress: Math.floor(Math.random() * 100),
    freight: Math.random() * 5000 + 500,
    remark: remarks[i % remarks.length],
    createTime: new Date(Date.now() - Math.random() * 30 * 24 * 60 * 60 * 1000)
      .toISOString()
      .slice(0, 19)
      .replace("T", " "),
  }));
  const { page = 1, pageSize = 10 } = params;
  return {
    data: mockData.slice((page - 1) * pageSize, page * pageSize),
    total: mockData.length,
  };
};

const handleSelectionChange = (selection: any[]) => {
  selectedRows.value = selection;
};
</script>
