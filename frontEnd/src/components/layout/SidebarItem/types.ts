import type { AppRouteRecordRaw } from "../../../types/router";

export type { AppRouteRecordRaw };

export interface SidebarItemProps {
  item: AppRouteRecordRaw;
  basePath: string;
  isCollapse?: boolean;
}
