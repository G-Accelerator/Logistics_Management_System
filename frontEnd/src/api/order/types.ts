export interface Order {
  id: number;
  orderNo: string;
  cargoName: string;
  cargoType: string;
  origin: string;
  destination: string;
  senderName: string;
  receiverName: string;
  senderPhone: string;
  receiverPhone: string;
  status: string;
  createTime: string;
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
}
