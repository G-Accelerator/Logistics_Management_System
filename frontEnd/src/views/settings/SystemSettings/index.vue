<template>
  <div class="system-settings">
    <el-tabs
      v-model="activeTab"
      class="settings-tabs"
      @tab-change="onTabChange"
    >
      <el-tab-pane label="快递公司管理" name="express">
        <div class="tab-content">
          <div class="toolbar">
            <el-button type="primary" @click="handleCreate">
              <el-icon><Plus /></el-icon>新增快递公司
            </el-button>
            <template v-if="!sortMode">
              <el-button
                @click="
                  sortMode = true;
                  initSortable();
                "
              >
                <el-icon><Sort /></el-icon>调整展示顺序
              </el-button>
            </template>
            <template v-else>
              <el-button type="success" @click="completeSortOrder">
                <el-icon><Check /></el-icon>完成排序
              </el-button>
              <el-button @click="toggleSortMode">取消</el-button>
            </template>
          </div>

          <div
            v-loading="loading"
            class="company-list"
            ref="sortableContainer"
            :key="listKey"
          >
            <div
              v-for="company in companies"
              :key="company.id"
              :data-id="company.id"
              class="company-card"
              :class="{ 'sort-mode': sortMode }"
            >
              <div class="card-header">
                <span v-if="sortMode" class="sort-handle">
                  <el-icon><Rank /></el-icon>
                </span>
                <div class="company-name">
                  <span class="name">{{ company.name }}</span>
                  <span class="code">{{ company.code }}</span>
                </div>
                <div class="card-actions">
                  <el-tag
                    :type="company.enabled ? 'success' : 'info'"
                    size="small"
                    effect="light"
                  >
                    {{ company.enabled ? "启用" : "禁用" }}
                  </el-tag>
                  <template v-if="!sortMode">
                    <el-button
                      link
                      type="primary"
                      size="small"
                      @click="handleEdit(company)"
                    >
                      <el-icon><Edit /></el-icon>编辑
                    </el-button>
                    <el-button
                      link
                      type="danger"
                      size="small"
                      @click="handleDelete(company)"
                    >
                      <el-icon><Delete /></el-icon>删除
                    </el-button>
                  </template>
                </div>
              </div>
              <div class="card-body">
                <div class="info-row">
                  <el-icon class="info-icon"><Ticket /></el-icon>
                  <span class="info-label">运单前缀</span>
                  <span class="info-value">{{ company.trackingPrefix }}</span>
                </div>
                <div class="info-row">
                  <el-icon class="info-icon"><Phone /></el-icon>
                  <span class="info-label">客服电话</span>
                  <span class="info-value">{{ company.phone || "-" }}</span>
                </div>
                <div class="info-row">
                  <el-icon class="info-icon"><Link /></el-icon>
                  <span class="info-label">官网</span>
                  <span class="info-value">
                    <a
                      v-if="company.website"
                      :href="company.website"
                      target="_blank"
                      >{{ company.website }}</a
                    >
                    <span v-else>-</span>
                  </span>
                </div>
              </div>
            </div>

            <el-empty
              v-if="companies.length === 0"
              description="暂无快递公司"
            />
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="车辆配置" name="vehicle">
        <div class="tab-content">
          <data-table
            v-if="activeTab === 'vehicle'"
            ref="vehicleTableRef"
            :search-config="[]"
            :columns="vehicleColumns"
            :load-data="loadVehicleTableData"
            :show-index="false"
            :show-toolbar="true"
            :toolbar-left="vehicleToolbarLeft"
            :operations="vehicleOperations"
            :operation-width="140"
            :page-sizes="[10, 20, 50]"
            empty-text="暂无车辆"
          />

        </div>
      </el-tab-pane>

      <el-tab-pane label="消息模板" name="sms">
        <div v-loading="smsLoading" class="tab-content sms-template-tab">
          <el-alert
            type="info"
            :closable="false"
            show-icon
            title="消息通知说明"
            class="sms-intro-alert"
          >
            <p>
              消息将按模板发送至驾驶员手机，发送记录可在「消息记录」中查询。
              模板支持占位符，保存后即时生效。
            </p>
          </el-alert>

          <div class="sms-layout">
            <aside class="sms-sidebar">
              <el-card shadow="never" class="sms-placeholder-card">
            <template #header>
              <span class="sms-card-title">占位符字段说明</span>
            </template>
            <p class="sms-placeholder-intro">
              在模板正文中写入下列占位符（含花括号），发送时将替换为对应订单/车辆的实时数据。
            </p>
            <div class="sms-placeholder-grid">
              <div
                v-for="field in SMS_PLACEHOLDERS"
                :key="field.key"
                class="sms-placeholder-item"
              >
                <code class="sms-placeholder-key">{{ field.key }}</code>
                <span class="sms-placeholder-label">{{ field.label }}</span>
                <span class="sms-placeholder-desc">{{ field.desc }}</span>
              </div>
            </div>
              </el-card>
            </aside>

            <main class="sms-main">
              <div class="sms-template-grid">
            <el-card
              v-for="item in orderedSmsTemplates"
              :key="item.type"
              shadow="never"
              class="sms-template-card"
            >
            <template #header>
                <div class="sms-template-card-header">
                <div class="sms-template-title-row">
                  <el-tag
                    :type="smsTemplateMeta(item.type).tagType"
                    size="small"
                    effect="light"
                  >
                    {{ smsTemplateMeta(item.type).label }}
                  </el-tag>
                  <span class="sms-type-code">{{ item.type }}</span>
                </div>
                <span v-if="item.updateTime" class="sms-update-time">
                  更新于 {{ item.updateTime }}
                </span>
              </div>
            </template>

            <p class="sms-template-desc">
              {{ smsTemplateMeta(item.type).usage }}
            </p>
            <div class="sms-trigger-box">
              <span class="sms-trigger-label">何时使用</span>
              <span class="sms-trigger-text">{{
                smsTemplateMeta(item.type).trigger
              }}</span>
            </div>

            <el-form-item label="模板正文" class="sms-content-item">
              <el-input
                v-model="item.content"
                type="textarea"
                :rows="4"
                maxlength="2000"
                show-word-limit
                :placeholder="smsTemplateMeta(item.type).placeholder"
              />
            </el-form-item>

            <div
              v-if="isSmsTemplateDirty(item.type)"
              class="sms-template-actions"
            >
              <el-button @click="revertSmsTemplate(item.type)">
                取消
              </el-button>
              <el-button
                type="primary"
                :loading="smsSavingType === item.type"
                @click="saveSmsTemplate(item)"
              >
                保存
              </el-button>
            </div>
            </el-card>
              </div>
            </main>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑快递公司' : '新增快递公司'"
      width="480px"
      destroy-on-close
      class="settings-dialog"
    >
      <el-form :model="form" label-width="90px" @submit.prevent="handleSave">
        <el-form-item label="代码" required>
          <el-input
            v-model="form.code"
            :disabled="isEdit"
            placeholder="如：sf, zto"
          />
        </el-form-item>
        <el-form-item label="名称" required>
          <el-input v-model="form.name" placeholder="如：顺丰速运" />
        </el-form-item>
        <el-form-item label="运单前缀" required>
          <el-input v-model="form.trackingPrefix" placeholder="如：SF, ZTO" />
        </el-form-item>
        <el-form-item label="客服电话">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="官网">
          <el-input v-model="form.website" />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="form.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="vehicleDialogVisible"
      :title="vehicleIsEdit ? '编辑车辆' : '新增车辆'"
      width="520px"
      destroy-on-close
      class="settings-dialog"
    >
      <el-form :model="vehicleForm" label-width="100px" @submit.prevent>
        <el-form-item label="车牌" required>
          <el-input
            v-model="vehicleForm.plateNumber"
            placeholder="如：京A12345"
            maxlength="20"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="车辆类型" required>
          <el-select
            v-model="vehicleForm.vehicleType"
            placeholder="请选择"
            style="width: 100%"
          >
            <el-option
              v-for="t in VEHICLE_TYPE_OPTIONS"
              :key="t"
              :label="t"
              :value="t"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="驾驶员" required>
          <el-input
            v-model="vehicleForm.driverName"
            placeholder="姓名"
            maxlength="50"
          />
        </el-form-item>
        <el-form-item label="驾驶员电话" required>
          <el-input
            v-model="vehicleForm.driverPhone"
            placeholder="11位手机号"
            maxlength="11"
          />
        </el-form-item>
        <el-form-item label="限速(km/h)" required>
          <el-input-number
            v-model="vehicleForm.speedLimitKmh"
            :min="1"
            :max="200"
            :step="5"
            controls-position="right"
            class="speed-input"
          />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="vehicleForm.enabled" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="vehicleForm.remark"
            type="textarea"
            :rows="2"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="vehicleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleVehicleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="tsx">
