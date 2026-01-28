import type { RouteRecordRaw } from "vue-router";

// 扩展路由元信息类型
export interface RouteMeta {
  title?: string;
  icon?: string;
  hidden?: boolean;
  alwaysShow?: boolean;
  noCache?: boolean;
  affix?: boolean;
  noShowingChildren?: boolean;
}

export interface AppRouteRecordRaw extends Omit<
  RouteRecordRaw,
  "meta" | "children"
> {
  meta?: RouteMeta;
  children?: AppRouteRecordRaw[];
  noShowingChildren?: boolean;
}
