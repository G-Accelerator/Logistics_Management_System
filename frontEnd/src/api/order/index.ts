import request from "../../utils/request";
import type {
  Order,
  PageResult,
  OrderQueryParams,
  RoutePlanRequest,
  RoutePlanResponse,
  RouteTrackPoint,
  CreateOrderRequest,
  BatchResult,
  OperationLog,
  StationInfo,
  StationArriveResponse,
  BatchStationArriveResponse,
} from "./types";

export type {
  Order,
  PageResult,
  OrderQueryParams,
  RoutePlanRequest,
  RoutePlanResponse,
  RouteOptionData,
  RouteTrackPoint,
  CreateOrderRequest,
  BatchResult,
  OperationLog,
  StationInfo,
  StationStatus,
  StationArriveResponse,
  BatchStationArriveResponse,
} from "./types";

export function getOrders(
  params: OrderQueryParams,
): Promise<PageResult<Order>> {
  return request.get("/orders", { params });
}

/**
 * 获取订单统计
 */
export function getOrderStats(): Promise<{
  total: number;
  pending: number;
  shipping: number;
  completed: number;
}> {
  return request.get("/orders/stats");
}

/**
 * 买家订单列表（根据token自动过滤）
 */
export function getBuyerOrders(
  params: Omit<OrderQueryParams, "receiverPhone">,
): Promise<PageResult<Order>> {
  return request.get("/orders/buyer", { params });
}

/**
 * 买家订单统计（根据token自动过滤）
 */
export function getBuyerStats(): Promise<{
  total: number;
  pending: number;
  shipping: number;
  completed: number;
}> {
  return request.get("/orders/buyer/stats");
}

/**
 * 卖家订单列表（根据token自动过滤）
 */
export function getSellerOrders(
  params: Omit<OrderQueryParams, "senderPhone">,
): Promise<PageResult<Order>> {
  return request.get("/orders/seller", { params });
}

/**
 * 卖家订单统计（根据token自动过滤）
 */
export function getSellerStats(): Promise<{
  total: number;
  pending: number;
  shipping: number;
  completed: number;
}> {
  return request.get("/orders/seller/stats");
}

/**
 * 获取单个订单
 */
export function getOrder(orderNo: string): Promise<Order> {
  return request.get(`/orders/${orderNo}`);
}

/**
 * 创建订单
 */
export function createOrder(data: CreateOrderRequest): Promise<Order> {
  return request.post("/orders", data);
}

/**
 * 删除订单
 */
export function deleteOrder(orderNo: string): Promise<boolean> {
  return request.delete(`/orders/${orderNo}`);
}

/**
 * 获取订单站点数据
 */
export function getTrackPoints(orderNo: string): Promise<RouteTrackPoint[]> {
  return request.get(`/orders/${orderNo}/track-points`);
}

/**
 * 路线规划
 */
export function planRouteApi(
  data: RoutePlanRequest,
): Promise<RoutePlanResponse> {
  return request.post("/orders/plan-route", data, { timeout: 30000 });
}

// ==================== 状态变更 API ====================

/**
 * 发货操作
 */
export function shipOrder(orderNo: string): Promise<Order> {
  return request.put(`/orders/${orderNo}/ship`);
}

/**
 * 签收操作
 */
export function receiveOrder(orderNo: string): Promise<Order> {
  return request.put(`/orders/${orderNo}/receive`);
}

/**
 * 取消订单
 */
export function cancelOrder(orderNo: string): Promise<Order> {
  return request.put(`/orders/${orderNo}/cancel`);
}

// ==================== 批量操作 API ====================

/**
 * 批量发货
 */
export function batchShip(orderNos: string[]): Promise<BatchResult> {
  return request.put("/orders/batch/ship", { orderNos });
}

/**
 * 批量签收
 */
export function batchReceive(orderNos: string[]): Promise<BatchResult> {
  return request.put("/orders/batch/receive", { orderNos });
}

// ==================== 操作日志 API ====================

/**
 * 获取订单操作日志
 */
export function getOrderLogs(orderNo: string): Promise<OperationLog[]> {
  return request.get(`/orders/${orderNo}/logs`);
}

// ==================== 站点状态 API ====================

/**
 * 获取订单站点状态
 */
export function getStationStatus(orderNo: string): Promise<StationInfo[]> {
  return request.get(`/orders/${orderNo}/stations`);
}

/**
 * 标记单个站点到达
 */
export function markStationArrived(
  orderNo: string,
  stationIndex: number,
): Promise<StationArriveResponse> {
  return request.put(`/orders/${orderNo}/stations/${stationIndex}/arrive`);
}

/**
 * 标记全部站点到达
 */
export function markAllStationsArrived(
  orderNo: string,
): Promise<BatchStationArriveResponse> {
  return request.put(`/orders/${orderNo}/stations/arrive-all`);
}

/**
 * 标记到达至指定站点
 */
export function markStationsArrivedTo(
  orderNo: string,
  targetIndex: number,
): Promise<BatchStationArriveResponse> {
  return request.put(`/orders/${orderNo}/stations/arrive-to/${targetIndex}`);
}
