package com.example.demo.dto;

import java.util.List;

/**
 * 路线规划请求
 */
public class RoutePlanRequest {
    private String origin;
    private String destination;
    private List<Double> originCoord;
    private List<Double> destCoord;
    private List<Integer> strategies; // 指定要规划的策略: 0-最快 1-最短 2-最省钱 4-躲避拥堵

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public List<Double> getOriginCoord() {
        return originCoord;
    }

    public void setOriginCoord(List<Double> originCoord) {
        this.originCoord = originCoord;
    }

    public List<Double> getDestCoord() {
        return destCoord;
    }

    public void setDestCoord(List<Double> destCoord) {
        this.destCoord = destCoord;
    }

    public List<Integer> getStrategies() {
        return strategies;
    }

    public void setStrategies(List<Integer> strategies) {
        this.strategies = strategies;
    }
}
