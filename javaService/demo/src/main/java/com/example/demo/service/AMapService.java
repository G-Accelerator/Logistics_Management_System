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

    @Value("${amap.key:}")
    private String amapKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 路线策略
    private static final Map<Integer, RouteStrategy> STRATEGIES = Map.of(
        0, new RouteStrategy("最快路线", "primary", "优先选择耗时最短的路线"),
        1, new RouteStrategy("最短路线", "success", "优先选择距离最短的路线"),
        2, new RouteStrategy("最省钱", "warning", "优先选择收费最少的路线"),
        4, new RouteStrategy("躲避拥堵", "info", "根据实时路况避开拥堵路段")
    );

    record RouteStrategy(String label, String tagType, String description) {}

    /**
     * 规划路线
     */
    public RoutePlanResponse planRoute(RoutePlanRequest request) {
        if (amapKey == null || amapKey.isEmpty()) {
            log.warn("未配置高德 API Key，使用模拟数据");
            return generateMockRoutes(request);
        }

        try {
            log.info("路线规划: {} -> {}", request.getOrigin(), request.getDestination());
            RoutePlanResponse response = planRouteFromAMap(request);
            log.info("规划完成，返回 {} 条路线", response.getRoutes() != null ? response.getRoutes().size() : 0);
            return response;
        } catch (Exception e) {
            log.error("高德 API 调用失败: {}", e.getMessage());
            return generateMockRoutes(request);
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
        if (destCoord == null || destCoord.size() < 2) {
            destCoord = geocode(request.getDestination());
        }

        if (originCoord == null || destCoord == null) {
            log.warn("地理编码失败，origin={}, dest={}", originCoord, destCoord);
            return generateMockRoutes(request);
        }

        String originStr = originCoord.get(0) + "," + originCoord.get(1);
        String destStr = destCoord.get(0) + "," + destCoord.get(1);

        List<RouteOption> routes = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        // 获取要请求的策略列表，如果未指定则使用全部策略
        List<Integer> requestedStrategies = request.getStrategies();
        if (requestedStrategies == null || requestedStrategies.isEmpty()) {
            requestedStrategies = new ArrayList<>(STRATEGIES.keySet());
        }

        // 只请求指定策略的路线
        for (Integer strategyKey : requestedStrategies) {
            RouteStrategy strategy = STRATEGIES.get(strategyKey);
            if (strategy == null) continue;
            
            try {
                RouteOption route = fetchRoute(originStr, destStr, strategyKey, 
                    strategy, request.getOrigin(), request.getDestination());
                if (route != null) {
                    String key = (route.getDistance() / 100) + "-" + (route.getDuration() / 60);
                    if (!seen.contains(key)) {
                        seen.add(key);
                        routes.add(route);
                    }
                }
            } catch (Exception ignored) {}
        }

        if (routes.isEmpty()) {
            log.warn("所有策略请求失败，返回模拟数据");
            return generateMockRoutes(request);
        }
        return new RoutePlanResponse(routes);
    }

    /**
     * 地理编码
     */
    private List<Double> geocode(String address) {
        try {
            String url = String.format(
                "https://restapi.amap.com/v3/geocode/geo?key=%s&address=%s",
                amapKey, URLEncoder.encode(address, StandardCharsets.UTF_8)
            );
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            
            String status = root.path("status").asText();
            if (!"1".equals(status)) {
                log.warn("地理编码失败: address={}, info={}", address, root.path("info").asText());
                return null;
            }
            
            if (root.path("geocodes").size() > 0) {
                String location = root.path("geocodes").get(0).path("location").asText();
                String[] parts = location.split(",");
                return List.of(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]));
            }
            log.warn("地理编码无结果: address={}", address);
        } catch (Exception e) {
            log.error("地理编码异常: address={}, error={}", address, e.getMessage());
        }
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
                distance, duration, trackPoints
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

        int stationCount = Math.min(8, Math.max(4, pathPoints.size() / 100));
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
                location = reverseGeocode(coord[0], coord[1]);
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

            if ("1".equals(root.path("status").asText())) {
                JsonNode comp = root.path("regeocode").path("addressComponent");
                String city = comp.path("city").isArray() ? "" : comp.path("city").asText();
                String province = comp.path("province").asText();
                String district = comp.path("district").asText();
                String base = city.isEmpty() ? province : city;
                return (base + district + "转运中心").replace("[]", "");
            }
        } catch (Exception ignored) {}
        return "";
    }

    /**
     * 生成模拟路线数据
     */
    private RoutePlanResponse generateMockRoutes(RoutePlanRequest request) {
        List<RouteOption> routes = new ArrayList<>();
        Random random = new Random();

        // 基础距离和时间（模拟）
        int baseDistance = 800000 + random.nextInt(400000); // 800-1200km
        int baseTime = baseDistance / 80 * 3600 / 1000; // 按80km/h估算

        // 模拟城市坐标
        Map<String, double[]> cityCoords = Map.of(
            "北京", new double[]{116.407526, 39.904030},
            "上海", new double[]{121.473701, 31.230416},
            "广州", new double[]{113.264385, 23.129112},
            "深圳", new double[]{114.057868, 22.543099},
            "杭州", new double[]{120.155070, 30.274084},
            "成都", new double[]{104.066541, 30.572269}
        );

        double[] originCoord = findCityCoord(request.getOrigin(), cityCoords, 
            request.getOriginCoord(), new double[]{116.407526, 39.904030});
        double[] destCoord = findCityCoord(request.getDestination(), cityCoords,
            request.getDestCoord(), new double[]{121.473701, 31.230416});

        // 不同策略对应不同的中转路线
        String[][] transitRoutes = {
            {"济南市历下区", "徐州市云龙区", "南京市玄武区"},           // 最快路线 - 高速直达
            {"石家庄市长安区", "郑州市金水区", "合肥市蜀山区"},         // 最短路线 - 走内陆
            {"天津市和平区", "济宁市任城区", "淮安市清江浦区", "苏州市姑苏区"}, // 最省钱 - 避开收费站多绕路
            {"保定市竞秀区", "德州市德城区", "泰安市泰山区", "临沂市兰山区", "连云港市海州区"} // 躲避拥堵 - 走小城市
        };

        // 生成不同策略的路线
        Object[][] strategies = {
            {"最快路线", "primary", "优先选择耗时最短的路线", 1.0, 1.0, 0},
            {"最短路线", "success", "优先选择距离最短的路线", 0.9, 1.1, 1},
            {"最省钱", "warning", "优先选择收费最少的路线", 1.1, 1.15, 2},
            {"躲避拥堵", "info", "根据实时路况避开拥堵路段", 1.05, 0.95, 3}
        };

        for (Object[] s : strategies) {
            int distance = (int) (baseDistance * (double) s[3]);
            int duration = (int) (baseTime * (double) s[4]);
            int routeIndex = (int) s[5];
            
            List<TrackPoint> trackPoints = generateMockTrackPoints(
                request.getOrigin(), request.getDestination(),
                originCoord, destCoord, transitRoutes[routeIndex]
            );

            routes.add(new RouteOption(
                (String) s[0], (String) s[1], (String) s[2],
                distance, duration, trackPoints
            ));
        }

        return new RoutePlanResponse(routes);
    }

    private double[] findCityCoord(String address, Map<String, double[]> cityCoords,
                                   List<Double> providedCoord, double[] defaultCoord) {
        if (providedCoord != null && providedCoord.size() >= 2) {
            return new double[]{providedCoord.get(0), providedCoord.get(1)};
        }
        for (Map.Entry<String, double[]> entry : cityCoords.entrySet()) {
            if (address.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return defaultCoord;
    }

    private List<TrackPoint> generateMockTrackPoints(String origin, String dest,
                                                      double[] originCoord, double[] destCoord,
                                                      String[] transitCities) {
        List<TrackPoint> points = new ArrayList<>();
        
        // 起点
        points.add(new TrackPoint("发货站点", origin, originCoord[0], originCoord[1]));

        // 中转站点（根据不同路线策略使用不同的中转城市）
        int transitCount = transitCities.length;
        
        for (int i = 0; i < transitCount; i++) {
            double ratio = (i + 1.0) / (transitCount + 1);
            double lng = originCoord[0] + (destCoord[0] - originCoord[0]) * ratio;
            double lat = originCoord[1] + (destCoord[1] - originCoord[1]) * ratio;
            // 添加一些随机偏移，让路线看起来不同
            lng += (Math.random() - 0.5) * 0.5;
            lat += (Math.random() - 0.5) * 0.3;
            points.add(new TrackPoint("中转站点", transitCities[i] + "转运中心", lng, lat));
        }

        // 终点
        points.add(new TrackPoint("收货站点", dest, destCoord[0], destCoord[1]));

        return points;
    }
}
