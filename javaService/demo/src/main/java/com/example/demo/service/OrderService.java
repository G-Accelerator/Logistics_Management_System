package com.example.demo.service;

import com.example.demo.dto.PageResult;
import com.example.demo.entity.Order;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class OrderService {
    
    private static final String[] STATUSES = {"pending", "shipping", "completed", "cancelled"};
    private static final String[] CARGO_TYPES = {"normal", "fragile", "cold", "dangerous"};
    private static final String[] CITIES = {
        "北京市朝阳区", "上海市浦东新区", "广州市天河区", "深圳市南山区",
        "杭州市西湖区", "成都市武侯区", "武汉市洪山区", "南京市鼓楼区"
    };
    private static final String[] NAMES = {"张三", "李四", "王五", "赵六", "钱七", "孙八"};
    private static final String[] CARGOS = {"电子产品", "服装鞋帽", "食品饮料", "家具建材", "医疗器械", "化工原料"};
    
    private final List<Order> mockOrders;
    private final Random random = new Random();
    
    public OrderService() {
        this.mockOrders = generateMockOrders(100);
    }
    
    public PageResult<Order> getOrders(int page, int pageSize, String orderNo, 
                                        String status, String cargoType) {
        List<Order> filtered = mockOrders.stream()
            .filter(o -> orderNo == null || orderNo.isEmpty() || o.getOrderNo().contains(orderNo))
            .filter(o -> status == null || status.isEmpty() || o.getStatus().equals(status))
            .filter(o -> cargoType == null || cargoType.isEmpty() || o.getCargoType().equals(cargoType))
            .toList();
        
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, filtered.size());
        
        List<Order> pageData = start < filtered.size() ? filtered.subList(start, end) : List.of();
        return new PageResult<>(pageData, filtered.size());
    }
    
    private List<Order> generateMockOrders(int count) {
        List<Order> orders = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        for (int i = 0; i < count; i++) {
            LocalDateTime date = LocalDateTime.now().minusDays(random.nextInt(30));
            String orderNo = String.format("ORD%s%04d", 
                date.format(DateTimeFormatter.ofPattern("yyyyMMdd")), i + 1);
            
            orders.add(new Order(
                (long) (i + 1),
                orderNo,
                CARGOS[random.nextInt(CARGOS.length)],
                CARGO_TYPES[random.nextInt(CARGO_TYPES.length)],
                CITIES[random.nextInt(CITIES.length)],
                CITIES[random.nextInt(CITIES.length)],
                NAMES[random.nextInt(NAMES.length)],
                NAMES[random.nextInt(NAMES.length)],
                "138" + String.format("%08d", random.nextInt(100000000)),
                "139" + String.format("%08d", random.nextInt(100000000)),
                STATUSES[random.nextInt(STATUSES.length)],
                date.format(formatter)
            ));
        }
        return orders;
    }
}
