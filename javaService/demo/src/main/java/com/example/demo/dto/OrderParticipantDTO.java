package com.example.demo.dto;

/**
 * 从订单聚合的买家/卖家统计
 */
public class OrderParticipantDTO {
    private String phone;
    private String displayName;
    private long pendingCount;
    private long shippingCount;
    private long completedCount;
    private long cancelledCount;
    private long totalCount;
    private String lastOrderTime;

    public OrderParticipantDTO() {}

    public OrderParticipantDTO(
            String phone,
            String displayName,
            long pendingCount,
            long shippingCount,
            long completedCount,
            long cancelledCount,
            long totalCount,
            String lastOrderTime) {
        this.phone = phone;
        this.displayName = displayName;
        this.pendingCount = pendingCount;
        this.shippingCount = shippingCount;
        this.completedCount = completedCount;
        this.cancelledCount = cancelledCount;
        this.totalCount = totalCount;
        this.lastOrderTime = lastOrderTime;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public long getPendingCount() {
        return pendingCount;
    }

    public void setPendingCount(long pendingCount) {
        this.pendingCount = pendingCount;
    }

    public long getShippingCount() {
        return shippingCount;
    }

    public void setShippingCount(long shippingCount) {
        this.shippingCount = shippingCount;
    }

    public long getCompletedCount() {
        return completedCount;
    }

    public void setCompletedCount(long completedCount) {
        this.completedCount = completedCount;
    }

    public long getCancelledCount() {
        return cancelledCount;
    }

    public void setCancelledCount(long cancelledCount) {
        this.cancelledCount = cancelledCount;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public String getLastOrderTime() {
        return lastOrderTime;
    }

    public void setLastOrderTime(String lastOrderTime) {
        this.lastOrderTime = lastOrderTime;
    }
}
