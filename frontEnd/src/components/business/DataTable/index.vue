<template>
  <div class="data-table">
    <!-- 搜索区域 -->
    <el-card v-if="searchConfig.length > 0" class="search-card">
      <el-form :model="searchForm" class="search-form">
        <div class="search-form-grid">
          <el-form-item
            v-for="(item, index) in searchConfig"
            :key="getSearchItemKey(item, index)"
            :label="item.label"
            class="search-form-item"
          >
            <!-- 多组件模式 -->
            <template v-if="item.components && item.components.length > 0">
              <div class="search-components-group">
                <template v-for="comp in item.components" :key="comp.prop">
                  <!-- 自定义渲染函数 -->
                  <component v-if="comp.render" :is="comp.render(searchForm)" />

                  <!-- 输入框 -->
                  <el-input
                    v-else-if="!comp.type || comp.type === 'input'"
                    v-model="searchForm[comp.prop]"
                    :placeholder="comp.placeholder || `请输入`"
                    clearable
                    :style="comp.style || { width: '150px' }"
                  />

                  <!-- 下拉选择 -->
                  <el-select
                    v-else-if="comp.type === 'select'"
                    v-model="searchForm[comp.prop]"
                    :placeholder="comp.placeholder || `请选择`"
                    clearable
                    :style="comp.style || { width: '150px' }"
                  >
                    <el-option
                      v-for="option in comp.options"
                      :key="option.value"
                      :label="option.label"
                      :value="option.value"
                    />
                  </el-select>

                  <!-- 日期选择 -->
                  <el-date-picker
                    v-else-if="comp.type === 'date'"
                    v-model="searchForm[comp.prop]"
                    type="date"
                    :placeholder="comp.placeholder || `请选择日期`"
                    clearable
                    :style="comp.style || { width: '150px' }"
                  />

                  <!-- 日期范围 -->
                  <el-date-picker
                    v-else-if="comp.type === 'daterange'"
                    v-model="searchForm[comp.prop]"
                    type="daterange"
                    range-separator="至"
                    start-placeholder="开始日期"
                    end-placeholder="结束日期"
                    clearable
                    :style="comp.style || { width: '240px' }"
                  />
                </template>
              </div>
            </template>

            <!-- 单组件模式 -->
            <template v-else>
              <!-- 自定义渲染函数 -->
              <component v-if="item.render" :is="item.render(searchForm)" />

              <!-- 输入框（默认） -->
              <el-input
                v-else-if="!item.type || item.type === 'input'"
                v-model="searchForm[item.prop as string]"
                :placeholder="item.placeholder || `请输入${item.label}`"
                clearable
                style="width: 100%"
              />

              <!-- 下拉选择 -->
              <el-select
                v-else-if="item.type === 'select'"
                v-model="searchForm[item.prop as string]"
                :placeholder="item.placeholder || `请选择${item.label}`"
                clearable
                style="width: 100%"
              >
                <el-option
                  v-for="option in item.options"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>

              <!-- 日期选择 -->
              <el-date-picker
                v-else-if="item.type === 'date'"
                v-model="searchForm[item.prop as string]"
                type="date"
                :placeholder="item.placeholder || `请选择${item.label}`"
                clearable
                style="width: 100%"
              />

              <!-- 日期范围 -->
              <el-date-picker
                v-else-if="item.type === 'daterange'"
                v-model="searchForm[item.prop as string]"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                clearable
                style="width: 100%"
              />
            </template>
          </el-form-item>

          <!-- 搜索按钮区域 -->
          <div class="search-form-actions">
            <el-button type="primary" :icon="Search" @click="handleSearch">
              搜索
            </el-button>
            <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          </div>
        </div>
      </el-form>
    </el-card>

    <!-- 表格区域 -->
    <el-card class="table-card">
      <!-- 工具栏 -->
      <div v-if="showToolbar" class="toolbar">
        <div class="toolbar-left">
          <component v-if="toolbarLeft" :is="toolbarLeft()" />
        </div>
        <div class="toolbar-right">
          <component v-if="toolbarRight" :is="toolbarRight()" />
        </div>
      </div>

      <!-- 表格 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        :border="border"
        :stripe="stripe"
        :height="height"
        :max-height="maxHeight"
        @selection-change="handleSelectionChange"
      >
        <!-- 多选列 -->
        <el-table-column
          v-if="showSelection"
          type="selection"
          width="55"
          align="center"
        />

        <!-- 序号列 -->
        <el-table-column
          v-if="showIndex"
          type="index"
          label="序号"
          width="60"
          align="center"
        />

        <!-- 数据列 -->
        <el-table-column
          v-for="column in columns"
          :key="column.prop"
          :prop="column.prop"
          :label="column.label"
          :width="column.width"
          :min-width="column.minWidth"
          :align="column.align || 'left'"
          :fixed="column.fixed"
          :sortable="column.sortable"
          :show-overflow-tooltip="column.showOverflowTooltip"
        >
          <template #default="scope">
            <!-- 自定义渲染函数 -->
            <component
              v-if="column.render"
              :is="column.render(scope.row, column, scope.$index)"
            />

            <!-- 格式化函数 -->
            <span v-else-if="column.formatter">
              {{
                column.formatter(scope.row, column) || column.emptyText || "-"
              }}
            </span>

            <!-- 默认显示 -->
            <span v-else>
              {{ scope.row[column.prop] ?? column.emptyText ?? "-" }}
            </span>
          </template>
        </el-table-column>

        <!-- 操作列 -->
        <el-table-column
          v-if="showOperation"
          label="操作"
          :width="operationWidth"
          :fixed="operationFixed"
          align="center"
        >
          <template #default="scope">
            <slot name="operation" :row="scope.row" :$index="scope.$index">
              <el-button
                v-for="btn in operations"
                :key="btn.label"
                :type="btn.type || 'primary'"
                link
                @click="btn.handler(scope.row, scope.$index)"
              >
                {{ btn.label }}
              </el-button>
            </slot>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div v-if="showPagination" class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="pageSizes"
          :total="pagination.total"
          :background="true"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";
