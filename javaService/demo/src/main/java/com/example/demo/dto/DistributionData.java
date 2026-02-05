package com.example.demo.dto;

import java.util.List;

/**
 * 分布数据（用于状态分布、城市分布、快递公司分布等）
 */
public class DistributionData {
    private List<String> labels;  // 标签数组
    private List<Long> values;    // 数值数组

    public DistributionData() {}

    public DistributionData(List<String> labels, List<Long> values) {
        this.labels = labels;
        this.values = values;
    }

    public List<String> getLabels() { return labels; }
    public void setLabels(List<String> labels) { this.labels = labels; }

    public List<Long> getValues() { return values; }
    public void setValues(List<Long> values) { this.values = values; }
}
