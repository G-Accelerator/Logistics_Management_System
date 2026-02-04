import { createRouter, createWebHistory } from "vue-router";
import type { RouteRecordRaw } from "vue-router";
import {
  constantRoutes,
  asyncRoutes,
  buyerRoutes,
  sellerRoutes,
} from "./routes";

export { constantRoutes, asyncRoutes, buyerRoutes, sellerRoutes };
export type { AppRouteRecordRaw, RouteMeta } from "./routes";

const router = createRouter({
  history: createWebHistory(),
  routes: [
    ...constantRoutes,
    ...asyncRoutes,
    ...buyerRoutes,
    ...sellerRoutes,
  ] as RouteRecordRaw[],
});

// 路由守卫
router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem("token");
  const buyerPhone = localStorage.getItem("buyerPhone");
  const sellerPhone = localStorage.getItem("sellerPhone");

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

  // 卖家角色限制访问
  if (sellerPhone) {
    const sellerAllowedPaths = [
      "/seller/shipment",
      "/seller/orders",
      "/transport/track",
      "/dashboard",
    ];
    const isAllowed = sellerAllowedPaths.some(
      (path) => to.path === path || to.path.startsWith(path),
    );
    if (!isAllowed) {
      next("/seller/shipment");
      return;
    }
  }

  next();
});

export default router;
