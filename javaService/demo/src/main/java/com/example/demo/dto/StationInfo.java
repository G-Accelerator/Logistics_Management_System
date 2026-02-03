package com.example.demo.dto;

/**
 * 站点信息 DTO，用于 API 响应
 */
public class StationInfo {
    private int index;
    private String location;
    private double lng;
    private double lat;
    // 站点状态: "pending"(待到达) | "arrived"(已到达)
    private String status;
    // 到达时间，格式 "yyyy-MM-dd HH:mm:ss"
    private String arrivalTime;

    public StationInfo() {}

    public StationInfo(int index, String location, double lng, double lat, String status, String arrivalTime) {
        this.index = index;
        this.location = location;
        this.lng = lng;
        this.lat = lat;
        this.status = status;
        this.arrivalTime = arrivalTime;
    }

    public int getIndex() { return index; }
    public void setIndex(int index) { this.index = index; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public double getLng() { return lng; }
    public void setLng(double lng) { this.lng = lng; }
    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(String arrivalTime) { this.arrivalTime = arrivalTime; }
}
