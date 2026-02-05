<template>
  <el-dialog
    v-model="visible"
    :title="title"
    :width="width"
    destroy-on-close
    @closed="handleClosed"
  >
    <!-- 上传区域 -->
    <div class="import-upload-section">
      <el-upload
        ref="uploadRef"
        class="import-upload"
        drag
        :auto-upload="false"
        :limit="1"
        :accept="acceptTypes"
        :on-change="handleFileChange"
        :on-exceed="handleExceed"
        :before-upload="beforeUpload"
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip">
            仅支持 .xlsx 格式，文件大小不超过 10MB
          </div>
        </template>
      </el-upload>

      <div class="template-download">
        <el-button type="primary" link @click="handleDownloadTemplate">
          <el-icon><download /></el-icon>
          下载导入模板
        </el-button>
      </div>
    </div>

    <!-- 导入结果展示 -->
    <div v-if="importResult" class="import-result-section">
      <el-divider content-position="left">导入结果</el-divider>

      <!-- 统计信息 -->
      <div class="result-stats">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-statistic title="总记录数" :value="importResult.total" />
          </el-col>
          <el-col :span="8">
            <el-statistic
              title="成功"
              :value="importResult.success"
              :value-style="{ color: '#67c23a' }"
            />
          </el-col>
          <el-col :span="8">
            <el-statistic
              title="失败"
              :value="importResult.failed"
              :value-style="{
                color: importResult.failed > 0 ? '#f56c6c' : '#909399',
              }"
            />
          </el-col>
        </el-row>
      </div>

      <!-- 失败记录列表 -->
      <div v-if="importResult.errors.length > 0" class="error-list">
        <div class="error-list-header">
          <span>失败记录详情</span>
          <el-button type="primary" size="small" @click="handleDownloadErrors">
            <el-icon><download /></el-icon>
            下载失败记录
          </el-button>
        </div>
        <el-table :data="importResult.errors" max-height="200" size="small">
          <el-table-column prop="row" label="行号" width="80" />
          <el-table-column prop="field" label="字段" width="120" />
          <el-table-column
            prop="value"
            label="原始值"
            width="150"
            show-overflow-tooltip
          />
          <el-table-column
            prop="message"
            label="错误原因"
            show-overflow-tooltip
          />
        </el-table>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="visible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="uploading"
          :disabled="!selectedFile"
          @click="handleImport"
        >
          开始导入
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";
import { ElMessage } from "element-plus";
import { UploadFilled, Download } from "@element-plus/icons-vue";
import type { UploadInstance, UploadFile, UploadRawFile } from "element-plus";
import type { ImportResult } from "./types";
import {
  downloadTemplate,
  importOrders,
  downloadErrors,
} from "../../../api/order";
import {
  downloadImportTemplate,
  downloadImportErrors,
} from "../../../utils/file";

export type {
  ImportDialogProps,
  ImportDialogEmits,
  ImportResult,
  ImportError,
} from "./types";

interface Props {
  modelValue: boolean;
  title?: string;
  width?: string;
}

const props = withDefaults(defineProps<Props>(), {
  title: "导入订单",
  width: "600px",
});

const emit = defineEmits<{
  "update:modelValue": [value: boolean];
  success: [result: ImportResult];
}>();

// 响应式状态
const visible = ref(props.modelValue);
const uploadRef = ref<UploadInstance>();
const selectedFile = ref<File | null>(null);
const uploading = ref(false);
const importResult = ref<ImportResult | null>(null);

// 常量
const acceptTypes = ".xlsx";
const maxFileSize = 10 * 1024 * 1024; // 10MB

// 监听 props 变化
watch(
  () => props.modelValue,
  (val) => {
    visible.value = val;
  },
);

watch(visible, (val) => {
  emit("update:modelValue", val);
});

// 文件选择处理
const handleFileChange = (file: UploadFile) => {
  if (file.raw) {
    selectedFile.value = file.raw;
    // 清除之前的导入结果
    importResult.value = null;
  }
};

// 文件数量超限处理
const handleExceed = () => {
  ElMessage.warning("只能上传一个文件，请先移除已选文件");
};

// 上传前验证
const beforeUpload = (file: UploadRawFile): boolean => {
  const isXlsx = file.name.endsWith(".xlsx");
  if (!isXlsx) {
    ElMessage.error("请上传 .xlsx 格式的文件");
    return false;
  }

  const isLt10M = file.size <= maxFileSize;
  if (!isLt10M) {
    ElMessage.error("文件大小不能超过 10MB");
    return false;
  }

  return true;
};

// 下载模板
const handleDownloadTemplate = async () => {
  try {
    const blob = await downloadTemplate();
    downloadImportTemplate(blob);
    ElMessage.success("模板下载成功");
  } catch {
    ElMessage.error("模板下载失败，请重试");
  }
};

// 执行导入
const handleImport = async () => {
  if (!selectedFile.value) {
    ElMessage.warning("请先选择文件");
    return;
  }

  uploading.value = true;
  try {
    const result = await importOrders(selectedFile.value);
    importResult.value = result;

    if (result.success > 0) {
      ElMessage.success(`成功导入 ${result.success} 条订单`);
      emit("success", result);
    }

    if (result.failed > 0) {
      ElMessage.warning(`${result.failed} 条记录导入失败，请查看详情`);
    }
  } catch {
    ElMessage.error("导入失败，请检查文件格式");
  } finally {
    uploading.value = false;
  }
};

// 下载失败记录
const handleDownloadErrors = async () => {
  if (!importResult.value?.errors.length) return;

  try {
    const blob = await downloadErrors(importResult.value.errors);
    downloadImportErrors(blob);
    ElMessage.success("失败记录下载成功");
  } catch {
    ElMessage.error("下载失败，请重试");
  }
};

// 对话框关闭时重置状态
const handleClosed = () => {
  selectedFile.value = null;
  importResult.value = null;
  uploadRef.value?.clearFiles();
};
</script>

<style scoped>
.import-upload-section {
  margin-bottom: 16px;
}

.import-upload {
  width: 100%;
}

.import-upload :deep(.el-upload-dragger) {
  width: 100%;
  padding: 30px 20px;
}

.template-download {
  margin-top: 12px;
  text-align: center;
}

.import-result-section {
  margin-top: 16px;
}

.result-stats {
  margin-bottom: 20px;
}

.error-list {
  margin-top: 16px;
}

.error-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  font-weight: 500;
  color: #303133;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
