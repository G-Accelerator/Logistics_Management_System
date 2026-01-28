import type { AppRouteRecordRaw } from "./types";

export type { AppRouteRecordRaw, RouteMeta } from "./types";

// 公共路由
export const constantRoutes: AppRouteRecordRaw[] = [
  {
    path: "/login",
    name: "Login",
    component: () => import("../../views/auth/Login/index.vue"),
    meta: {
      title: "登录",
      hidden: true,
    },
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
        meta: {
          title: "首页",
          icon: "HomeFilled",
          affix: true,
        },
      },
    ],
  },
];

// 动态路由（根据权限加载）
export const asyncRoutes: AppRouteRecordRaw[] = [
  {
    path: "/order",
    component: () => import("../../layouts/MainLayout.vue"),
    redirect: "/order/list",
    meta: {
      title: "订单管理",
      icon: "Document",
    },
    children: [
      {
        path: "/order/list",
        name: "OrderList",
        component: () => import("../../views/order/OrderList/index.vue"),
        meta: {
          title: "订单列表",
          icon: "List",
        },
      },
      {
        path: "/order/create",
        name: "OrderCreate",
        component: () => import("../../views/order/OrderCreate/index.vue"),
        meta: {
          title: "创建订单",
          icon: "Plus",
          noCache: true,
        },
      },
      {
        path: "/order/detail/:id",
        name: "OrderDetail",
        component: () => import("../../views/order/OrderDetail/index.vue"),
        meta: {
          title: "订单详情",
          icon: "View",
          hidden: true,
        },
      },
    ],
  },
  {
    path: "/warehouse",
    component: () => import("../../layouts/MainLayout.vue"),
    redirect: "/warehouse/list",
    meta: {
      title: "仓库管理",
      icon: "Box",
    },
    children: [
      {
        path: "/warehouse/list",
        name: "WarehouseList",
        component: () =>
          import("../../views/warehouse/WarehouseList/index.vue"),
        meta: {
          title: "仓库列表",
          icon: "List",
        },
      },
      {
        path: "/warehouse/inventory",
        name: "WarehouseInventory",
        component: () =>
          import("../../views/warehouse/WarehouseInventory/index.vue"),
        meta: {
          title: "库存管理",
          icon: "Goods",
        },
      },
      {
        path: "/warehouse/inventory-list",
        name: "InventoryList",
        component: () =>
          import("../../views/warehouse/InventoryList/index.vue"),
        meta: {
          title: "库存列表（新）",
          icon: "Goods",
        },
      },
    ],
  },
  {
    path: "/transport",
    component: () => import("../../layouts/MainLayout.vue"),
    redirect: "/transport/list",
    meta: {
      title: "运输管理",
      icon: "Van",
    },
    children: [
      {
        path: "/transport/list",
        name: "TransportList",
        component: () =>
          import("../../views/transport/TransportList/index.vue"),
        meta: {
          title: "运输列表",
          icon: "List",
        },
      },
      {
        path: "/transport/track",
        name: "TransportTrack",
        component: () =>
          import("../../views/transport/TransportTrack/index.vue"),
        meta: {
          title: "运输跟踪",
          icon: "Location",
        },
      },
      {
        path: "/transport/shipment",
        name: "ShipmentList",
        component: () => import("../../views/transport/ShipmentList/index.vue"),
        meta: {
          title: "货运管理（新）",
          icon: "Van",
        },
      },
    ],
  },
  {
    path: "/settings",
    component: () => import("../../layouts/MainLayout.vue"),
    redirect: "/settings/system",
    meta: {
      title: "系统设置",
      icon: "Setting",
    },
    children: [
      {
        path: "/settings/system",
        name: "SystemSettings",
        component: () =>
          import("../../views/settings/SystemSettings/index.vue"),
        meta: {
          title: "系统配置",
          icon: "Tools",
        },
      },
      {
        path: "/settings/user",
        name: "UserSettings",
        component: () => import("../../views/settings/UserSettings/index.vue"),
        meta: {
          title: "用户管理",
          icon: "User",
        },
      },
    ],
  },
];
