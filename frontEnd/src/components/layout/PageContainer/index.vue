<template>
  <div class="page-container">
    <el-card v-if="showHeader" class="page-header" shadow="never">
      <div class="header-content">
        <div class="header-left">
          <h3 class="page-title">{{ title }}</h3>
          <p v-if="description" class="page-description">{{ description }}</p>
        </div>
        <div v-if="$slots.extra" class="header-right">
          <slot name="extra"></slot>
        </div>
      </div>
    </el-card>

    <el-card class="page-content" :shadow="shadow">
      <slot></slot>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import type { PageContainerProps } from "./types";

export type { PageContainerProps } from "./types";

withDefaults(defineProps<PageContainerProps>(), {
  showHeader: true,
  shadow: "never",
});
</script>

<style scoped>
.page-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-header {
  margin-bottom: 0;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  flex: 1;
}

.page-title {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

.page-description {
  margin: 0;
  font-size: 14px;
  color: #666;
}

.header-right {
  display: flex;
  gap: 10px;
}

.page-content {
  flex: 1;
}
</style>
