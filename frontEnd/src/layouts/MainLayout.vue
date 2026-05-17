<template>
  <el-container class="layout-container">
    <el-aside :width="isCollapse ? '72px' : '240px'" class="sidebar">
      <!-- Logo 区域 -->
      <div class="logo">
        <div class="logo-icon">
          <svg viewBox="0 0 40 40" fill="none">
            <rect
              x="4"
              y="12"
              width="32"
              height="20"
              rx="3"
              stroke="currentColor"
              stroke-width="2"
              fill="rgba(255,255,255,0.1)"
            />
            <circle
              cx="12"
              cy="32"
              r="4"
              stroke="currentColor"
              stroke-width="2"
              fill="none"
            />
            <circle
              cx="28"
              cy="32"
              r="4"
              stroke="currentColor"
              stroke-width="2"
              fill="none"
            />
            <path
              d="M8 12V8C8 6 10 4 12 4H28L34 12"
              stroke="currentColor"
              stroke-width="2"
              fill="none"
            />
            <path
              d="M14 20H26M14 24H22"
              stroke="currentColor"
              stroke-width="1.5"
              stroke-linecap="round"
            />
          </svg>
        </div>
        <transition name="fade">
          <div v-if="!isCollapse" class="logo-text">
            <span class="logo-title">物流系统</span>
            <span class="logo-subtitle">LOGISTICS</span>
          </div>
        </transition>
      </div>

      <!-- 菜单区域 -->
      <el-scrollbar class="sidebar-scrollbar">
        <el-menu
          :key="menuKey"
          :default-active="activeMenu"
          :default-openeds="openedMenus"
          :collapse="isCollapse"
          :unique-opened="false"
          :collapse-transition="false"
          class="sidebar-menu"
          router
        >
          <sidebar-item
            v-for="route in menuRoutes"
            :key="route.path"
            :item="route"
            :base-path="route.path"
            :is-collapse="isCollapse"
          />
        </el-menu>
      </el-scrollbar>

      <!-- 折叠按钮 -->
      <div class="collapse-trigger" @click="toggleCollapse">
        <el-icon :size="18">
          <DArrowLeft v-if="!isCollapse" />
          <DArrowRight v-else />
        </el-icon>
        <span v-if="!isCollapse" class="collapse-text">收起菜单</span>
      </div>
    </el-aside>

    <el-container class="main-container">
      <el-header class="header">
        <div class="header-left">
          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item :to="{ path: '/dashboard' }"
              >首页</el-breadcrumb-item
            >
            <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path">
              {{ item.meta?.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="34" class="user-avatar">
                {{ userStore.userInfo?.nickname?.charAt(0) || "U" }}
              </el-avatar>
              <span class="username">{{ userStore.userInfo?.nickname }}</span>
              <el-icon class="arrow-icon"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <tags-view />

      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <keep-alive :include="cachedViews">
              <component :is="Component" />
            </keep-alive>
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, ref, watch, nextTick } from "vue";
import { useRouter, useRoute } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  ArrowDown,
  User,
  SwitchButton,
  DArrowLeft,
  DArrowRight,
} from "@element-plus/icons-vue";
import { useUserStore } from "../store/user";
import { useTagsViewStore } from "../store/tagsView";
import SidebarItem from "../components/layout/SidebarItem/index.vue";
import TagsView from "../components/layout/TagsView/index.vue";
import { getAccessibleRoutes } from "../router";
import type { AppRouteRecordRaw } from "../types/router";

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();
const tagsViewStore = useTagsViewStore();

const isCollapse = ref(false);
const menuKey = ref(0);

const cachedViews = computed(() => tagsViewStore.cachedViews);

const menuRoutes = computed(() => {
  userStore.userInfo?.role;
  userStore.buyerPhone;
  userStore.sellerPhone;
  return filterMenuRoutes(getAccessibleRoutes());
});

