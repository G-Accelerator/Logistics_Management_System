import request from "../../utils/request";
import type {
  StatisticsOverview,
  TrendData,
  DistributionData,
  StatusDistribution,
  TopCitiesParams,
  TrendParams,
} from "./types";

export type {
  StatisticsOverview,
  TrendData,
  DistributionData,
  StatusDistribution,
  TopCitiesParams,
  TrendParams,
} from "./types";

/**
 * 获取统计概览
 * GET /api/statistics/overview
 */
export function getStatisticsOverview(): Promise<StatisticsOverview> {
  return request.get("/statistics/overview");
}

/**
 * 获取订单趋势
 * GET /api/statistics/trend?days=7
 */
export function getOrderTrend(params?: TrendParams): Promise<TrendData> {
  return request.get("/statistics/trend", { params });
}

/**
 * 获取状态分布
 * GET /api/statistics/status-distribution
 */
export function getStatusDistribution(): Promise<StatusDistribution> {
  return request.get("/statistics/status-distribution");
}

/**
 * 获取热门城市
 * GET /api/statistics/top-cities?type=origin&limit=10
 */
export function getTopCities(
  params: TopCitiesParams,
): Promise<DistributionData> {
  return request.get("/statistics/top-cities", { params });
}

/**
 * 获取快递公司统计
 * GET /api/statistics/express-companies
 */
export function getExpressCompanies(): Promise<DistributionData> {
  return request.get("/statistics/express-companies");
}
