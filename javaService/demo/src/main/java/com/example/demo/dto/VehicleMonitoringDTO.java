package com.example.demo.dto;

/**
 * 车辆监控列表行（运输中且已绑车）
 */
public class VehicleMonitoringDTO {
    private Long vehicleId;
    private String plateNumber;
    private String vehicleType;
    private String driverName;
    private String driverPhone;
    private Integer speedLimitKmh;
    private Boolean online;
    private String orderNo;
    private String trackingNo;
    private Integer currentSpeedKmh;
    private boolean overspeed;
    private boolean offlineAlert;

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getDriverPhone() {
        return driverPhone;
    }

    public void setDriverPhone(String driverPhone) {
        this.driverPhone = driverPhone;
    }

    public Integer getSpeedLimitKmh() {
        return speedLimitKmh;
    }

    public void setSpeedLimitKmh(Integer speedLimitKmh) {
        this.speedLimitKmh = speedLimitKmh;
    }

    public Boolean getOnline() {
        return online;
    }

    public void setOnline(Boolean online) {
        this.online = online;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getTrackingNo() {
        return trackingNo;
    }

    public void setTrackingNo(String trackingNo) {
        this.trackingNo = trackingNo;
    }

    public Integer getCurrentSpeedKmh() {
        return currentSpeedKmh;
    }

    public void setCurrentSpeedKmh(Integer currentSpeedKmh) {
        this.currentSpeedKmh = currentSpeedKmh;
    }

    public boolean isOverspeed() {
        return overspeed;
    }

    public void setOverspeed(boolean overspeed) {
        this.overspeed = overspeed;
    }

    public boolean isOfflineAlert() {
        return offlineAlert;
    }

    public void setOfflineAlert(boolean offlineAlert) {
        this.offlineAlert = offlineAlert;
    }
}
