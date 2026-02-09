package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/**
 * 操作日志实体类
 * 记录订单状态变更的操作日志
 */
public class OperationLog {
    private Long id;
    private String orderNo;
    private String action;        // ship, receive, cancel
    private String fromStatus;
    private String toStatus;
    private String operator;      // 操作人（预留，当前为 "system"）
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operateTime;
    private String remark;

    public OperationLog() {}

    public OperationLog(String orderNo, String action, String fromStatus, String toStatus, String operator) {
        this.orderNo = orderNo;
        this.action = action;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.operator = operator;
        this.operateTime = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getFromStatus() { return fromStatus; }
    public void setFromStatus(String fromStatus) { this.fromStatus = fromStatus; }
    public String getToStatus() { return toStatus; }
    public void setToStatus(String toStatus) { this.toStatus = toStatus; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public LocalDateTime getOperateTime() { return operateTime; }
    public void setOperateTime(LocalDateTime operateTime) { this.operateTime = operateTime; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