import { h, ref, computed, onMounted, nextTick } from "vue";
import { ElButton, ElMessage, ElMessageBox, ElTag } from "element-plus";
import {
  Plus,
  Sort,
  Check,
  Rank,
  Edit,
  Delete,
  Ticket,
  Phone,
  Link,
} from "@element-plus/icons-vue";
import Sortable from "sortablejs";
import {
  getExpressCompanies,
  createExpressCompany,
  updateExpressCompany,
  deleteExpressCompany,
  type ExpressCompany,
} from "../../../api/system/expressCompany";
import {
  getVehicles,
  createVehicle,
  updateVehicle,
  deleteVehicle,
  VEHICLE_TYPE_OPTIONS,
  type Vehicle,
  type VehicleType,
} from "../../../api/system/vehicle";
import {
  getSmsTemplates,
  updateSmsTemplate,
  type SmsTemplate,
  type SmsTemplateType,
} from "../../../api/system/smsTemplate";
import { useExpressCompanyStore } from "../../../store/expressCompany";
import DataTable from "../../../components/business/DataTable/index.vue";
import type { Column, Operation } from "../../../components/business/DataTable/types";

const activeTab = ref("express");
const companies = ref<ExpressCompany[]>([]);
const originalCompanies = ref<ExpressCompany[]>([]);
const loading = ref(false);
const dialogVisible = ref(false);
const isEdit = ref(false);
const sortMode = ref(false);
const sortableContainer = ref<HTMLElement>();
const listKey = ref(0);
let sortableInstance: ReturnType<typeof Sortable.create> | null = null;

