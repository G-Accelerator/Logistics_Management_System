package com.example.demo.dto;

import java.util.List;

/**
 * 订单导出请求
 */
public class ExportRequest {
    private List<Long> ids;           // 指定导出的订单ID（可选）
    private OrderQueryParams filters; // 筛选条件（可选）

    public ExportRequest() {}

    public ExportRequest(List<Long> ids, OrderQueryParams filters) {
        this.ids = ids;
        this.filters = filters;
    }

    // Getters and Setters
    public List<Long> getIds() { return ids; }
    public void setIds(List<Long> ids) { this.ids = ids; }

    public OrderQueryParams getFilters() { return filters; }
    public void setFilters(OrderQueryParams filters) { this.filters = filters; }

    /**
     * 订单查询参数
     */
    public static class OrderQueryParams {
        private String orderNo;
        private String status;
        private String cargoType;
        private String cargoName;
        private String expressCompany;
        private String senderName;
        private String receiverName;
        private String receiverPhone;

        public OrderQueryParams() {}

        // Getters and Setters
        public String getOrderNo() { return orderNo; }
        public void setOrderNo(String orderNo) { this.orderNo = orderNo; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getCargoType() { return cargoType; }
        public void setCargoType(String cargoType) { this.cargoType = cargoType; }

        public String getCargoName() { return cargoName; }
        public void setCargoName(String cargoName) { this.cargoName = cargoName; }

        public String getExpressCompany() { return expressCompany; }
        public void setExpressCompany(String expressCompany) { this.expressCompany = expressCompany; }

        public String getSenderName() { return senderName; }
        public void setSenderName(String senderName) { this.senderName = senderName; }

        public String getReceiverName() { return receiverName; }
        public void setReceiverName(String receiverName) { this.receiverName = receiverName; }

        public String getReceiverPhone() { return receiverPhone; }
        public void setReceiverPhone(String receiverPhone) { this.receiverPhone = receiverPhone; }
    }
}
