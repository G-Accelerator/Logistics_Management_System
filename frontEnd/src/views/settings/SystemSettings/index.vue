<template>
  <div class="system-settings">
    <el-tabs v-model="activeTab" class="settings-tabs">
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
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
import { useExpressCompanyStore } from "../../../store/expressCompany";

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
</style>
