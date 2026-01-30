import request from "../../utils/request";
import type {
  Order,
  PageResult,
  OrderQueryParams,
  RoutePlanRequest,
  RoutePlanResponse,
  RouteTrackPoint,
  CreateOrderRequest,
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
} from "./types";

export function getOrders(
  params: OrderQueryParams,
): Promise<PageResult<Order>> {
  return request.get("/orders", { params });
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
