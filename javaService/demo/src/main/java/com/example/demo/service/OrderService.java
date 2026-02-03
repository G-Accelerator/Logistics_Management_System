package com.example.demo.service;

import com.example.demo.dto.PageResult;
import com.example.demo.dto.RoutePlanResponse.TrackPoint;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private static final String DATA_FILE = "data/orders.json";

    private final ObjectMapper objectMapper;
    private final List<Order> orders = Collections.synchronizedList(new ArrayList<>());
    private final AtomicLong idGenerator = new AtomicLong(1);

    public OrderService() {
        this.objectMapper = new ObjectMapper();
        // 注册 Java 8 日期时间模块，支持 LocalDateTime 序列化
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

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
                                       String status, String cargoType, String cargoName,
                                       String expressCompany, String senderName, String receiverName,
                                       String receiverPhone) {
        List<Order> filtered = orders.stream()
            .filter(o -> orderNo == null || orderNo.isEmpty() || o.getOrderNo().contains(orderNo))
            .filter(o -> status == null || status.isEmpty() || o.getStatus().equals(status))
            .filter(o -> cargoType == null || cargoType.isEmpty() || o.getCargoType().equals(cargoType))
            .filter(o -> cargoName == null || cargoName.isEmpty() || 
                (o.getCargoName() != null && o.getCargoName().contains(cargoName)))
            .filter(o -> expressCompany == null || expressCompany.isEmpty() || 
                expressCompany.equals(o.getExpressCompany()))
            .filter(o -> senderName == null || senderName.isEmpty() || 
                (o.getSenderName() != null && o.getSenderName().contains(senderName)))
            .filter(o -> receiverName == null || receiverName.isEmpty() || 
                (o.getReceiverName() != null && o.getReceiverName().contains(receiverName)))
            .filter(o -> receiverPhone == null || receiverPhone.isEmpty() || 
                (o.getReceiverPhone() != null && o.getReceiverPhone().equals(receiverPhone)))
            .toList();

        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, filtered.size());

        List<Order> pageData = start < filtered.size() ? filtered.subList(start, end) : List.of();
        return new PageResult<>(pageData, filtered.size());
    }

    /**
     * 根据订单号获取订单
     */
    public Order getOrder(String orderNo) {
        return orders.stream()
            .filter(o -> o.getOrderNo().equals(orderNo))
            .findFirst()
            .orElse(null);
    }

    /**
     * 根据订单号获取站点数据
     */
    public List<TrackPoint> getTrackPoints(String orderNo) {
        Order order = getOrder(orderNo);
        if (order == null || order.getTrackPoints() == null) {
            return List.of();
        }
        return order.getTrackPoints();
    }

    /**
     * 更新订单（用于状态变更后持久化）
     */
    public Order updateOrder(Order order) {
        if (order == null || order.getOrderNo() == null) {
            return null;
        }
        // 订单已在内存中被修改，只需保存到文件
        saveToFile();
        log.info("更新订单: {}", order.getOrderNo());
        return order;
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
            try (InputStreamReader reader = new InputStreamReader(
                    new FileInputStream(file), StandardCharsets.UTF_8)) {
                List<Order> loaded = objectMapper.readValue(reader, new TypeReference<List<Order>>() {});
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
            // 使用 UTF-8 编码写入，避免中文乱码
            try (OutputStreamWriter writer = new OutputStreamWriter(
                    new FileOutputStream(file), StandardCharsets.UTF_8)) {
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(writer, orders);
            }
        } catch (IOException e) {
            log.error("保存订单数据失败: {}", e.getMessage());
        }
    }

    /**
     * 获取订单统计
     */
    public Map<String, Object> getStats() {
        long total = orders.size();
        long pending = orders.stream().filter(o -> "pending".equals(o.getStatus())).count();
        long shipping = orders.stream().filter(o -> "shipping".equals(o.getStatus())).count();
        long completed = orders.stream().filter(o -> "completed".equals(o.getStatus())).count();
        
        return Map.of(
            "total", total,
            "pending", pending,
            "shipping", shipping,
            "completed", completed
        );
    }

    /**
     * 根据手机号获取订单统计（买家专用）
     */
    public Map<String, Object> getStatsByPhone(String phone) {
        List<Order> filtered = orders.stream()
            .filter(o -> phone.equals(o.getReceiverPhone()))
            .toList();
        
        long total = filtered.size();
        long pending = filtered.stream().filter(o -> "pending".equals(o.getStatus())).count();
        long shipping = filtered.stream().filter(o -> "shipping".equals(o.getStatus())).count();
        long completed = filtered.stream().filter(o -> "completed".equals(o.getStatus())).count();
        
        return Map.of(
            "total", total,
            "pending", pending,
            "shipping", shipping,
            "completed", completed
        );
    }
}
