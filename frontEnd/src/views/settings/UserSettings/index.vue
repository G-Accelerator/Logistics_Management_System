<template>
  <page-container title="用户管理" description="管理系统用户">
    <div class="empty-state">
      <el-icon class="empty-icon"><User /></el-icon>
      <p class="empty-text">页面开发中...</p>
    </div>
  </page-container>
</template>

<script setup lang="ts">
import { User } from "@element-plus/icons-vue";
import PageContainer from "../../../components/layout/PageContainer/index.vue";
</script>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  color: var(--text-tertiary);
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
  color: var(--border-color);
}

.empty-text {
  margin: 0;
  font-size: 14px;
}
</style>
