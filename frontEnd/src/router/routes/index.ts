import type { AppRouteRecordRaw } from "./types";

export type { AppRouteRecordRaw, RouteMeta } from "./types";

// 角色类型
export type UserRole = "admin" | "buyer" | "seller";

// 公共路由（无需登录）
export const constantRoutes: AppRouteRecordRaw[] = [
  {
    path: "/recruit",
    name: "RecruitList",
    component: () => import("../../views/RecruitList/RecruitList.vue"),
    meta: { title: "招聘列表", hidden: true },
  },
  {
    path: "/login",
    name: "Login",
    component: () => import("../../views/auth/Login/index.vue"),
    meta: { title: "登录", hidden: true },
  },
  {
    path: "/",
    component: () => import("../../layouts/MainLayout.vue"),
    redirect: "/dashboard",
    children: [
      {
        path: "/dashboard",
        name: "Dashboard",
        component: () => import("../../views/home/Dashboard/index.vue"),
        meta: { title: "首页", icon: "HomeFilled", affix: true },
      },
    ],
  },
];

// 动态路由（根据角色过滤）
export const asyncRoutes: AppRouteRecordRaw[] = [
  // 买家中心 - 仅买家可见
  {
    path: "/buyer",
    component: () => import("../../layouts/MainLayout.vue"),
    redirect: "/buyer/orders",
    meta: { title: "买家中心", icon: "ShoppingCart", roles: ["buyer"] },
    children: [
      {
        path: "/buyer/orders",
        name: "BuyerOrders",
        component: () => import("../../views/buyer/Orders/index.vue"),
        meta: { title: "我的订单", icon: "Document" },
      },
    ],
  },
  // 卖家中心 - 仅卖家可见
  {
    path: "/seller",
    component: () => import("../../layouts/MainLayout.vue"),
    redirect: "/seller/shipment",
    meta: { title: "卖家中心", icon: "Shop", roles: ["seller"] },
    children: [
      {
        path: "/seller/shipment",
        name: "SellerShipment",
        component: () => import("../../views/seller/Shipment/index.vue"),
        meta: { title: "我的发货", icon: "Van" },
      },
      {
        path: "/seller/orders",
        name: "SellerOrders",
        component: () => import("../../views/seller/Orders/index.vue"),
        meta: { title: "运输列表", icon: "List" },
      },
    ],
  },
  // 订单管理 - 仅管理员可见
  {
    path: "/order",
    component: () => import("../../layouts/MainLayout.vue"),
    redirect: "/order/list",
    meta: { title: "订单管理", icon: "Document", roles: ["admin"] },
    children: [
      {
        path: "/order/list",
        name: "OrderList",
        component: () => import("../../views/order/OrderList/index.vue"),
        meta: { title: "订单列表", icon: "List" },
      },
      {
        path: "/order/create",
        name: "OrderCreate",
        component: () => import("../../views/order/OrderCreate/index.vue"),
        meta: { title: "创建订单", icon: "Plus", noCache: true },
      },
      {
        path: "/order/waybill",
        name: "OrderWaybill",
        component: () => import("../../views/order/Waybill/index.vue"),
        meta: { title: "运单管理", icon: "Tickets" },
      },
    ],
  },
  // 物流查询 - 所有角色可见
  {
    path: "/transport",
    component: () => import("../../layouts/MainLayout.vue"),
    redirect: "/transport/track",
    meta: { title: "物流查询", icon: "Van" },
    children: [
      {
        path: "/transport/track",
        name: "TransportTrack",
        component: () =>
          import("../../views/transport/TransportTrack/index.vue"),
        meta: { title: "物流跟踪", icon: "Location" },
      },
    ],
  },
  // 数据统计 - 仅管理员可见
  {
    path: "/statistics",
    component: () => import("../../layouts/MainLayout.vue"),
    redirect: "/statistics/report",
    meta: { title: "数据统计", icon: "DataAnalysis", roles: ["admin"] },
    children: [
      {
        path: "/statistics/report",
        name: "StatisticsReport",
        component: () => import("../../views/statistics/Report/index.vue"),
        meta: { title: "统计报表", icon: "TrendCharts" },
      },
    ],
  },
  // 系统设置 - 仅管理员可见
  {
    path: "/settings",
    component: () => import("../../layouts/MainLayout.vue"),
    redirect: "/settings/system",
    meta: { title: "系统设置", icon: "Setting", roles: ["admin"] },
    children: [
      {
        path: "/settings/system",
        name: "SystemSettings",
        component: () =>
          import("../../views/settings/SystemSettings/index.vue"),
        meta: { title: "系统配置", icon: "Tools" },
      },
      {
        path: "/settings/user",
        name: "UserSettings",
        component: () => import("../../views/settings/UserSettings/index.vue"),
        meta: { title: "用户管理", icon: "User" },
      },
    ],
  },
];

// 根据角色过滤路由
export function filterRoutesByRole(
  routes: AppRouteRecordRaw[],
  role: UserRole,
): AppRouteRecordRaw[] {
  return routes
    .filter((route) => {
      // 如果没有设置 roles，则所有角色可访问
      if (!route.meta?.roles || route.meta.roles.length === 0) {
        return true;
      }
      // 检查当前角色是否在允许列表中
      return route.meta.roles.includes(role);
    })
    .map((route) => {
      // 递归过滤子路由
      if (route.children) {
        return { ...route, children: filterRoutesByRole(route.children, role) };
      }
      return route;
    });
}