const store = useExpressCompanyStore();

const vehicleTableRef = ref<InstanceType<typeof DataTable> | null>(null);
const vehicleDialogVisible = ref(false);
const vehicleIsEdit = ref(false);
const defaultVehicleType = (): VehicleType => VEHICLE_TYPE_OPTIONS[0];

const vehicleForm = ref<Vehicle>({
  plateNumber: "",
  vehicleType: defaultVehicleType(),
  driverName: "",
  driverPhone: "",
  speedLimitKmh: 80,
  enabled: true,
  remark: "",
});

const smsTemplates = ref<SmsTemplate[]>([]);
const smsLoading = ref(false);
const smsSavingType = ref<SmsTemplateType | "">("");
const smsSavedContent = ref<Record<SmsTemplateType, string>>({
  OVERSPEED: "",
  OFFLINE: "",
  REGULAR: "",
});

const syncSmsSavedContent = () => {
  const next: Record<SmsTemplateType, string> = {
    OVERSPEED: "",
    OFFLINE: "",
    REGULAR: "",
  };
  for (const t of smsTemplates.value) {
    next[t.type] = t.content ?? "";
  }
  smsSavedContent.value = next;
};

const isSmsTemplateDirty = (type: SmsTemplateType) => {
  const item = smsTemplates.value.find((t) => t.type === type);
  const current = (item?.content ?? "").trim();
  const saved = (smsSavedContent.value[type] ?? "").trim();
  return current !== saved;
};

const revertSmsTemplate = (type: SmsTemplateType) => {
  const item = smsTemplates.value.find((t) => t.type === type);
  if (item) {
    item.content = smsSavedContent.value[type] ?? "";
  }
};