const filterMenuRoutes = (routes: AppRouteRecordRaw[]): AppRouteRecordRaw[] => {
  return routes.filter((route) => {
    if (route.meta?.hidden) return false;
    if (route.children) {
      route.children = filterMenuRoutes(route.children);
    }
    return true;
  });
};

const activeMenu = ref(route.path);
const openedMenus = ref<string[]>([]);

const updateOpenedMenus = () => {
  const { path } = route;
  const parentPath = "/" + path.split("/")[1];
  if (parentPath && parentPath !== "/") {
    if (!openedMenus.value.includes(parentPath)) {
      openedMenus.value = [parentPath];
    }
  }
  nextTick(() => {
    activeMenu.value = path;
    menuKey.value++;
  });
};

watch(() => route.path, updateOpenedMenus, { immediate: true });

const breadcrumbs = computed(() => {
  const matched = route.matched.filter((item) => item.meta?.title);
  return matched.filter((item) => item.path !== "/");
});

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value;
};

const handleCommand = async (command: string) => {
  if (command === "logout") {
    try {
      await ElMessageBox.confirm("确定要退出登录吗？", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      });
      await tagsViewStore.delAllViews();
      await userStore.logout();
      ElMessage.success("已退出登录");
      router.push("/login");
    } catch {}
  } else if (command === "profile") {
    router.push("/profile");
  }
};
</script>

<style scoped>
.layout-container {
  height: 100vh;
  background: var(--bg-tertiary);
}

/* 侧边栏 */
.sidebar {
  background: linear-gradient(180deg, #1e3a5f 0%, #0f2744 100%);
  display: flex;
  flex-direction: column;
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 2px 0 12px rgba(0, 0, 0, 0.15);
  position: relative;
  overflow: visible;
}

/* Logo */
.logo {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  gap: 12px;
  background: rgba(0, 0, 0, 0.15);
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.logo-icon {
  width: 40px;
  height: 40px;
  color: #38bdf8;
  flex-shrink: 0;
}

.logo-icon svg {
  width: 100%;
  height: 100%;
}

.logo-text {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
  overflow: hidden;
}

.logo-title {
  font-size: 18px;
  font-weight: 600;
  color: #ffffff;
  letter-spacing: 2px;
  white-space: nowrap;
}

.logo-subtitle {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.4);
  text-transform: uppercase;
  letter-spacing: 3px;
  white-space: nowrap;
}

/* 菜单滚动区 */
.sidebar-scrollbar {
  flex: 1;
  overflow: hidden;
}

/* 菜单 */
.sidebar-menu {
  border-right: none;
  background: transparent;
  padding: 8px 0;
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 240px;
}

.sidebar-menu.el-menu--collapse {
  width: 72px;
}

/* 折叠按钮 */
.collapse-trigger {
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  color: rgba(255, 255, 255, 0.5);
  background: rgba(0, 0, 0, 0.2);
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  transition: all 0.3s ease;
}

.collapse-trigger:hover {
  color: #ffffff;
  background: rgba(255, 255, 255, 0.1);
}

.collapse-text {
  font-size: 13px;
  white-space: nowrap;
}

/* 主容器 */
.main-container {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 顶部栏 */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: var(--bg-primary);
  border-bottom: 1px solid var(--border-color);
  padding: 0 24px;
  height: 56px;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  flex: 1;
}

.breadcrumb {
  flex: 1;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: var(--radius-full);
  transition: all 0.3s ease;
}

.user-info:hover {
  background: var(--bg-secondary);
}

.user-avatar {
  background: var(--gradient-primary);
  color: #ffffff;
  font-weight: 600;
  font-size: 14px;
}

.username {
  font-size: 14px;
  color: var(--text-primary);
  font-weight: 500;
}

.arrow-icon {
  font-size: 12px;
  color: var(--text-tertiary);
}

/* 主内容区 */
.main-content {
  background: var(--bg-tertiary);
  padding: 16px 20px;
  flex: 1;
  overflow-y: auto;
}

/* 动画 */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.25s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(8px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
