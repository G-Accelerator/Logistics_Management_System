export interface Order {
  id?: number;
  orderNo?: string;
  cargoName: string;
  cargoType: string;
  cargoWeight?: number;
  cargoVolume?: number;
  cargoQuantity?: number;
  remark?: string;
  expressCompany?: string;
  origin: string;
  destination: string;
  senderName: string;
  receiverName: string;
  senderPhone: string;
  receiverPhone: string;
  status?: string;
  createTime?: string;
  duration?: number; // 预计时长(秒)
}

/** 创建订单请求 */
export interface CreateOrderRequest {
  cargoName: string;
  cargoType: string;
  cargoWeight?: number;
  cargoVolume?: number;
  cargoQuantity?: number;
  remark?: string;
  expressCompany?: string;
  origin: string;
  destination: string;
  senderName: string;
  receiverName: string;
  senderPhone: string;
  receiverPhone: string;
  duration?: number; // 预计时长(秒)
  trackPoints: RouteTrackPoint[];
}

export interface PageResult<T> {
  data: T[];
  total: number;
}

export interface OrderQueryParams {
  page: number;
  pageSize: number;
  orderNo?: string;
  status?: string;
  cargoType?: string;
  cargoName?: string;
  expressCompany?: string;
  senderName?: string;
  receiverName?: string;
  receiverPhone?: string;
}

/** 路线策略 */
export type RouteStrategy = 0 | 1 | 2 | 4; // 0-最快 1-最短 2-最省钱 4-躲避拥堵

/** 路线规划请求参数 */
export interface RoutePlanRequest {
  origin: string;
  destination: string;
  originCoord?: [number, number];
  destCoord?: [number, number];
  strategies?: RouteStrategy[]; // 指定要规划的策略
}

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
  trackPoints: RouteTrackPoint[];
}

/** 路线规划响应 */
export interface RoutePlanResponse {
  routes: RouteOptionData[];
}

/** 批量操作结果 */
export interface BatchResult {
  success: boolean;
  successCount: number;
  failedOrders?: string[];
  message?: string;
}

/** 操作日志 */
export interface OperationLog {
  id: number;
  orderNo: string;
  action: "ship" | "receive" | "cancel";
  fromStatus: string;
  toStatus: string;
  operator: string;
  operateTime: string;
  remark?: string;
}

// ==================== 站点状态类型 ====================

/** 站点状态 */
export type StationStatus = "pending" | "arrived";

/** 站点信息 */
export interface StationInfo {
  index: number;
  location: string;
  lng: number;
  lat: number;
  status: StationStatus;
  arrivalTime?: string;
}

/** 单个站点到达响应 */
export interface StationArriveResponse {
  success: boolean;
  message: string;
  arrivalTime: string;
  stationIndex: number;
}

/** 批量站点到达响应 */
export interface BatchStationArriveResponse {
  success: boolean;
  message: string;
  arrivedCount: number;
}
