package com.example.demo.service;

import com.example.demo.dto.RoutePlanRequest;
import com.example.demo.dto.RoutePlanResponse;
import com.example.demo.dto.RoutePlanResponse.RouteOption;
import com.example.demo.dto.RoutePlanResponse.TrackPoint;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 高德地图服务
 */
@Service
public class AMapService {

    private static final Logger log = LoggerFactory.getLogger(AMapService.class);

    @Value("${amap.key}")
    private String amapKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 路线策略（高德 API v3）
    // 参考：https://lbs.amap.com/api/webservice/guide/api/direction
    private static final Map<Integer, RouteStrategy> STRATEGIES = Map.of(
        0, new RouteStrategy("速度优先", "primary", "优先选择耗时最短的路线"),
        1, new RouteStrategy("费用优先", "success", "不走收费路段，耗时最少"),
        2, new RouteStrategy("距离优先", "warning", "优先选择距离最短的路线"),
        4, new RouteStrategy("躲避拥堵", "info", "根据实时路况避开拥堵路段")
    );

    record RouteStrategy(String label, String tagType, String description) {}

    /**
     * 规划路线
     */
    public RoutePlanResponse planRoute(RoutePlanRequest request) {
        try {
            log.info("路线规划: {} -> {}", request.getOrigin(), request.getDestination());
            RoutePlanResponse response = planRouteFromAMap(request);
            log.info("规划完成，返回 {} 条路线", response.getRoutes() != null ? response.getRoutes().size() : 0);
            return response;
        } catch (Exception e) {
            log.error("高德 API 调用失败: {}", e.getMessage());
            throw new RuntimeException("路线规划失败: " + e.getMessage());
        }
    }

    /**
     * 调用高德地图 API 规划路线
     */
    private RoutePlanResponse planRouteFromAMap(RoutePlanRequest request) throws Exception {
        List<Double> originCoord = request.getOriginCoord();
        List<Double> destCoord = request.getDestCoord();
        
        log.info("收到坐标: originCoord={}, destCoord={}", originCoord, destCoord);

        // 如果没有坐标，先进行地理编码
        if (originCoord == null || originCoord.size() < 2) {
            originCoord = geocode(request.getOrigin());
        }
        
        // 两次地理编码之间加延迟，避免 QPS 限制
        if (destCoord == null || destCoord.size() < 2) {
            try { Thread.sleep(400); } catch (InterruptedException ignored) {}
            destCoord = geocode(request.getDestination());
        }

        if (originCoord == null || destCoord == null) {
            throw new RuntimeException("地理编码失败，无法获取坐标");
        }

        String originStr = originCoord.get(0) + "," + originCoord.get(1);
        String destStr = destCoord.get(0) + "," + destCoord.get(1);

        List<RouteOption> routes = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        // 获取要请求的策略列表，如果未指定则使用全部策略
        List<Integer> requestedStrategies = request.getStrategies();
        if (requestedStrategies == null || requestedStrategies.isEmpty()) {
            requestedStrategies = List.of(0, 1, 2, 4); // 按固定顺序
        }

        // 按策略顺序请求路线
        Map<Integer, RouteOption> routeMap = new LinkedHashMap<>();
        for (Integer strategyKey : requestedStrategies) {
            RouteStrategy strategy = STRATEGIES.get(strategyKey);
            if (strategy == null) continue;
            
            try {
                RouteOption route = fetchRoute(originStr, destStr, strategyKey, 
                    strategy, request.getOrigin(), request.getDestination());
                if (route != null) {
                    // 去重：高德 API 不同策略可能返回相同路线
                    String key = route.getLabel() + "-" + (route.getDistance() / 1000) + "-" + (route.getDuration() / 60);
                    if (!seen.contains(key)) {
                        seen.add(key);
                        routes.add(route);
                    }
                }
            } catch (Exception e) {
                log.warn("策略 {} 请求失败: {}", strategyKey, e.getMessage());
            }
        }

        if (routes.isEmpty()) {
            throw new RuntimeException("所有策略请求均失败");
        }
        return new RoutePlanResponse(routes);
    }

