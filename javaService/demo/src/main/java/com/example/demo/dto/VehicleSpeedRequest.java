package com.example.demo.dto;

/**
 * 运单调整绑定车辆当前车速
 */
public class VehicleSpeedRequest {
    private Integer currentSpeedKmh;

    public Integer getCurrentSpeedKmh() {
        return currentSpeedKmh;
    }

    public void setCurrentSpeedKmh(Integer currentSpeedKmh) {
        this.currentSpeedKmh = currentSpeedKmh;
    }
}
