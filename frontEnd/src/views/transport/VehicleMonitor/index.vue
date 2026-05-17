<template>
  <page-container
    title="车辆监控"
    description="展示运输中已派车辆；离线、超速优先展示。在线状态在运单「在途管控」中手动维护并持久保存。所有车辆可发送常规消息。"
  >
    <data-table
      ref="tableRef"
      :columns="columns"
      :load-data="loadData"
      :search-config="[]"
      :show-pagination="false"
      :show-index="false"
      :show-toolbar="true"
      :toolbar-left="toolbarLeft"
      row-key="orderNo"
      :row-class-name="rowClassName"
      empty-text="暂无运输中的派车记录"
      table-class="monitor-table"
      :operation-width="120"
    >
      <template #operation="{ row }">
        <el-button link type="primary" size="small" @click="openNotify(row)">
          {{
            row.offlineAlert || row.overspeed ? "发送消息" : "常规消息"
          }}
        </el-button>
      </template>
    </data-table>

    <el-dialog
      v-model="notifyVisible"
      title="发送消息"
      width="560px"
      destroy-on-close
      @closed="resetNotify"
    >
      <template v-if="notifyRow">
        <p class="notify-meta">
          {{ notifyRow.plateNumber }} · {{ notifyRow.driverName }} ·
          {{ notifyRow.driverPhone }}
        </p>
        <el-form label-width="90px">
          <el-form-item label="模板类型">
            <el-radio-group v-model="notifyType" @change="onNotifyTypeChange">
              <el-radio value="OVERSPEED" :disabled="!notifyRow.overspeed">
                超速
              </el-radio>
              <el-radio value="OFFLINE" :disabled="!notifyRow.offlineAlert">
                离线
              </el-radio>
              <el-radio value="REGULAR">常规</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="消息内容">
            <el-input
              v-model="notifyContent"
              type="textarea"
              :rows="5"
              maxlength="2000"
              show-word-limit
              placeholder="根据模板生成，可在此基础上修改后发送"
            />
            <el-button
              class="reset-template-btn"
              link
              type="primary"
              :loading="previewLoading"
              @click="refreshPreview"
            >
              恢复为模板内容
            </el-button>
          </el-form-item>
        </el-form>
      </template>
      <template #footer>
        <el-button @click="notifyVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="notifySending"
          :disabled="!notifyContent.trim()"
          @click="submitNotify"
        >
          发送
        </el-button>
      </template>
    </el-dialog>
  </page-container>
</template>

<script setup lang="tsx">
import { h, onMounted, onUnmounted, ref, watch } from "vue";
import { ElButton, ElMessage, ElTag } from "element-plus";
import { Refresh } from "@element-plus/icons-vue";
import PageContainer from "../../../components/layout/PageContainer/index.vue";
import DataTable from "../../../components/business/DataTable/index.vue";
import {
  getVehicleMonitoring,
  previewVehicleNotify,
  sendVehicleNotify,
  type VehicleMonitoringRow,
  type VehicleNotifyTemplateType,
} from "../../../api/system/vehicle";

defineOptions({ name: "VehicleMonitor" });

const tableRef = ref<InstanceType<typeof DataTable> | null>(null);
let timer: ReturnType<typeof setInterval> | null = null;

const notifyVisible = ref(false);
const notifyRow = ref<VehicleMonitoringRow | null>(null);
const notifyType = ref<VehicleNotifyTemplateType>("REGULAR");
const notifyContent = ref("");
const notifySending = ref(false);
const previewLoading = ref(false);

const rowClassName = ({ row }: { row: VehicleMonitoringRow }) => {
  if (row.offlineAlert || row.overspeed) return "monitor-row-alert";
  return "";
};

const toolbarLeft = () =>
  h(
    ElButton,
    {
      type: "primary",
      onClick: () => tableRef.value?.refresh(),
    },
    () => [h(Refresh), " 刷新"],
  );

