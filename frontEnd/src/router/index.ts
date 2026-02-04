import { createRouter, createWebHistory } from "vue-router";
import type { RouteRecordRaw } from "vue-router";
import { constantRoutes, asyncRoutes, filterRoutesByRole } from "./routes";
import type { UserRole } from "./routes";

export { constantRoutes, asyncRoutes, filterRoutesByRole };
export type { AppRouteRecordRaw, RouteMeta, UserRole } from "./routes";

// 获取当前用户角色
function getCurrentRole(): UserRole {
  const buyerPhone = localStorage.getItem("buyerPhone");
  const sellerPhone = localStorage.getItem("sellerPhone");
  if (buyerPhone) return "buyer";
  if (sellerPhone) return "seller";
  return "admin";
}

// 创建路由实例
const router = createRouter({
  history: createWebHistory(),
  routes: [...constantRoutes, ...asyncRoutes] as RouteRecordRaw[],
});

// 获取当前角色可访问的路由（用于菜单渲染）
export function getAccessibleRoutes() {
  const role = getCurrentRole();
  return [...constantRoutes, ...filterRoutesByRole(asyncRoutes, role)];
}

// 检查路由是否可访问
function canAccessRoute(path: string, role: UserRole): boolean {
  const accessibleRoutes = filterRoutesByRole(asyncRoutes, role);

  // 检查路径是否在可访问路由中
  const checkRoutes = (routes: typeof accessibleRoutes): boolean => {
    for (const route of routes) {
      if (route.path === path || path.startsWith(route.path + "/")) {
        return true;
      }
      if (route.children && checkRoutes(route.children)) {
        return true;
      }
    }
    return false;
  };

  // 常量路由始终可访问
  const isConstantRoute = constantRoutes.some(
    (r) =>
      r.path === path ||
      path.startsWith(r.path + "/") ||
      r.children?.some((c) => c.path === path),
  );

  return isConstantRoute || checkRoutes(accessibleRoutes);
}

// 获取角色默认首页
function getRoleDefaultPage(role: UserRole): string {
  switch (role) {
    case "buyer":
      return "/buyer/orders";
    case "seller":
      return "/seller/shipment";
    default:
      return "/dashboard";
  }
}

// 路由守卫
router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem("token");

  // 白名单路由，无需登录
  const whiteList = ["/login", "/recruit"];
  if (whiteList.includes(to.path)) {
    next();
    return;
  }

  // 未登录跳转登录页
  if (!token) {
    next("/login");
    return;
  }

  // 获取当前角色
  const role = getCurrentRole();

  // 检查路由访问权限
  if (!canAccessRoute(to.path, role)) {
    // 无权限，跳转到角色默认页
    next(getRoleDefaultPage(role));
    return;
  }

  next();
});

export default router;
