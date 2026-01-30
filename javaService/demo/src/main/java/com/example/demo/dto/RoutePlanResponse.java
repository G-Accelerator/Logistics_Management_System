package com.example.demo.dto;

import java.util.List;

/**
 * 路线规划响应
 */
public class RoutePlanResponse {
    private List<RouteOption> routes;

    public RoutePlanResponse() {}

    public RoutePlanResponse(List<RouteOption> routes) {
        this.routes = routes;
    }

    public List<RouteOption> getRoutes() {
        return routes;
    }

    public void setRoutes(List<RouteOption> routes) {
        this.routes = routes;
    }

    /**
     * 路线选项
     */
    public static class RouteOption {
        private String label;
        private String tagType;
        private String description;
        private int distance;
        private int duration;
        private List<TrackPoint> trackPoints;

        public RouteOption() {}

        public RouteOption(String label, String tagType, String description, 
                          int distance, int duration, List<TrackPoint> trackPoints) {
            this.label = label;
            this.tagType = tagType;
            this.description = description;
            this.distance = distance;
            this.duration = duration;
            this.trackPoints = trackPoints;
        }

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public String getTagType() { return tagType; }
        public void setTagType(String tagType) { this.tagType = tagType; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public int getDistance() { return distance; }
        public void setDistance(int distance) { this.distance = distance; }
        public int getDuration() { return duration; }
        public void setDuration(int duration) { this.duration = duration; }
        public List<TrackPoint> getTrackPoints() { return trackPoints; }
        public void setTrackPoints(List<TrackPoint> trackPoints) { this.trackPoints = trackPoints; }
    }

    /**
     * 路线站点
     */
    public static class TrackPoint {
        private String time;
        private String status;
        private String location;
        private double lng;
        private double lat;
        private boolean passed;
        private boolean isCurrent;

        public TrackPoint() {}

        public TrackPoint(String status, String location, double lng, double lat) {
            this.time = "";
            this.status = status;
            this.location = location;
            this.lng = lng;
            this.lat = lat;
            this.passed = false;
            this.isCurrent = false;
        }

        public String getTime() { return time; }
        public void setTime(String time) { this.time = time; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public double getLng() { return lng; }
        public void setLng(double lng) { this.lng = lng; }
        public double getLat() { return lat; }
        public void setLat(double lat) { this.lat = lat; }
        public boolean isPassed() { return passed; }
        public void setPassed(boolean passed) { this.passed = passed; }
        public boolean isCurrent() { return isCurrent; }
        public void setCurrent(boolean current) { isCurrent = current; }
    }
}
