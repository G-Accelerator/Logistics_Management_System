package com.example.demo.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 批量操作结果
 */
public class BatchResult {
    private boolean success;
    private int successCount;
    private int failedCount;
    private List<String> failedOrders;
    private String message;

    public BatchResult() {
        this.failedOrders = new ArrayList<>();
    }

    public BatchResult(boolean success, int successCount, List<String> failedOrders) {
        this.success = success;
        this.successCount = successCount;
        this.failedOrders = failedOrders != null ? failedOrders : new ArrayList<>();
        this.failedCount = this.failedOrders.size();
    }

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public int getSuccessCount() { return successCount; }
    public void setSuccessCount(int successCount) { this.successCount = successCount; }
    public int getFailedCount() { return failedCount; }
    public void setFailedCount(int failedCount) { this.failedCount = failedCount; }
    public List<String> getFailedOrders() { return failedOrders; }
    public void setFailedOrders(List<String> failedOrders) { 
        this.failedOrders = failedOrders; 
        this.failedCount = failedOrders != null ? failedOrders.size() : 0;
    }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
