<template>
  <el-container class="layout-container">
    <el-aside :width="isCollapse ? '64px' : '200px'" class="sidebar">
      <div class="logo">
        <h3 v-if="!isCollapse">物流管理</h3>
        <h3 v-else>物流</h3>
      </div>

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
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-icon" @click="toggleCollapse">
            <Expand v-if="isCollapse" />
            <Fold v-else />
          </el-icon>

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
              <el-avatar :size="32" src="" />
              <span v-if="!isCollapse" class="username">{{
                userStore.userInfo?.nickname
              }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided
                  >退出登录</el-dropdown-item
                >
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 标签页 -->
      <tags-view />

      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
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
import { Expand, Fold } from "@element-plus/icons-vue";
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

// 缓存的视图
const cachedViews = computed(() => tagsViewStore.cachedViews);

// 获取所有菜单路由（根据角色过滤）
const menuRoutes = computed(() => {
  // 触发响应式更新
  userStore.userInfo?.role;
  userStore.buyerPhone;
  userStore.sellerPhone;

  return filterMenuRoutes(getAccessibleRoutes());
});

// 过滤菜单路由
const filterMenuRoutes = (routes: AppRouteRecordRaw[]): AppRouteRecordRaw[] => {
  return routes.filter((route) => {
    if (route.meta?.hidden) {
      return false;
    }
    if (route.children) {
      route.children = filterMenuRoutes(route.children);
    }
    return true;
  });
};

// 当前激活的菜单
const activeMenu = ref(route.path);

// 默认展开的菜单
const openedMenus = ref<string[]>([]);

// 根据当前路由自动展开父级菜单并更新激活状态
const updateOpenedMenus = () => {
  const { path } = route;
  const parentPath = "/" + path.split("/")[1];

  // 先更新展开的菜单
  if (parentPath && parentPath !== "/") {
    if (!openedMenus.value.includes(parentPath)) {
      openedMenus.value = [parentPath];
    }
  }

  // 更新激活菜单并强制重新渲染
  nextTick(() => {
    activeMenu.value = path;
    menuKey.value++;
  });
};

// 监听路由变化，自动展开对应菜单
watch(() => route.path, updateOpenedMenus, { immediate: true });

// 面包屑导航
const breadcrumbs = computed(() => {
  const matched = route.matched.filter((item) => item.meta?.title);
  return matched.filter((item) => item.path !== "/");
});

// 切换侧边栏折叠
const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value;
};

// 下拉菜单命令处理
const handleCommand = async (command: string) => {
  if (command === "logout") {
    try {
      await ElMessageBox.confirm("确定要退出登录吗？", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      });
      // 清除所有标签页
      await tagsViewStore.delAllViews();
      // 退出登录
      await userStore.logout();
      ElMessage.success("已退出登录");
      router.push("/login");
    } catch {
      // 取消退出
    }
  } else if (command === "profile") {
    ElMessage.info("个人中心功能开发中");
  }
};
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.sidebar {
  background-color: var(--sidebar-bg);
  color: white;
  transition: width 0.3s;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--sidebar-logo-bg);
  transition: all 0.3s;
}

.logo h3 {
  margin: 0;
  color: white;
  font-size: 18px;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
}

.sidebar-scrollbar {
  height: calc(100vh - 60px);
}

.sidebar-menu {
  border-right: none;
  background-color: var(--sidebar-bg);
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 200px;
}

/* 折叠状态样式 */
.sidebar-menu.el-menu--collapse {
  width: 64px;
}

.sidebar-menu.el-menu--collapse :deep(.el-sub-menu__title span),
.sidebar-menu.el-menu--collapse :deep(.el-menu-item span) {
  display: none;
}

.sidebar-menu.el-menu--collapse :deep(.el-sub-menu__icon-arrow) {
  display: none;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: var(--header-bg);
  border-bottom: 1px solid var(--header-border);
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
  flex: 1;
}

.collapse-icon {
  font-size: 20px;
  cursor: pointer;
  transition: all 0.3s;
  color: var(--text-secondary);
}

.collapse-icon:hover {
  color: var(--primary-color);
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
  transition: all 0.3s;
}

.user-info:hover {
  opacity: 0.8;
}

.username {
  font-size: 14px;
  color: var(--text-primary);
}

.main-content {
  background-color: var(--content-bg);
  padding: 20px;
  height: calc(100vh - 60px - 40px);
  overflow-y: auto;
}

/* 页面切换动画 */
.fade-transform-leave-active,
.fade-transform-enter-active {
  transition: all 0.3s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style>
