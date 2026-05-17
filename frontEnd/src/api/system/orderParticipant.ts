import request from "../../utils/request";

export type ParticipantRole = "buyer" | "seller";

export interface OrderParticipant {
  phone: string;
  displayName: string;
  pendingCount: number;
  shippingCount: number;
  completedCount: number;
  cancelledCount: number;
  totalCount: number;
  lastOrderTime: string;
}

export interface PageResult<T> {
  data: T[];
  total: number;
}

export interface OrderParticipantQuery {
  role: ParticipantRole;
  keyword?: string;
  status?: string;
  page: number;
  pageSize: number;
}

export function getOrderParticipants(
  params: OrderParticipantQuery,
): Promise<PageResult<OrderParticipant>> {
  return request.get("/system/order-participants", { params });
}
