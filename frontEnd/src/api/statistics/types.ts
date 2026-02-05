/**
 * 统计概览数据
 */
export interface StatisticsOverview {
  totalOrders: number; // 总订单数
  monthOrders: number; // 本月订单数
  pendingOrders: number; // 待处理订单数
  completionRate: number; // 完成率 (0-100)
  monthGrowth: number; // 月环比增长率
}

/**
 * 订单趋势数据
 */
export interface TrendData {
  dates: string[]; // 日期数组 ["2026-01-01", ...]
  counts: number[]; // 订单数数组 [10, 15, ...]
}

/**
 * 分布数据（用于城市分布、快递公司分布等）
 */
export interface DistributionData {
  labels: string[]; // 标签数组
  values: number[]; // 数值数组
}

/**
 * 状态分布数据
 */
export interface StatusDistribution {
  pending: number; // 待发货
  shipping: number; // 运输中
  completed: number; // 已完成
  cancelled: number; // 已取消
}

/**
 * 热门城市查询参数
 */
export interface TopCitiesParams {
  type: "origin" | "destination"; // origin-发货城市, destination-收货城市
  limit?: number; // 返回数量限制，默认10
}

/**
 * 订单趋势查询参数
 */
export interface TrendParams {
  days?: number; // 查询天数，默认7
}
