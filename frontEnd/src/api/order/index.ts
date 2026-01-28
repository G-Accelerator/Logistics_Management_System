import request from "../../utils/request";
import type { Order, PageResult, OrderQueryParams } from "./types";

export type { Order, PageResult, OrderQueryParams };

export function getOrders(
  params: OrderQueryParams,
): Promise<PageResult<Order>> {
  return request.get("/orders", { params });
}