const SMS_TEMPLATE_ORDER: SmsTemplateType[] = [
  "OVERSPEED",
  "OFFLINE",
  "REGULAR",
];

const SMS_PLACEHOLDERS = [
  { key: "{plateNumber}", label: "车牌号", desc: "承运车辆牌照" },
  { key: "{driverName}", label: "驾驶员", desc: "司机姓名" },
  { key: "{driverPhone}", label: "驾驶员电话", desc: "短信接收号码" },
  { key: "{vehicleType}", label: "车辆类型", desc: "如大货车、中货车" },
  {
    key: "{speedLimitKmh}",
    label: "限速",
    desc: "车辆配置的最高时速（km/h）",
  },
  {
    key: "{currentSpeedKmh}",
    label: "当前车速",
    desc: "订单在途实时车速（km/h）",
  },
  { key: "{orderNo}", label: "订单号", desc: "系统内部订单编号" },
  { key: "{trackingNo}", label: "运单号", desc: "对外物流单号" },
] as const;

type SmsTemplateMeta = {
  label: string;
  tagType: "warning" | "danger" | "info";
  usage: string;
  trigger: string;
  placeholder: string;
};

const SMS_TEMPLATE_META: Record<SmsTemplateType, SmsTemplateMeta> = {
  OVERSPEED: {
    label: "超速预警",
    tagType: "warning",
    usage: "提醒驾驶员当前车速已超过车辆限速，请减速。",
    trigger:
      "自动：运输中车速首次超过限速时发送；手动：车辆监控页对超速车辆发送。",
    placeholder:
      "例：【物流预警】车牌{plateNumber}当前{currentSpeedKmh}km/h，超过限速{speedLimitKmh}km/h，订单{orderNo}，请立即减速。",
  },
  OFFLINE: {
    label: "离线预警",
    tagType: "danger",
    usage: "提醒终端信号中断或车辆被标记为离线，需排查设备或联系驾驶员。",
    trigger:
      "自动：车辆在线状态由在线变为离线时发送；手动：车辆监控页对离线车辆发送。",
    placeholder:
      "例：【物流预警】车牌{plateNumber}驾驶员{driverName}设备离线，订单{orderNo}，请检查终端。",
  },
  REGULAR: {
    label: "常规通知",
    tagType: "info",
    usage: "通用业务通知，不依赖超速/离线条件，适合人工跟进或安抚性提醒。",
    trigger: "手动：车辆监控页对任意运输中车辆均可发送（可在模板基础上修改正文）。",
    placeholder:
      "例：【物流通知】{driverName}您好，订单{orderNo}运输中，如有疑问请联系调度。",
  },
};

const smsTemplateMeta = (type: SmsTemplateType) => SMS_TEMPLATE_META[type];

const orderedSmsTemplates = computed(() => {
  const map = new Map(smsTemplates.value.map((t) => [t.type, t]));
  return SMS_TEMPLATE_ORDER.map(
    (type) => map.get(type) ?? { type, content: "" },
  );
});

const form = ref<ExpressCompany>({
  code: "",
  name: "",
  trackingPrefix: "",
  sortOrder: 0,
  enabled: true,
});

const loadCompanies = async () => {
  loading.value = true;
  try {
    const result = await getExpressCompanies(1, 100);
    const sorted = result.data.sort(
      (a, b) => (a.sortOrder || 0) - (b.sortOrder || 0),
    );
    companies.value = sorted;
    originalCompanies.value = JSON.parse(JSON.stringify(sorted));
  } catch {
    ElMessage.error("获取快递公司列表失败");
  } finally {
    loading.value = false;
  }
};

const initSortable = () => {
  if (!sortableContainer.value) return;
  sortableInstance = Sortable.create(sortableContainer.value, {
    animation: 150,
    ghostClass: "sortable-ghost",
    dragClass: "sortable-drag",
    handle: ".card-header",
  });
};

const toggleSortMode = async () => {
  sortMode.value = !sortMode.value;
  if (sortMode.value) {
    await nextTick();
    initSortable();
  } else {
    if (sortableInstance) {
      sortableInstance.destroy();
      sortableInstance = null;
    }
    companies.value = JSON.parse(JSON.stringify(originalCompanies.value));
    listKey.value++;
  }
};

