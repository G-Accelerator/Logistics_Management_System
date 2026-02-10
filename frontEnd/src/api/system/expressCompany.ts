import request from "../../utils/request";

export interface ExpressCompany {
  id?: number;
  code: string;
  name: string;
  trackingPrefix?: string;
  sortOrder?: number;
  enabled: boolean;
  phone?: string;
  website?: string;
  createTime?: string;
  updateTime?: string;
}

export interface PageResult<T> {
  data: T[];
  total: number;
}

/**
 * 获取所有快递公司（分页）
 */
export function getExpressCompanies(
  page: number,
  pageSize: number,
): Promise<PageResult<ExpressCompany>> {
  return request.get("/express-companies", { params: { page, pageSize } });
}

/**
 * 获取所有启用的快递公司
 */
export function getEnabledExpressCompanies() {
  return request.get("/express-companies/enabled");
}

/**
 * 创建快递公司
 */
export function createExpressCompany(data: ExpressCompany) {
  return request.post("/express-companies", data);
}

/**
 * 更新快递公司
 */
export function updateExpressCompany(id: number, data: ExpressCompany) {
  return request.put(`/express-companies/${id}`, data);
}

/**
 * 删除快递公司
 */
export function deleteExpressCompany(id: number) {
  return request.delete(`/express-companies/${id}`);
}
