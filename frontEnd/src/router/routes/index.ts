import type { AppRouteRecordRaw } from "./types";

export type { AppRouteRecordRaw, RouteMeta } from "./types";

// 公共路由
export const constantRoutes: AppRouteRecordRaw[] = [
  {
    path: "/recruit",
    name: "RecruitList",
    component: () => import("../../views/RecruitList/RecruitList.vue"),
    meta: {
      title: "招聘列表",
      hidden: true,
    },
  },
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
    path: "/seller",
    component: () => import("../../layouts/MainLayout.vue"),
    redirect: "/seller/shipment",
    meta: {
      title: "卖家中心",
      icon: "Shop",
    },
    children: [
      {
        path: "/seller/shipment",
        name: "SellerShipment",
        component: () => import("../../views/seller/Shipment/index.vue"),
        meta: {
          title: "我的发货",
          icon: "Van",
        },
      },
    ],
  },
  {
    path: "/buyer",
    component: () => import("../../layouts/MainLayout.vue"),
    redirect: "/buyer/orders",
    meta: {
      title: "买家中心",
      icon: "ShoppingCart",
    },
    children: [
      {
        path: "/buyer/orders",
        name: "BuyerOrders",
        component: () => import("../../views/buyer/Orders/index.vue"),
        meta: {
          title: "我的订单",
          icon: "Document",
        },
      },
    ],
  },
  {
    path: "/admin",
    component: () => import("../../layouts/MainLayout.vue"),
    redirect: "/admin/waybill",
    meta: {
      title: "管理员中心",
      icon: "Management",
    },
    children: [
      {
        path: "/admin/waybill",
        name: "AdminWaybill",
        component: () => import("../../views/admin/Waybill/index.vue"),
        meta: {
          title: "运单管理",
          icon: "Tickets",
        },
      },
    ],
  },
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
