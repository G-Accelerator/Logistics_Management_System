<template>
  <div v-if="!item.meta?.hidden">
    <!-- 只有一个子菜单且不强制显示父级 -->
    <template
      v-if="
        hasOneShowingChild(item.children, item) &&
        (!onlyOneChild.children || onlyOneChild.noShowingChildren) &&
        !item.meta?.alwaysShow
      "
    >
      <el-menu-item :index="resolvePath(onlyOneChild.path)">
        <el-icon v-if="onlyOneChild.meta?.icon">
          <component :is="onlyOneChild.meta.icon" />
        </el-icon>
        <template #title>
          <span>{{ onlyOneChild.meta?.title }}</span>
        </template>
      </el-menu-item>
    </template>

    <!-- 有多个子菜单 -->
    <el-sub-menu v-else :index="resolvePath(item.path)">
      <template #title>
        <el-icon v-if="item.meta?.icon">
          <component :is="item.meta.icon" />
        </el-icon>
        <span>{{ item.meta?.title }}</span>
      </template>

      <sidebar-item
        v-for="child in item.children"
        :key="child.path"
        :item="child"
        :base-path="resolvePath(child.path)"
        :is-collapse="isCollapse"
      />
    </el-sub-menu>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import type { AppRouteRecordRaw, SidebarItemProps } from "./types";

export type { SidebarItemProps, AppRouteRecordRaw } from "./types";

defineOptions({
  name: "SidebarItem",
});

const props = withDefaults(defineProps<SidebarItemProps>(), {
  isCollapse: false,
});

const onlyOneChild = ref<AppRouteRecordRaw>({} as AppRouteRecordRaw);

const hasOneShowingChild = (
  children: AppRouteRecordRaw[] = [],
  parent: AppRouteRecordRaw,
) => {
  const showingChildren = children.filter((item) => {
    if (item.meta?.hidden) {
      return false;
    } else {
      onlyOneChild.value = item;
      return true;
    }
  });

  if (showingChildren.length === 1) {
    return true;
  }

  if (showingChildren.length === 0) {
    onlyOneChild.value = {
      ...parent,
      path: "",
      noShowingChildren: true,
    } as any;
    return true;
  }

  return false;
};

const resolvePath = (routePath: string) => {
  if (routePath.startsWith("/")) {
    return routePath;
  }
  return `${props.basePath}/${routePath}`
    .replace(/\/+/g, "/")
    .replace(/\/$/, "");
};
</script>
