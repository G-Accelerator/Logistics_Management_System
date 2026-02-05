package com.example.demo.dto;

/**
 * 统计概览数据
 */
public class StatisticsOverview {
    private long totalOrders;       // 总订单数
    private long monthOrders;       // 本月订单数
    private long pendingOrders;     // 待处理订单数
    private double completionRate;  // 完成率 (0-100)
    private double monthGrowth;     // 月环比增长率

    public StatisticsOverview() {}

    public StatisticsOverview(long totalOrders, long monthOrders, long pendingOrders, 
                              double completionRate, double monthGrowth) {
        this.totalOrders = totalOrders;
        this.monthOrders = monthOrders;
        this.pendingOrders = pendingOrders;
        this.completionRate = completionRate;
        this.monthGrowth = monthGrowth;
    }

    public long getTotalOrders() { return totalOrders; }
    public void setTotalOrders(long totalOrders) { this.totalOrders = totalOrders; }

    public long getMonthOrders() { return monthOrders; }
    public void setMonthOrders(long monthOrders) { this.monthOrders = monthOrders; }

    public long getPendingOrders() { return pendingOrders; }
    public void setPendingOrders(long pendingOrders) { this.pendingOrders = pendingOrders; }

    public double getCompletionRate() { return completionRate; }
    public void setCompletionRate(double completionRate) { this.completionRate = completionRate; }

    public double getMonthGrowth() { return monthGrowth; }
    public void setMonthGrowth(double monthGrowth) { this.monthGrowth = monthGrowth; }
}
