package com.example.demo.dto;

import java.util.List;

/**
 * 批量订单操作请求
 */
public class BatchOrderRequest {
    private List<String> orderNos;

    public BatchOrderRequest() {}

    public BatchOrderRequest(List<String> orderNos) {
        this.orderNos = orderNos;
    }

    public List<String> getOrderNos() {
        return orderNos;
    }

    public void setOrderNos(List<String> orderNos) {
        this.orderNos = orderNos;
    }
}
