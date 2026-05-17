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

/** 登录后各角色均可访问的公共页（不挂在带 redirect 的菜单树下） */
const AUTH_COMMON_PATHS = ["/dashboard", "/profile"];

function normalizeRoutePath(routePath: string, parentPath = ""): string {
  if (routePath.startsWith("/")) return routePath;
  const base = parentPath.endsWith("/") ? parentPath.slice(0, -1) : parentPath;
  return `${base}/${routePath}`.replace(/\/+/g, "/");
}

function pathMatches(routePath: string, targetPath: string, parentPath = ""): boolean {
  return normalizeRoutePath(routePath, parentPath) === targetPath;
}

function isConstantPath(path: string): boolean {
  return constantRoutes.some((route) => {
    if (pathMatches(route.path, path)) return true;
    if (
      route.children?.some((child) =>
        pathMatches(String(child.path), path, route.path),
      )
    ) {
      return true;
    }
    return false;
  });
}

// 检查路由是否可访问
function canAccessRoute(path: string, role: UserRole): boolean {
  if (AUTH_COMMON_PATHS.includes(path)) {
    return true;
  }

  if (isConstantPath(path)) {
    return true;
  }

  const accessibleRoutes = filterRoutesByRole(asyncRoutes, role);

  const checkRoutes = (routes: typeof accessibleRoutes): boolean => {
    for (const route of routes) {
      if (pathMatches(route.path, path) || path.startsWith(route.path + "/")) {
        return true;
      }
      if (route.children && checkRoutes(route.children)) {
        return true;
      }
    }
    return false;
  };

  return checkRoutes(accessibleRoutes);
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
