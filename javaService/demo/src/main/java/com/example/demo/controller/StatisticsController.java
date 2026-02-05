package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.DistributionData;
import com.example.demo.dto.StatisticsOverview;
import com.example.demo.dto.TrendData;
import com.example.demo.service.StatisticsService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@CrossOrigin(origins = "*")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    /**
     * 获取统计概览
     * GET /api/statistics/overview
     */
    @GetMapping("/overview")
    public ApiResponse<StatisticsOverview> getOverview() {
        StatisticsOverview overview = statisticsService.getOverview();
        return ApiResponse.success(overview);
    }

    /**
     * 获取订单趋势
     * GET /api/statistics/trend?days=7
     */
    @GetMapping("/trend")
    public ApiResponse<TrendData> getTrend(@RequestParam(defaultValue = "7") int days) {
        TrendData trend = statisticsService.getTrend(days);
        return ApiResponse.success(trend);
    }

    /**
     * 获取状态分布
     * GET /api/statistics/status-distribution
     */
    @GetMapping("/status-distribution")
    public ApiResponse<Map<String, Long>> getStatusDistribution() {
        Map<String, Long> distribution = statisticsService.getStatusDistribution();
        return ApiResponse.success(distribution);
    }

    /**
     * 获取热门城市
     * GET /api/statistics/top-cities?type=origin&limit=10
     */
    @GetMapping("/top-cities")
    public ApiResponse<DistributionData> getTopCities(
            @RequestParam(defaultValue = "origin") String type,
            @RequestParam(defaultValue = "10") int limit) {
        DistributionData cities = statisticsService.getTopCities(type, limit);
        return ApiResponse.success(cities);
    }

    /**
     * 获取快递公司统计
     * GET /api/statistics/express-companies
     */
    @GetMapping("/express-companies")
    public ApiResponse<DistributionData> getExpressCompanies() {
        DistributionData companies = statisticsService.getExpressCompanies();
        return ApiResponse.success(companies);
    }
}
