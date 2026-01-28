<template>
  <page-container title="库存管理" description="查看和管理仓库库存">
    <data-table
      ref="tableRef"
      :search-config="searchConfig"
      :columns="columns"
      :load-data="loadData"
      :operations="operations"
      :toolbar-left="renderToolbarLeft"
      :toolbar-right="renderToolbarRight"
      show-toolbar
    />
  </page-container>
</template>

<script setup lang="tsx">
import { ref } from "vue";
import {
  ElMessage,
  ElMessageBox,
  ElTag,
  ElProgress,
  ElButton,
} from "element-plus";
import { Plus, Warning, Download, Refresh } from "@element-plus/icons-vue";
import DataTable from "../../../components/business/DataTable/index.vue";
import PageContainer from "../../../components/layout/PageContainer/index.vue";

const tableRef = ref();

const handleAdd = () => ElMessage.info("新增库存功能");
const handleCheckLowStock = () => ElMessage.warning("库存预警功能");
const handleExport = () => ElMessage.success("导出成功");
const handleRefresh = () => {
  tableRef.value?.refresh();
  ElMessage.success("刷新成功");
};

const renderToolbarLeft = () => (
  <>
    <ElButton type="primary" onClick={handleAdd}>
      {{ icon: () => <Plus />, default: () => "新增库存" }}
    </ElButton>
    <ElButton type="warning" onClick={handleCheckLowStock}>
      {{ icon: () => <Warning />, default: () => "库存预警" }}
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
  { prop: "productName", label: "商品名称" },
  { prop: "sku", label: "SKU" },
  {
    prop: "warehouse",
    label: "仓库",
    type: "select" as const,
    options: [
      { label: "全部", value: "" },
      { label: "上海仓", value: "SH" },
      { label: "北京仓", value: "BJ" },
      { label: "广州仓", value: "GZ" },
      { label: "深圳仓", value: "SZ" },
    ],
  },
  {
    prop: "category",
    label: "分类",
    type: "select" as const,
    options: [
      { label: "全部", value: "" },
      { label: "电子产品", value: "electronics" },
      { label: "服装鞋帽", value: "clothing" },
      { label: "食品饮料", value: "food" },
      { label: "日用百货", value: "daily" },
    ],
  },
  {
    prop: ["minQuantity", "maxQuantity"],
    label: "库存范围",
    components: [
      {
        prop: "minQuantity",
        placeholder: "最小库存",
        style: { width: "110px" },
      },
      {
        prop: "maxQuantity",
        placeholder: "最大库存",
        style: { width: "110px" },
      },
    ],
  },
];

const getStockStatusType = (quantity: number, minStock: number) =>
  quantity === 0 ? "danger" : quantity < minStock ? "warning" : "success";
const getStockStatusText = (quantity: number, minStock: number) =>
  quantity === 0 ? "缺货" : quantity < minStock ? "预警" : "正常";
const getStockPercentage = (quantity: number, maxStock: number) =>
  Math.min(Math.floor((quantity / maxStock) * 100), 100);

const columns = [
  { prop: "sku", label: "SKU", width: 120, fixed: "left" as const },
  {
    prop: "productName",
    label: "商品名称",
    minWidth: 200,
    showOverflowTooltip: true,
  },
  {
    prop: "category",
    label: "分类",
    width: 100,
    formatter: (row: any) =>
      ({
        electronics: "电子产品",
        clothing: "服装鞋帽",
        food: "食品饮料",
        daily: "日用百货",
      })[row.category] || row.category,
  },
  {
    prop: "warehouse",
    label: "仓库",
    width: 100,
    formatter: (row: any) =>
      ({ SH: "上海仓", BJ: "北京仓", GZ: "广州仓", SZ: "深圳仓" })[
        row.warehouse
      ] || row.warehouse,
  },
  {
    prop: "quantity",
    label: "库存数量",
    width: 200,
    render: (row: any) => (
      <div style="display: flex; align-items: center; gap: 10px">
        <span style="min-width: 60px">
          {row.quantity} {row.unit}
        </span>
        <ElProgress
          percentage={getStockPercentage(row.quantity, row.maxStock)}
          status={row.quantity < row.minStock ? "exception" : undefined}
          stroke-width={6}
          style="flex: 1; max-width: 100px"
        />
      </div>
    ),
  },
  {
    prop: "minStock",
    label: "最低库存",
    width: 100,
    align: "center" as const,
    formatter: (row: any) => `${row.minStock} ${row.unit}`,
  },
  {
    prop: "stockStatus",
    label: "库存状态",
    width: 100,
    render: (row: any) => (
      <ElTag
        type={getStockStatusType(row.quantity, row.minStock)}
        effect="light"
      >
        {getStockStatusText(row.quantity, row.minStock)}
      </ElTag>
    ),
  },
  { prop: "location", label: "库位", width: 120 },
  { prop: "updateTime", label: "更新时间", width: 180 },
];

const operations = [
  {
    label: "调整",
    type: "primary" as const,
    handler: (row: any) => ElMessage.info(`调整库存：${row.productName}`),
  },
  {
    label: "删除",
    type: "danger" as const,
    handler: async (row: any) => {
      try {
        await ElMessageBox.confirm(
          `确定要删除 ${row.productName} 的库存记录吗？`,
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
  await new Promise((resolve) => setTimeout(resolve, 600));
  const products = [
    "iPhone 15 Pro",
    "MacBook Pro",
    "AirPods Pro",
    "iPad Air",
    "Apple Watch",
    "Nike运动鞋",
    "Adidas T恤",
    "可口可乐",
    "农夫山泉",
    "洗衣液",
  ];
  const mockData = Array.from({ length: 65 }, (_, i) => {
    const quantity = Math.floor(Math.random() * 1000);
    return {
      id: i + 1,
      sku: `SKU${String(i + 1).padStart(6, "0")}`,
      productName: products[i % products.length],
      category: ["electronics", "clothing", "food", "daily"][i % 4],
      warehouse: ["SH", "BJ", "GZ", "SZ"][i % 4],
      quantity,
      minStock: 100,
      maxStock: 1000,
      unit: ["件", "箱", "瓶", "包"][i % 4],
      location: `A${Math.floor(i / 10) + 1}-${(i % 10) + 1}`,
      updateTime: new Date(Date.now() - Math.random() * 7 * 24 * 60 * 60 * 1000)
        .toISOString()
        .slice(0, 19)
        .replace("T", " "),
    };
  });
  const { page = 1, pageSize = 10 } = params;
  return {
    data: mockData.slice((page - 1) * pageSize, page * pageSize),
    total: mockData.length,
  };
};
</script>
