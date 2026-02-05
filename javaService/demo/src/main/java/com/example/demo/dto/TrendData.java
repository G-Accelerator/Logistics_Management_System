package com.example.demo.dto;

import java.util.List;

/**
 * 订单趋势数据
 */
public class TrendData {
    private List<String> dates;   // 日期数组
    private List<Long> counts;    // 订单数数组

    public TrendData() {}

    public TrendData(List<String> dates, List<Long> counts) {
        this.dates = dates;
        this.counts = counts;
    }

    public List<String> getDates() { return dates; }
    public void setDates(List<String> dates) { this.dates = dates; }

    public List<Long> getCounts() { return counts; }
    public void setCounts(List<Long> counts) { this.counts = counts; }
}