    /**
     * 地理编码（带重试）
     */
    private List<Double> geocode(String address) {
        int maxRetries = 3;
        int delay = 500;
        
        for (int i = 0; i < maxRetries; i++) {
            try {
                if (i > 0) {
                    Thread.sleep(delay);
                    delay *= 2; // 指数退避
                }
                
                String url = String.format(
                    "https://restapi.amap.com/v3/geocode/geo?key=%s&address=%s",
                    amapKey, URLEncoder.encode(address, StandardCharsets.UTF_8)
                );
                String response = restTemplate.getForObject(url, String.class);
                JsonNode root = objectMapper.readTree(response);
                
                String status = root.path("status").asText();
                String info = root.path("info").asText();
                
                if ("1".equals(status) && root.path("geocodes").size() > 0) {
                    String location = root.path("geocodes").get(0).path("location").asText();
                    String[] parts = location.split(",");
                    return List.of(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]));
                }
                
                // QPS 限制错误，继续重试
                if (info.contains("QPS") || info.contains("ENGINE_RESPONSE_DATA_ERROR")) {
                    log.warn("地理编码 QPS 限制，第{}次重试: address={}", i + 1, address);
                    continue;
                }
                
                log.warn("地理编码失败: address={}, info={}", address, info);
                return null;
                
            } catch (Exception e) {
                log.warn("地理编码异常，第{}次重试: address={}, error={}", i + 1, address, e.getMessage());
            }
        }
        
        log.error("地理编码最终失败: address={}", address);
        return null;
    }

    /**
     * 获取单条路线
     */
    private RouteOption fetchRoute(String origin, String dest, int strategy, 
                                   RouteStrategy strategyInfo, String originName, String destName) {
        try {
            String url = String.format(
                "https://restapi.amap.com/v3/direction/driving?key=%s&origin=%s&destination=%s&strategy=%d&extensions=all",
                amapKey, origin, dest, strategy
            );
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);

            String status = root.path("status").asText();
            if (!"1".equals(status)) {
                log.warn("策略{}请求失败: {}", strategy, root.path("info").asText());
                return null;
            }

            JsonNode route = root.path("route").path("paths").get(0);
            if (route == null) return null;

            int distance = route.path("distance").asInt();
            int duration = route.path("duration").asInt();
            int tolls = route.path("tolls").asInt(0);
            log.info("策略{} 解析结果: distance={}米, duration={}秒, tolls={}元", 
                strategy, distance, duration, tolls);

            // 提取路径点并生成站点
            List<double[]> pathPoints = new ArrayList<>();
            for (JsonNode step : route.path("steps")) {
                String polyline = step.path("polyline").asText();
                for (String point : polyline.split(";")) {
                    String[] coords = point.split(",");
                    pathPoints.add(new double[]{
                        Double.parseDouble(coords[0]), 
                        Double.parseDouble(coords[1])
                    });
                }
            }

            List<TrackPoint> trackPoints = generateTrackPoints(pathPoints, originName, destName);

            return new RouteOption(
                strategyInfo.label(), strategyInfo.tagType(), strategyInfo.description(),
                distance, duration, tolls, trackPoints
            );
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 根据路径点生成物流站点
     */
    private List<TrackPoint> generateTrackPoints(List<double[]> pathPoints, 
                                                  String originName, String destName) {
        List<TrackPoint> points = new ArrayList<>();
        if (pathPoints.isEmpty()) return points;

        // 减少中转站数量以降低 API 调用次数
        int stationCount = Math.min(4, Math.max(3, pathPoints.size() / 200));
        int step = pathPoints.size() / (stationCount - 1);

        List<Integer> indices = new ArrayList<>();
        indices.add(0);
        for (int i = 1; i < stationCount - 1; i++) {
            indices.add(Math.min(i * step, pathPoints.size() - 1));
        }
        indices.add(pathPoints.size() - 1);

        Set<String> seenLocations = new HashSet<>();

        for (int i = 0; i < indices.size(); i++) {
            int idx = indices.get(i);
            double[] coord = pathPoints.get(idx);
            boolean isStart = i == 0;
            boolean isEnd = i == indices.size() - 1;

            String location;
            String status;

            if (isStart) {
                location = originName;
                status = "发货站点";
            } else if (isEnd) {
                location = destName;
                status = "收货站点";
            } else {
                // 添加延迟避免 QPS 限制 (高德限制 3次/秒)
                try { Thread.sleep(400); } catch (InterruptedException ignored) {}
                location = reverseGeocodeWithRetry(coord[0], coord[1]);
                status = "中转站点";
                
                // 去重
                String locationKey = location.replace("转运中心", "");
                if (location.isEmpty() || seenLocations.contains(locationKey)) {
                    continue;
                }
                seenLocations.add(locationKey);
            }

            points.add(new TrackPoint(status, location, coord[0], coord[1]));
        }

        return points;
    }

    /**
     * 带重试的逆地理编码
     */
    private String reverseGeocodeWithRetry(double lng, double lat) {
        int maxRetries = 3;
        int delay = 500;
        
        for (int i = 0; i < maxRetries; i++) {
            String result = reverseGeocode(lng, lat);
            if (!result.isEmpty()) {
                return result;
            }
            // 指数退避
            try { Thread.sleep(delay); } catch (InterruptedException ignored) {}
            delay *= 2;
        }
        
        // 失败时返回基于坐标的默认名称
        return String.format("中转站(%.2f,%.2f)", lng, lat);
    }

    /**
     * 逆地理编码
     */
    private String reverseGeocode(double lng, double lat) {
        try {
            String url = String.format(
                "https://restapi.amap.com/v3/geocode/regeo?key=%s&location=%f,%f",
                amapKey, lng, lat
            );
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);

            String status = root.path("status").asText();
            if ("1".equals(status)) {
                JsonNode comp = root.path("regeocode").path("addressComponent");
                String city = comp.path("city").isArray() ? "" : comp.path("city").asText();
                String province = comp.path("province").asText();
                String district = comp.path("district").asText();
                String base = city.isEmpty() ? province : city;
                return (base + district + "转运中心").replace("[]", "");
            } else {
                String info = root.path("info").asText();
                log.warn("逆地理编码失败: location={},{}, info={}", lng, lat, info);
            }
        } catch (Exception e) {
            log.warn("逆地理编码异常: location={},{}, error={}", lng, lat, e.getMessage());
        }
        return "";
    }
}
