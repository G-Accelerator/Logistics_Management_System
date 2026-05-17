package com.example.demo.dto;

import com.example.demo.dto.RoutePlanResponse.TrackPoint;
import java.util.List;

/**
 * 发货请求 DTO
 */
public class ShipRequest {
    private List<TrackPoint> trackPoints;
    private int duration;
    /** 发货必选绑定车辆 */
    private Long vehicleId;

    public ShipRequest() {}

    public List<TrackPoint> getTrackPoints() { return trackPoints; }
    public void setTrackPoints(List<TrackPoint> trackPoints) { this.trackPoints = trackPoints; }
    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }
    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }
}
