package com.example.demo.dto;

public class VehicleNotifyRequest {

    private Long vehicleId;
    private String orderNo;
    /** OVERSPEED | OFFLINE | REGULAR */
    private String templateType;
    /** 手动发送正文（可基于模板修改）；为空则使用模板渲染结果 */
    private String content;

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getTemplateType() {
        return templateType;
    }

    public void setTemplateType(String templateType) {
        this.templateType = templateType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
