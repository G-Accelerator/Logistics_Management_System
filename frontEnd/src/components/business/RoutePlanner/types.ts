/** 路线策略 */
export type RouteStrategy = 0 | 1 | 2 | 4;

/** 路线站点 */
export interface RouteTrackPoint {
  time: string;
  status: string;
  location: string;
  lng: number;
  lat: number;
  passed: boolean;
  isCurrent?: boolean;
}

/** 路线选项 */
export interface RouteOptionData {
  label: string;
  tagType: "primary" | "success" | "warning" | "info";
  description: string;
  distance: number;
  duration: number;
  tolls: number; // 收费金额（元）
  trackPoints: RouteTrackPoint[];
}

/** 组件 Props */
export interface RoutePlannerProps {
  origin: string;
  destination: string;
  originCoord?: [number, number] | null;
  destCoord?: [number, number] | null;
}

/** 组件暴露的方法 */
export interface RoutePlannerExpose {
  clearRoutes: () => void;
  getSelectedRoute: () => RouteOptionData | null;
  getTrackPoints: () => RouteTrackPoint[];
}