const completeSortOrder = async () => {
  try {
    if (!sortableContainer.value) return;
    const cards = sortableContainer.value.querySelectorAll(".company-card");
    const newOrder: ExpressCompany[] = [];

    cards.forEach((card, index) => {
      const company = companies.value.find(
        (c) => c.id === parseInt(card.getAttribute("data-id") || "0"),
      );
      if (company) newOrder.push({ ...company, sortOrder: index + 1 });
    });

    if (newOrder.length === 0) {
      newOrder.push(
        ...companies.value.map((company, index) => ({
          ...company,
          sortOrder: index + 1,
        })),
      );
    }

    await Promise.all(
      newOrder.map((company) => updateExpressCompany(company.id!, company)),
    );

    companies.value = newOrder;
    originalCompanies.value = JSON.parse(JSON.stringify(newOrder));
    sortMode.value = false;

    if (sortableInstance) {
      sortableInstance.destroy();
      sortableInstance = null;
    }
    await store.refresh();
    ElMessage.success("排序已保存");
  } catch (error: any) {
    ElMessage.error(error?.message || "保存排序失败");
    loadCompanies();
  }
};

const handleCreate = () => {
  isEdit.value = false;
  form.value = {
    code: "",
    name: "",
    trackingPrefix: "",
    sortOrder: 0,
    enabled: true,
  };
  dialogVisible.value = true;
};

const handleEdit = (company: ExpressCompany) => {
  isEdit.value = true;
  form.value = { ...company };
  dialogVisible.value = true;
};

const handleDelete = async (company: ExpressCompany) => {
  try {
    await ElMessageBox.confirm(`确定删除 ${company.name} 吗？`, "提示", {
      type: "warning",
    });
    await deleteExpressCompany(company.id!);
    ElMessage.success("删除成功");
    await store.refresh();
    loadCompanies();
  } catch (error: any) {
    if (error !== "cancel") ElMessage.error(error?.message || "删除失败");
  }
};

const handleSave = async () => {
  try {
    if (!form.value.code || !form.value.name || !form.value.trackingPrefix) {
      ElMessage.warning("请填写必填项");
      return;
    }

    if (isEdit.value) {
      await updateExpressCompany(form.value.id!, form.value);
      ElMessage.success("更新成功");
    } else {
      form.value.sortOrder = companies.value.length + 1;
      await createExpressCompany(form.value);
      ElMessage.success("创建成功");
    }
    dialogVisible.value = false;
    await store.refresh();
    loadCompanies();
  } catch (error: any) {
    ElMessage.error(error?.message || "操作失败");
  }
};

const vehicleColumns: Column[] = [
  { prop: "plateNumber", label: "车牌", width: 130 },
  { prop: "vehicleType", label: "车辆类型", width: 120 },
  { prop: "driverName", label: "驾驶员", width: 100 },
  { prop: "driverPhone", label: "电话", width: 130 },
  { prop: "speedLimitKmh", label: "限速(km/h)", width: 110 },
  {
    prop: "enabled",
    label: "启用",
    width: 80,
    align: "center",
    render: (row: Vehicle) =>
      h(
        ElTag,
        { type: row.enabled ? "success" : "info", size: "small" },
        () => (row.enabled ? "是" : "否"),
      ),
  },
  { prop: "remark", label: "备注", minWidth: 140, showOverflowTooltip: true },
];

const vehicleOperations: Operation[] = [
  {
    label: "编辑",
    type: "primary",
    handler: (row) => handleVehicleEdit(row as Vehicle),
  },
  {
    label: "删除",
    type: "danger",
    handler: (row) => handleVehicleDelete(row as Vehicle),
  },
];

const vehicleToolbarLeft = () =>
  h(ElButton, { type: "primary", onClick: handleVehicleCreate }, () => [
    h(Plus),
    " 新增车辆",
  ]);

