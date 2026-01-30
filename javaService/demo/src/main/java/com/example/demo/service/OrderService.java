package com.example.demo.service;

import com.example.demo.dto.PageResult;
import com.example.demo.dto.RoutePlanResponse.TrackPoint;
import com.example.demo.entity.Order;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private static final String DATA_FILE = "data/orders.json";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<Order> orders = Collections.synchronizedList(new ArrayList<>());
    private final AtomicLong idGenerator = new AtomicLong(1);

    @PostConstruct
    public void init() {
        loadFromFile();
        log.info("订单服务初始化完成，已加载 {} 条订单", orders.size());
    }

    /**
     * 创建订单
     */
    public Order createOrder(Order order) {
        order.setId(idGenerator.getAndIncrement());
        order.setOrderNo(generateOrderNo());
        order.setStatus("pending");
        order.setCreateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        
        orders.add(0, order);
        saveToFile();
        
        log.info("创建订单: {}", order.getOrderNo());
        return order;
    }

    /**
     * 查询订单列表
     */
    public PageResult<Order> getOrders(int page, int pageSize, String orderNo,
                                       String status, String cargoType) {
        List<Order> filtered = orders.stream()
            .filter(o -> orderNo == null || orderNo.isEmpty() || o.getOrderNo().contains(orderNo))
            .filter(o -> status == null || status.isEmpty() || o.getStatus().equals(status))
            .filter(o -> cargoType == null || cargoType.isEmpty() || o.getCargoType().equals(cargoType))
            .toList();

        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, filtered.size());

        List<Order> pageData = start < filtered.size() ? filtered.subList(start, end) : List.of();
        return new PageResult<>(pageData, filtered.size());
    }

    /**
     * 根据订单号获取站点数据
     */
    public List<TrackPoint> getTrackPoints(String orderNo) {
        Order order = orders.stream()
            .filter(o -> o.getOrderNo().equals(orderNo))
            .findFirst()
            .orElse(null);

        if (order == null || order.getTrackPoints() == null) {
            return List.of();
        }
        return order.getTrackPoints();
    }

    /**
     * 删除订单
     */
    public boolean deleteOrder(String orderNo) {
        boolean removed = orders.removeIf(o -> o.getOrderNo().equals(orderNo));
        if (removed) {
            saveToFile();
            log.info("删除订单: {}", orderNo);
        }
        return removed;
    }

    private String generateOrderNo() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = String.format("%04d", new Random().nextInt(10000));
        return "ORD" + date + random;
    }

    private void loadFromFile() {
        File file = new File(DATA_FILE);
        if (file.exists()) {
            try {
                List<Order> loaded = objectMapper.readValue(file, new TypeReference<List<Order>>() {});
                orders.addAll(loaded);
                long maxId = orders.stream().mapToLong(Order::getId).max().orElse(0);
                idGenerator.set(maxId + 1);
            } catch (IOException e) {
                log.error("加载订单数据失败: {}", e.getMessage());
            }
        }
    }

    private synchronized void saveToFile() {
        try {
            File file = new File(DATA_FILE);
            file.getParentFile().mkdirs();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, orders);
        } catch (IOException e) {
            log.error("保存订单数据失败: {}", e.getMessage());
        }
    }
}
