package com.example.demo.dto;

/**
 * 运单在途管控：车速与绑定车辆在线状态（可只传其一）
 */
public class TransportControlRequest {
    private Integer currentSpeedKmh;
    private Boolean vehicleOnline;

    public Integer getCurrentSpeedKmh() {
        return currentSpeedKmh;
    }

    public void setCurrentSpeedKmh(Integer currentSpeedKmh) {
        this.currentSpeedKmh = currentSpeedKmh;
    }

    public Boolean getVehicleOnline() {
        return vehicleOnline;
    }

    public void setVehicleOnline(Boolean vehicleOnline) {
        this.vehicleOnline = vehicleOnline;
    }
}
