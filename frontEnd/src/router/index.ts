import { createRouter, createWebHistory } from "vue-router";
import { constantRoutes, asyncRoutes } from "./routes";

export { constantRoutes, asyncRoutes };
export type { AppRouteRecordRaw, RouteMeta } from "./routes";

const router = createRouter({
  history: createWebHistory(),
  routes: [...constantRoutes, ...asyncRoutes] as any,
});

// 路由守卫
router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem("token");

  if (to.path === "/login") {
    if (token) {
      next("/dashboard");
    } else {
      next();
    }
  } else {
    if (token) {
      next();
    } else {
      next("/login");
    }
  }
});

export default router;