const loadVehicleTableData = async (params: Record<string, unknown>) => {
  try {
    const result = await getVehicles(
      params.page as number,
      params.pageSize as number,
    );
    return { data: result.data, total: result.total };
  } catch {
    ElMessage.error("获取车辆列表失败");
    return { data: [], total: 0 };
  }
};

const loadSmsTemplates = async () => {
  smsLoading.value = true;
  try {
    smsTemplates.value = await getSmsTemplates();
    syncSmsSavedContent();
  } catch {
    ElMessage.error("获取消息模板失败");
  } finally {
    smsLoading.value = false;
  }
};

const saveSmsTemplate = async (item: SmsTemplate) => {
  if (!item.content?.trim()) {
    ElMessage.warning("模板内容不能为空");
    return;
  }
  smsSavingType.value = item.type;
  try {
    const updated = await updateSmsTemplate(item.type, item.content.trim());
    const idx = smsTemplates.value.findIndex((t) => t.type === item.type);
    if (idx >= 0) {
      smsTemplates.value[idx] = updated;
    }
    syncSmsSavedContent();
    ElMessage.success("模板已保存");
  } catch {
    /* 错误已由 axios 拦截器提示 */
  } finally {
    smsSavingType.value = "";
  }
};

const onTabChange = (name: string | number) => {
  if (name === "vehicle") {
    nextTick(() => vehicleTableRef.value?.refresh());
  } else if (name === "sms") {
    loadSmsTemplates();
  }
};

const normalizeVehicleType = (v: string | undefined): VehicleType => {
  const s = (v || "").trim();
  return (VEHICLE_TYPE_OPTIONS as readonly string[]).includes(s)
    ? (s as VehicleType)
    : defaultVehicleType();
};

const handleVehicleCreate = () => {
  vehicleIsEdit.value = false;
  vehicleForm.value = {
    plateNumber: "",
    vehicleType: defaultVehicleType(),
    driverName: "",
    driverPhone: "",
    speedLimitKmh: 80,
    enabled: true,
    remark: "",
  };
  vehicleDialogVisible.value = true;
};

const handleVehicleEdit = (row: Vehicle) => {
  vehicleIsEdit.value = true;
  vehicleForm.value = {
    ...row,
    vehicleType: normalizeVehicleType(row.vehicleType as string),
  };
  vehicleDialogVisible.value = true;
};

const handleVehicleDelete = async (row: Vehicle) => {
  try {
    await ElMessageBox.confirm(
      `确定删除车辆「${row.plateNumber}」吗？`,
      "提示",
      { type: "warning" },
    );
    await deleteVehicle(row.id!);
    ElMessage.success("删除成功");
    vehicleTableRef.value?.refresh();
  } catch (error: unknown) {
    if (error !== "cancel") {
      const msg =
        error && typeof error === "object" && "message" in error
          ? String((error as { message?: string }).message)
          : "删除失败";
      ElMessage.error(msg);
    }
  }
};

const handleVehicleSave = async () => {
  const f = vehicleForm.value;
  if (
    !f.plateNumber?.trim() ||
    !f.vehicleType ||
    !(VEHICLE_TYPE_OPTIONS as readonly string[]).includes(f.vehicleType) ||
    !f.driverName?.trim() ||
    !f.driverPhone?.trim()
  ) {
    ElMessage.warning("请填写车牌并选择车辆类型，以及驾驶员及电话");
    return;
  }
  if (f.speedLimitKmh == null || f.speedLimitKmh < 1 || f.speedLimitKmh > 200) {
    ElMessage.warning("限速需在 1–200 km/h 之间");
    return;
  }
  try {
    if (vehicleIsEdit.value && f.id != null) {
      await updateVehicle(f.id, f);
      ElMessage.success("更新成功");
    } else {
      await createVehicle(f);
      ElMessage.success("创建成功");
    }
    vehicleDialogVisible.value = false;
    vehicleTableRef.value?.refresh();
  } catch {
    /* 错误已由 axios 拦截器提示 */
  }
};

onMounted(() => loadCompanies());
</script>

<style scoped>
.system-settings {
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
  padding: 20px;
}

