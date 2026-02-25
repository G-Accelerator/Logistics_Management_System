<template>
  <div class="page-container">
    <div v-if="showHeader" class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h3 class="page-title">
            <slot name="icon">
              <el-icon v-if="icon" class="title-icon"
                ><component :is="icon"
              /></el-icon>
            </slot>
            {{ title }}
          </h3>
          <p v-if="description" class="page-description">{{ description }}</p>
        </div>
        <div v-if="$slots.extra" class="header-right">
          <slot name="extra"></slot>
        </div>
      </div>
    </div>

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
  gap: 16px;
}

.page-header {
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
  padding: 16px 20px;
  border: 1px solid var(--border-color);
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
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-icon {
  color: var(--primary-color);
  font-size: 20px;
}

.page-description {
  margin: 6px 0 0 0;
  font-size: 13px;
  color: var(--text-tertiary);
}

.header-right {
  display: flex;
  gap: 10px;
}

.page-content {
  flex: 1;
  border-radius: var(--radius-lg);
}

.page-content :deep(.el-card__body) {
  padding: 20px;
}
</style>
