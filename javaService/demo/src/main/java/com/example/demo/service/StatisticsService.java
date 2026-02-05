package com.example.demo.service;

import com.example.demo.dto.DistributionData;
import com.example.demo.dto.StatisticsOverview;
import com.example.demo.dto.TrendData;
import com.example.demo.entity.Order;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    private static final Logger log = LoggerFactory.getLogger(StatisticsService.class);
    private static final String DATA_FILE = "data/orders.json";

    private final ObjectMapper objectMapper;

    public StatisticsService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * 加载订单数据
     */
    private List<Order> loadOrders() {
        File file = new File(DATA_FILE);
        if (file.exists()) {
            try (InputStreamReader reader = new InputStreamReader(
                    new FileInputStream(file), StandardCharsets.UTF_8)) {
                return objectMapper.readValue(reader, new TypeReference<List<Order>>() {});
            } catch (IOException e) {
                log.error("加载订单数据失败: {}", e.getMessage());
            }
        }
        return new ArrayList<>();
    }


    /**
     * 获取统计概览
     */
    public StatisticsOverview getOverview() {
        List<Order> orders = loadOrders();
        
        long totalOrders = orders.size();
        long pendingOrders = orders.stream()
            .filter(o -> "pending".equals(o.getStatus()))
            .count();
        long completedOrders = orders.stream()
            .filter(o -> "completed".equals(o.getStatus()))
            .count();
        
        // 计算完成率
        double completionRate = totalOrders > 0 
            ? Math.round(completedOrders * 10000.0 / totalOrders) / 100.0 
            : 0.0;
        
        // 计算本月订单数
        YearMonth currentMonth = YearMonth.now();
        long monthOrders = orders.stream()
            .filter(o -> isInMonth(o.getCreateTime(), currentMonth))
            .count();
        
        // 计算上月订单数（用于环比）
        YearMonth lastMonth = currentMonth.minusMonths(1);
        long lastMonthOrders = orders.stream()
            .filter(o -> isInMonth(o.getCreateTime(), lastMonth))
            .count();
        
        // 计算月环比增长率
        double monthGrowth = lastMonthOrders > 0 
            ? Math.round((monthOrders - lastMonthOrders) * 10000.0 / lastMonthOrders) / 100.0 
            : (monthOrders > 0 ? 100.0 : 0.0);
        
        return new StatisticsOverview(totalOrders, monthOrders, pendingOrders, completionRate, monthGrowth);
    }

    /**
     * 获取订单趋势
     */
    public TrendData getTrend(int days) {
        List<Order> orders = loadOrders();
        LocalDate today = LocalDate.now();
        
        List<String> dates = new ArrayList<>();
        List<Long> counts = new ArrayList<>();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dateStr = date.format(formatter);
            dates.add(dateStr);
            
            long count = orders.stream()
                .filter(o -> isOnDate(o.getCreateTime(), date))
                .count();
            counts.add(count);
        }
        
        return new TrendData(dates, counts);
    }

    /**
     * 获取状态分布
     */
    public Map<String, Long> getStatusDistribution() {
        List<Order> orders = loadOrders();
        
        Map<String, Long> distribution = new LinkedHashMap<>();
        distribution.put("pending", orders.stream().filter(o -> "pending".equals(o.getStatus())).count());
        distribution.put("shipping", orders.stream().filter(o -> "shipping".equals(o.getStatus())).count());
        distribution.put("completed", orders.stream().filter(o -> "completed".equals(o.getStatus())).count());
        distribution.put("cancelled", orders.stream().filter(o -> "cancelled".equals(o.getStatus())).count());
        
        return distribution;
    }


    /**
     * 获取热门城市
     * @param type "origin" 发货城市 或 "destination" 收货城市
     * @param limit 返回数量限制
     */
    public DistributionData getTopCities(String type, int limit) {
        List<Order> orders = loadOrders();
        
        Map<String, Long> cityCount;
        if ("origin".equals(type)) {
            cityCount = orders.stream()
                .filter(o -> o.getOrigin() != null && !o.getOrigin().isEmpty())
                .collect(Collectors.groupingBy(
                    o -> extractCity(o.getOrigin()),
                    Collectors.counting()
                ));
        } else {
            cityCount = orders.stream()
                .filter(o -> o.getDestination() != null && !o.getDestination().isEmpty())
                .collect(Collectors.groupingBy(
                    o -> extractCity(o.getDestination()),
                    Collectors.counting()
                ));
        }
        
        // 按数量降序排列，取前limit个
        List<Map.Entry<String, Long>> sorted = cityCount.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(limit)
            .toList();
        
        List<String> cities = sorted.stream().map(Map.Entry::getKey).toList();
        List<Long> counts = sorted.stream().map(Map.Entry::getValue).toList();
        
        return new DistributionData(cities, counts);
    }

    // 快递公司名称映射
    private static final Map<String, String> EXPRESS_COMPANY_NAMES = Map.ofEntries(
        Map.entry("sf", "顺丰速运"),
        Map.entry("yd", "韵达快递"),
        Map.entry("yto", "圆通速递"),
        Map.entry("yt", "圆通速递"),
        Map.entry("zto", "中通快递"),
        Map.entry("zt", "中通快递"),
        Map.entry("sto", "申通快递"),
        Map.entry("st", "申通快递"),
        Map.entry("jd", "京东物流"),
        Map.entry("ems", "EMS"),
        Map.entry("db", "德邦快递"),
        Map.entry("yunda", "韵达快递")
    );

    /**
     * 获取快递公司统计
     */
    public DistributionData getExpressCompanies() {
        List<Order> orders = loadOrders();
        
        Map<String, Long> companyCount = orders.stream()
            .filter(o -> o.getExpressCompany() != null && !o.getExpressCompany().isEmpty())
            .collect(Collectors.groupingBy(
                Order::getExpressCompany,
                Collectors.counting()
            ));
        
        // 按数量降序排列
        List<Map.Entry<String, Long>> sorted = companyCount.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .toList();
        
        // 将快递公司代码转换为中文名称
        List<String> companies = sorted.stream()
            .map(e -> EXPRESS_COMPANY_NAMES.getOrDefault(e.getKey(), e.getKey()))
            .toList();
        List<Long> counts = sorted.stream().map(Map.Entry::getValue).toList();
        
        return new DistributionData(companies, counts);
    }

    // ==================== 辅助方法 ====================

    /**
     * 判断订单创建时间是否在指定月份
     */
    private boolean isInMonth(String createTime, YearMonth month) {
        if (createTime == null || createTime.isEmpty()) {
            return false;
        }
        try {
            // 格式: "yyyy-MM-dd HH:mm"
            LocalDateTime dateTime = LocalDateTime.parse(createTime, 
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            return YearMonth.from(dateTime).equals(month);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断订单创建时间是否在指定日期
     */
    private boolean isOnDate(String createTime, LocalDate date) {
        if (createTime == null || createTime.isEmpty()) {
            return false;
        }
        try {
            LocalDateTime dateTime = LocalDateTime.parse(createTime, 
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            return dateTime.toLocalDate().equals(date);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从地址中提取城市名
     * 例如: "北京市朝阳区xxx" -> "北京市"
     */
    private String extractCity(String address) {
        if (address == null || address.isEmpty()) {
            return "未知";
        }
        // 简单处理：取前3-4个字符作为城市名
        // 处理直辖市和普通城市
        if (address.contains("市")) {
            int idx = address.indexOf("市");
            return address.substring(0, Math.min(idx + 1, address.length()));
        }
        // 如果没有"市"，取前4个字符
        return address.substring(0, Math.min(4, address.length()));
    }
}
