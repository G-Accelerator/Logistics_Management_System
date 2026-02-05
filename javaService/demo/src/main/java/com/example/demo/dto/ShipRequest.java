package com.example.demo.dto;

import com.example.demo.dto.RoutePlanResponse.TrackPoint;
import java.util.List;

/**
 * 发货请求 DTO
 */
public class ShipRequest {
    private List<TrackPoint> trackPoints;
    private int duration;

    public ShipRequest() {}

    public List<TrackPoint> getTrackPoints() { return trackPoints; }
    public void setTrackPoints(List<TrackPoint> trackPoints) { this.trackPoints = trackPoints; }
    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }
}
