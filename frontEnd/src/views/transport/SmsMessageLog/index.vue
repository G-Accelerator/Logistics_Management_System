<template>
  <page-container
    title="消息记录"
    description="查看所有已发送的消息记录（自动预警与手动发送），支持多条件筛选。"
  >
    <data-table
      ref="tableRef"
      :search-config="searchConfig"
      :columns="columns"
      :load-data="loadData"
      :show-toolbar="false"
      :show-index="false"
      :operation-width="80"
      :page-sizes="[10, 20, 50]"
    >
      <template #operation="{ row }">
        <el-button link type="primary" size="small" @click="showDetail(row)">
          详情
        </el-button>
      </template>
    </data-table>

    <el-dialog v-model="detailVisible" title="消息详情" width="560px">
      <el-descriptions v-if="detailRow" :column="1" border>
        <el-descriptions-item label="发送时间">{{
          detailRow.sentAt
        }}</el-descriptions-item>
        <el-descriptions-item label="消息类型">{{
          detailRow.templateTypeLabel
        }}</el-descriptions-item>
        <el-descriptions-item label="触发方式">{{
          detailRow.triggerSource
        }}</el-descriptions-item>
        <el-descriptions-item label="收件人">{{
          detailRow.recipientPhone
        }}</el-descriptions-item>
        <el-descriptions-item label="车牌">{{
          detailRow.plateNumber || "—"
        }}</el-descriptions-item>
        <el-descriptions-item label="订单号">{{
          detailRow.orderNo || "—"
        }}</el-descriptions-item>
        <el-descriptions-item label="消息内容">
          <p class="detail-content">{{ detailRow.content }}</p>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </page-container>
</template>

<script setup lang="tsx">
import { h, ref } from "vue";
import { ElDatePicker, ElMessage, ElTag } from "element-plus";
import PageContainer from "../../../components/layout/PageContainer/index.vue";
import DataTable from "../../../components/business/DataTable/index.vue";
import {
  getSmsMessages,
  type SmsMessageLog,
} from "../../../api/system/smsMessage";

defineOptions({ name: "SmsMessageLog" });

const tableRef = ref<InstanceType<typeof DataTable> | null>(null);
const detailVisible = ref(false);
const detailRow = ref<SmsMessageLog | null>(null);

const typeTagType = (type: string) => {
  if (type === "OVERSPEED") return "warning";
  if (type === "OFFLINE") return "danger";
  return "info";
};

const searchConfig = [
  {
    prop: "templateType",
    label: "消息类型",
    type: "select" as const,
    placeholder: "全部",
    options: [
      { label: "超速预警", value: "OVERSPEED" },
      { label: "离线预警", value: "OFFLINE" },
      { label: "常规通知", value: "REGULAR" },
    ],
  },
  {
    prop: "triggerSource",
    label: "触发方式",
    type: "select" as const,
    placeholder: "全部",
    options: [
      { label: "自动-超速", value: "自动-超速" },
      { label: "自动-离线", value: "自动-离线" },
      { label: "手动-车辆监控", value: "手动-车辆监控" },
    ],
  },
  {
    prop: "orderNo",
    label: "订单号",
    type: "input" as const,
    placeholder: "模糊搜索",
  },
  {
    prop: "plateNumber",
    label: "车牌",
    type: "input" as const,
    placeholder: "模糊搜索",
  },
  {
    prop: "recipientPhone",
    label: "收件手机",
    type: "input" as const,
    placeholder: "模糊搜索",
  },
  {
    prop: "sentAtRange",
    label: "发送时间",
    render: (form: Record<string, unknown>) =>
      h(ElDatePicker, {
        modelValue: form.sentAtRange as [string, string] | null,
        "onUpdate:modelValue": (v: [string, string] | null) => {
          form.sentAtRange = v;
        },
        type: "datetimerange",
        rangeSeparator: "至",
        startPlaceholder: "开始",
        endPlaceholder: "结束",
        valueFormat: "YYYY-MM-DD HH:mm:ss",
        style: { width: "340px" },
      }),
  },
];

const columns = [
  { prop: "sentAt", label: "发送时间", width: 170 },
  {
    prop: "templateTypeLabel",
    label: "类型",
    width: 100,
    render: (row: SmsMessageLog) =>
      h(
        ElTag,
        { type: typeTagType(row.templateType), size: "small" },
        () => row.templateTypeLabel,
      ),
  },
  { prop: "triggerSource", label: "触发方式", width: 130 },
  { prop: "recipientPhone", label: "收件人", width: 120 },
  { prop: "plateNumber", label: "车牌", width: 110 },
  {
    prop: "orderNo",
    label: "订单号",
    minWidth: 150,
    showOverflowTooltip: true,
  },
  {
    prop: "content",
    label: "消息内容",
    minWidth: 240,
    showOverflowTooltip: true,
  },
];

const loadData = async (params: Record<string, unknown>) => {
  try {
    const range = params.sentAtRange as [string, string] | null | undefined;
    const result = await getSmsMessages({
      page: params.page as number,
      pageSize: params.pageSize as number,
      templateType: (params.templateType as string) || undefined,
      triggerSource: (params.triggerSource as string) || undefined,
      orderNo: (params.orderNo as string)?.trim() || undefined,
      plateNumber: (params.plateNumber as string)?.trim() || undefined,
      recipientPhone: (params.recipientPhone as string)?.trim() || undefined,
      startTime: range?.[0],
      endTime: range?.[1],
    });
    return { data: result.data, total: result.total };
  } catch {
    ElMessage.error("加载消息记录失败");
    return { data: [], total: 0 };
  }
};

const showDetail = (row: SmsMessageLog) => {
  detailRow.value = row;
  detailVisible.value = true;
};
</script>

<style scoped>
.detail-content {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
  line-height: 1.6;
}
</style>