const columns = [
  {
    prop: "alert",
    label: "预警",
    width: 120,
    align: "center" as const,
    render: (row: VehicleMonitoringRow) => {
      if (row.offlineAlert) {
        return h(ElTag, { type: "danger", size: "small" }, () => "离线");
      }
      if (row.overspeed) {
        return h(ElTag, { type: "warning", size: "small" }, () => "超速");
      }
      return h("span", { class: "muted" }, "正常");
    },
  },
  { prop: "plateNumber", label: "车牌", width: 120 },
  { prop: "vehicleType", label: "类型", width: 90 },
  { prop: "driverName", label: "驾驶员", width: 90 },
  { prop: "driverPhone", label: "电话", width: 120 },
  {
    prop: "online",
    label: "在线",
    width: 80,
    align: "center" as const,
    render: (row: VehicleMonitoringRow) =>
      h(
        ElTag,
        { type: row.online ? "success" : "info", size: "small" },
        () => (row.online ? "是" : "否"),
      ),
  },
  { prop: "speedLimitKmh", label: "限速", width: 80 },
  { prop: "currentSpeedKmh", label: "当前车速", width: 100 },
  { prop: "orderNo", label: "订单号", minWidth: 160 },
  { prop: "trackingNo", label: "运单号", minWidth: 180 },
];

const loadData = async () => {
  try {
    const data = await getVehicleMonitoring();
    return { data, total: data.length };
  } catch {
    ElMessage.error("加载监控数据失败");
    return { data: [], total: 0 };
  }
};

const isAlertRow = (row: VehicleMonitoringRow) =>
  row.offlineAlert || row.overspeed;

const defaultNotifyType = (
  row: VehicleMonitoringRow,
): VehicleNotifyTemplateType => {
  if (row.overspeed) return "OVERSPEED";
  if (row.offlineAlert) return "OFFLINE";
  return "REGULAR";
};

const refreshPreview = async () => {
  const row = notifyRow.value;
  if (!row) return;
  previewLoading.value = true;
  try {
    notifyContent.value = await previewVehicleNotify(
      row.vehicleId,
      row.orderNo,
      notifyType.value,
    );
  } catch {
    ElMessage.error("加载模板内容失败");
  } finally {
    previewLoading.value = false;
  }
};

const onNotifyTypeChange = () => {
  refreshPreview();
};

const openNotify = (row: VehicleMonitoringRow) => {
  notifyRow.value = row;
  notifyType.value = isAlertRow(row) ? defaultNotifyType(row) : "REGULAR";
  notifyVisible.value = true;
};

const resetNotify = () => {
  notifyRow.value = null;
  notifyContent.value = "";
};

watch(notifyVisible, (v) => {
  if (v) refreshPreview();
});

const submitNotify = async () => {
  const row = notifyRow.value;
  const text = notifyContent.value.trim();
  if (!row || !text) {
    ElMessage.warning("请填写消息内容");
    return;
  }
  notifySending.value = true;
  try {
    await sendVehicleNotify({
      vehicleId: row.vehicleId,
      orderNo: row.orderNo,
      templateType: notifyType.value,
      content: text,
    });
    ElMessage.success("消息已发送");
    notifyVisible.value = false;
  } catch {
    /* 错误已由 axios 拦截器提示 */
  } finally {
    notifySending.value = false;
  }
};

onMounted(() => {
  timer = setInterval(() => tableRef.value?.refresh(), 45000);
});

onUnmounted(() => {
  if (timer) clearInterval(timer);
});
</script>

<style scoped>
.muted {
  color: var(--el-text-color-placeholder);
  font-size: 12px;
}
.notify-meta {
  margin: 0 0 16px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}
.reset-template-btn {
  margin-top: 8px;
  padding: 0;
}
:deep(.monitor-row-alert) {
  background-color: var(--el-color-danger-light-9) !important;
}
:deep(.monitor-row-alert.el-table__row--striped) {
  background-color: var(--el-color-danger-light-8) !important;
}
</style>
