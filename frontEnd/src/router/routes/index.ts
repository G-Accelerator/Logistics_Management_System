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

// 买家专属路由
export const buyerRoutes: AppRouteRecordRaw[] = [
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
    path: "/transport",
    component: () => import("../../layouts/MainLayout.vue"),
    redirect: "/transport/track",
    meta: {
      title: "物流查询",
      icon: "Van",
    },
    children: [
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
];

// 卖家专属路由
export const sellerRoutes: AppRouteRecordRaw[] = [
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
      {
        path: "/seller/orders",
        name: "SellerOrders",
        component: () => import("../../views/seller/Orders/index.vue"),
        meta: {
          title: "运输列表",
          icon: "List",
        },
      },
    ],
  },
  {
    path: "/transport",
    component: () => import("../../layouts/MainLayout.vue"),
    redirect: "/transport/track",
    meta: {
      title: "物流查询",
      icon: "Van",
    },
    children: [
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
];

// 动态路由（管理员路由）
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
        path: "/order/waybill",
        name: "OrderWaybill",
        component: () => import("../../views/order/Waybill/index.vue"),
        meta: {
          title: "运单管理",
          icon: "Tickets",
        },
      },
    ],
  },
  {
    path: "/transport",
    component: () => import("../../layouts/MainLayout.vue"),
    redirect: "/transport/track",
    meta: {
      title: "物流查询",
      icon: "Van",
    },
    children: [
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
