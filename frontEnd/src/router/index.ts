import { createRouter, createWebHistory } from "vue-router";
import type { RouteRecordRaw } from "vue-router";
import { constantRoutes, asyncRoutes, buyerRoutes } from "./routes";

export { constantRoutes, asyncRoutes, buyerRoutes };
export type { AppRouteRecordRaw, RouteMeta } from "./routes";

// 根据角色获取路由
const getRoutesByRole = (role: string | null): RouteRecordRaw[] => {
  if (role === "buyer") {
    return [...constantRoutes, ...buyerRoutes] as RouteRecordRaw[];
  }
  return [...constantRoutes, ...asyncRoutes] as RouteRecordRaw[];
};

const router = createRouter({
  history: createWebHistory(),
  routes: [...constantRoutes, ...asyncRoutes] as RouteRecordRaw[],
});

// 是否已添加动态路由
let hasAddedRoutes = false;

// 重置路由
export const resetRouter = () => {
  const newRouter = createRouter({
    history: createWebHistory(),
    routes: constantRoutes as RouteRecordRaw[],
  });
  (router as any).matcher = (newRouter as any).matcher;
  hasAddedRoutes = false;
};

// 根据角色添加路由
export const addRoutesByRole = (role: string) => {
  if (hasAddedRoutes) return;

  const routes = role === "buyer" ? buyerRoutes : asyncRoutes;
  routes.forEach((route) => {
    router.addRoute(route as RouteRecordRaw);
  });
  hasAddedRoutes = true;
};

// 路由守卫
router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem("token");
  const buyerPhone = localStorage.getItem("buyerPhone");

  // 白名单路由，无需登录
  const whiteList = ["/login", "/recruit"];
  if (whiteList.includes(to.path)) {
    next();
    return;
  }

  if (!token) {
    next("/login");
    return;
  }

  // 买家角色限制访问
  if (buyerPhone) {
    const buyerAllowedPaths = [
      "/buyer/orders",
      "/transport/track",
      "/dashboard",
    ];
    const isAllowed = buyerAllowedPaths.some(
      (path) => to.path === path || to.path.startsWith(path),
    );
    if (!isAllowed) {
      next("/buyer/orders");
      return;
    }
  }

  next();
});

export default router;
