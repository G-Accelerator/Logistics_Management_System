<template>
  <page-container title="系统配置" description="管理系统配置项">
    <el-tabs v-model="activeTab">
      <!-- 快递公司管理 -->
      <el-tab-pane label="快递公司管理" name="express">
        <div style="padding: 20px 0">
          <div style="display: flex; gap: 12px; margin-bottom: 20px">
            <el-button type="primary" @click="handleCreate">
              新增快递公司
            </el-button>
            <template v-if="!sortMode">
              <el-button
                @click="
                  sortMode = true;
                  initSortable();
                "
              >
                调整展示顺序
              </el-button>
            </template>
            <template v-else>
              <el-button type="success" @click="completeSortOrder">
                完成排序
              </el-button>
              <el-button @click="toggleSortMode"> 取消 </el-button>
            </template>
          </div>

          <div
            v-loading="loading"
            class="company-list"
            ref="sortableContainer"
            :key="listKey"
          >
            <el-card
              v-for="company in companies"
              :key="company.id"
              :data-id="company.id"
              class="company-card"
              :class="{ 'sort-mode': sortMode }"
            >
              <template #header>
                <div class="card-header">
                  <span v-if="sortMode" class="sort-handle">⋮⋮</span>
                  <span class="company-name">{{ company.name }}</span>
                  <div class="card-actions">
                    <el-tag :type="company.enabled ? 'success' : 'info'">
                      {{ company.enabled ? "启用" : "禁用" }}
                    </el-tag>
                    <template v-if="!sortMode">
                      <el-button
                        link
                        type="primary"
                        @click="handleEdit(company)"
                      >
                        编辑
                      </el-button>
                      <el-button
                        link
                        type="danger"
                        @click="handleDelete(company)"
                      >
                        删除
                      </el-button>
                    </template>
                  </div>
                </div>
              </template>

              <div class="company-info">
                <div class="info-row">
                  <span class="label">代码：</span>
                  <span class="value">{{ company.code }}</span>
                </div>
                <div class="info-row">
                  <span class="label">运单前缀：</span>
                  <span class="value">{{ company.trackingPrefix }}</span>
                </div>
                <div class="info-row">
                  <span class="label">客服电话：</span>
                  <span class="value">{{ company.phone || "-" }}</span>
                </div>
                <div class="info-row">
                  <span class="label">官网：</span>
                  <span class="value">
                    <a
                      v-if="company.website"
                      :href="company.website"
                      target="_blank"
                    >
                      {{ company.website }}
                    </a>
                    <span v-else>-</span>
                  </span>
                </div>
                <div v-if="!sortMode" class="info-row">
                  <span class="label">排序：</span>
                  <span class="value">{{ company.sortOrder }}</span>
                </div>
              </div>
            </el-card>

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
      width="500px"
      destroy-on-close
    >
      <el-form :model="form" label-width="100px" @submit.prevent="handleSave">
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
  </page-container>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import Sortable from "sortablejs";
import PageContainer from "../../../components/layout/PageContainer/index.vue";
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
const listKey = ref(0); // 用于强制重新渲染列表
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
  } catch (error) {
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
    // 进入排序模式
    await nextTick();
    initSortable();
  } else {
    // 退出排序模式（取消）
    if (sortableInstance) {
      sortableInstance.destroy();
      sortableInstance = null;
    }
    // 恢复原始顺序
    companies.value = JSON.parse(JSON.stringify(originalCompanies.value));
    // 强制重新渲染列表，因为 Sortable.js 直接修改了 DOM 顺序
    listKey.value++;
  }
};

const completeSortOrder = async () => {
  try {
    // 从 DOM 中获取当前的卡片顺序
    if (!sortableContainer.value) return;

    const cards = sortableContainer.value.querySelectorAll(".company-card");
    const newOrder: ExpressCompany[] = [];

    cards.forEach((card, index) => {
      const company = companies.value.find(
        (c) => c.id === parseInt(card.getAttribute("data-id") || "0"),
      );
      if (company) {
        newOrder.push({
          ...company,
          sortOrder: index + 1,
        });
      }
    });

    // 如果没有找到 data-id，则按当前顺序处理
    if (newOrder.length === 0) {
      newOrder.push(
        ...companies.value.map((company, index) => ({
          ...company,
          sortOrder: index + 1,
        })),
      );
    }

    // 批量更新
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

    // 清除缓存，使其他页面能获取最新数据
    await store.refresh();

    ElMessage.success("排序已保存");
  } catch (error: any) {
    ElMessage.error(error?.message || "保存排序失败");
    // 重新加载以恢复原始顺序
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
    // 刷新数据
    await store.refresh();
    loadCompanies();
  } catch (error: any) {
    if (error !== "cancel") {
      ElMessage.error(error?.message || "删除失败");
    }
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
      // 新建时，排序号设为最后一位
      form.value.sortOrder = companies.value.length + 1;
      await createExpressCompany(form.value);
      ElMessage.success("创建成功");
    }
    dialogVisible.value = false;
    // 刷新数据
    await store.refresh();
    loadCompanies();
  } catch (error: any) {
    ElMessage.error(error?.message || "操作失败");
  }
};

onMounted(() => {
  loadCompanies();
});
</script>

<style scoped>
.company-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.company-card {
  transition: all 0.3s ease;
}

.company-card.sort-mode {
  cursor: move;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  user-select: none;
}

.sort-handle {
  cursor: grab;
  color: #909399;
  font-size: 18px;
  margin-right: 8px;
  user-select: none;
  transition: all 0.2s ease;
}

.sort-handle:hover {
  color: #606266;
  transform: scale(1.2);
}

.sort-handle:active {
  cursor: grabbing;
  color: #303133;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.company-card.sort-mode .card-header {
  cursor: grab;
}

.company-card.sort-mode .card-header:active {
  cursor: grabbing;
}

.company-name {
  font-size: 16px;
  font-weight: 500;
}

.card-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.company-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-row {
  display: flex;
  gap: 8px;
  font-size: 13px;
}

.label {
  color: #606266;
  min-width: 70px;
}

.value {
  color: #303133;
  word-break: break-all;
}

.value a {
  color: #409eff;
  text-decoration: none;
}

.value a:hover {
  text-decoration: underline;
}

:deep(.sortable-ghost) {
  opacity: 0.4;
  background-color: #f5f7fa;
  border-radius: 4px;
  transform: scale(0.95);
  user-select: none;
}

:deep(.sortable-drag) {
  opacity: 1;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.25);
  transform: scale(1.02);
  z-index: 1000;
  user-select: none;
}
</style>