import { Search, Refresh } from "@element-plus/icons-vue";
import type { Component, VNode } from "vue";
import type { SearchItem, Column, Operation } from "./types";

export type { SearchItem, Column, Operation } from "./types";

interface Props {
  searchConfig?: SearchItem[];
  searchCols?:
    | number
    | { xs?: number; sm?: number; md?: number; lg?: number; xl?: number };
  columns: Column[];
  loadData: (params: any) => Promise<{ data: any[]; total: number }>;
  border?: boolean;
  stripe?: boolean;
  height?: string | number;
  maxHeight?: string | number;
  showSelection?: boolean;
  showIndex?: boolean;
  showToolbar?: boolean;
  toolbarLeft?: () => VNode | Component | any;
  toolbarRight?: () => VNode | Component | any;
  showOperation?: boolean;
  operationWidth?: string | number;
  operationFixed?: boolean | "left" | "right";
  operations?: Operation[];
  showPagination?: boolean;
  pageSizes?: number[];
  initialPage?: number;
  initialPageSize?: number;
}

const props = withDefaults(defineProps<Props>(), {
  searchConfig: () => [],
  searchCols: 4,
  border: true,
  stripe: true,
  showSelection: false,
  showIndex: true,
  showToolbar: false,
  showOperation: true,
  operationWidth: 200,
  operationFixed: "right",
  operations: () => [],
  showPagination: true,
  pageSizes: () => [10, 20, 50, 100],
  initialPage: 1,
  initialPageSize: 10,
});

const emit = defineEmits<{
  selectionChange: [selection: any[]];
}>();

const searchForm = reactive<Record<string, any>>({});

const initSearchForm = () => {
  props.searchConfig.forEach((item) => {
    if (item.components && item.components.length > 0) {
      item.components.forEach((comp) => {
        searchForm[comp.prop] = comp.render ? null : "";
      });
    } else if (item.render || Array.isArray(item.prop)) {
      const props = Array.isArray(item.prop) ? item.prop : [item.prop];
      props.forEach((p) => {
        searchForm[p] = null;
      });
    } else {
      searchForm[item.prop as string] = "";
    }
  });
};

initSearchForm();

const getSearchItemKey = (item: SearchItem, index: number): string => {
  if (item.components) {
    return `search-item-${index}`;
  }
  return Array.isArray(item.prop) ? item.prop.join("-") : item.prop;
};

const tableData = ref<any[]>([]);
const loading = ref(false);

const pagination = reactive({
  page: props.initialPage,
  pageSize: props.initialPageSize,
  total: 0,
});

const loadTableData = async () => {
  loading.value = true;
  try {
    const params = {
      ...searchForm,
      page: pagination.page,
      pageSize: pagination.pageSize,
    };
    const result = await props.loadData(params);
    tableData.value = result.data;
    pagination.total = result.total;
  } catch (error) {
    console.error("加载数据失败:", error);
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  pagination.page = 1;
  loadTableData();
};

const handleReset = () => {
  initSearchForm();
  pagination.page = 1;
  loadTableData();
};

const handleSizeChange = (size: number) => {
  pagination.pageSize = size;
  pagination.page = 1;
  loadTableData();
};

const handleCurrentChange = (page: number) => {
  pagination.page = page;
  loadTableData();
};

const handleSelectionChange = (selection: any[]) => {
  emit("selectionChange", selection);
};

const refresh = () => {
  loadTableData();
};

const resetAndRefresh = () => {
  handleReset();
};

defineExpose({
  refresh,
  resetAndRefresh,
  searchForm,
  pagination,
});

onMounted(() => {
  loadTableData();
});
</script>

<style scoped>
.data-table {
  width: 100%;
}

.search-card {
  margin-bottom: 20px;
}

.search-form {
  margin: 0;
}

.search-form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px 20px;
  align-items: start;
}

.search-form-item {
  margin-bottom: 0;
}

.search-form-item :deep(.el-form-item__label) {
  white-space: nowrap;
  min-width: 80px;
}

.search-form-item :deep(.el-form-item__content) {
  flex: 1;
}

.search-form-actions {
  display: flex;
  gap: 10px;
  align-items: flex-start;
  padding-top: 32px;
}

@media (min-width: 1400px) {
  .search-form-grid {
    grid-template-columns: repeat(4, 1fr);
  }
  .search-form-actions {
    grid-column: -1;
    justify-self: end;
  }
}

@media (min-width: 1200px) and (max-width: 1399px) {
  .search-form-grid {
    grid-template-columns: repeat(3, 1fr);
  }
  .search-form-actions {
    grid-column: -1;
    justify-self: end;
  }
}

@media (min-width: 768px) and (max-width: 1199px) {
  .search-form-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .search-form-actions {
    grid-column: -1;
    justify-self: end;
  }
}

@media (max-width: 767px) {
  .search-form-grid {
    grid-template-columns: 1fr;
  }
  .search-form-actions {
    grid-column: 1;
    justify-self: stretch;
    justify-content: flex-end;
    padding-top: 0;
    margin-top: 8px;
  }
}

.table-card {
  background: var(--card-bg);
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.toolbar-left,
.toolbar-right {
  display: flex;
  gap: 10px;
}

.search-components-group {
  display: flex;
  gap: 8px;
  align-items: center;
}

.search-components-group > * {
  flex-shrink: 0;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