.settings-tabs :deep(.el-tabs__header) {
  margin-bottom: 20px;
}

.settings-tabs :deep(.el-tabs__item.is-active) {
  color: var(--primary-color);
}

.settings-tabs :deep(.el-tabs__active-bar) {
  background: var(--gradient-primary);
}

.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.company-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}

.company-card {
  background: var(--bg-primary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  overflow: hidden;
  transition: all var(--transition-normal);
}

.company-card:hover {
  border-color: var(--primary-light);
  box-shadow: var(--shadow-md);
}

.company-card.sort-mode {
  cursor: move;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border-color);
}

.sort-handle {
  color: var(--text-tertiary);
  cursor: grab;
  font-size: 18px;
}

.sort-handle:active {
  cursor: grabbing;
}

.company-name {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.company-name .name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

.company-name .code {
  font-size: 12px;
  color: var(--text-tertiary);
}

.card-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-body {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}

.info-icon {
  color: var(--text-tertiary);
  font-size: 14px;
}

.info-label {
  color: var(--text-tertiary);
  min-width: 60px;
}

.info-value {
  color: var(--text-primary);
  flex: 1;
}

.info-value a {
  color: var(--primary-color);
  text-decoration: none;
}

.info-value a:hover {
  text-decoration: underline;
}

:deep(.sortable-ghost) {
  opacity: 0.4;
  background: var(--bg-secondary);
}

:deep(.sortable-drag) {
  box-shadow: var(--shadow-xl);
  transform: scale(1.02);
}

.settings-dialog :deep(.el-dialog__header) {
  border-bottom: 1px solid var(--border-color);
  padding-bottom: 16px;
}

.vehicle-table {
  width: 100%;
  margin-bottom: 16px;
}

.vehicle-pagination {
  display: flex;
  justify-content: flex-end;
}

.speed-input {
  width: 160px;
}

.sms-template-tab {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.sms-layout {
  display: grid;
  grid-template-columns: minmax(280px, 340px) minmax(0, 1fr);
  gap: 20px;
  align-items: start;
}

.sms-sidebar {
  position: sticky;
  top: 12px;
}

.sms-main {
  min-width: 0;
}

@media (max-width: 960px) {
  .sms-layout {
    grid-template-columns: 1fr;
  }

  .sms-sidebar {
    position: static;
  }
}

.sms-intro-alert :deep(.el-alert__description) {
  margin: 0;
  line-height: 1.6;
  font-size: 13px;
}

.sms-intro-alert p {
  margin: 0;
}

.sms-placeholder-card,
.sms-template-card {
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
}

.sms-card-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

.sms-placeholder-intro {
  margin: 0 0 16px;
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.6;
}

.sms-placeholder-grid {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.sms-placeholder-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 10px 12px;
  background: var(--bg-secondary);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
}

.sms-placeholder-key {
  font-size: 12px;
  color: var(--primary-color);
  background: transparent;
}

.sms-placeholder-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
}

.sms-placeholder-desc {
  font-size: 12px;
  color: var(--text-tertiary);
  line-height: 1.5;
}

.sms-template-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.sms-template-card :deep(.el-card__header) {
  padding-bottom: 12px;
}

.sms-template-card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.sms-template-title-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.sms-type-code {
  font-size: 11px;
  color: var(--text-tertiary);
  font-family: ui-monospace, monospace;
}

.sms-update-time {
  font-size: 12px;
  color: var(--text-tertiary);
  white-space: nowrap;
}

.sms-template-desc {
  margin: 0 0 12px;
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.6;
}

.sms-trigger-box {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  padding: 10px 12px;
  background: var(--bg-secondary);
  border-radius: var(--radius-md);
  font-size: 13px;
  line-height: 1.55;
}

.sms-trigger-label {
  flex-shrink: 0;
  font-weight: 600;
  color: var(--text-primary);
}

.sms-trigger-text {
  color: var(--text-secondary);
}

.sms-content-item {
  margin-bottom: 0;
}

.sms-content-item :deep(.el-form-item__label) {
  font-weight: 600;
}

.sms-template-actions {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
