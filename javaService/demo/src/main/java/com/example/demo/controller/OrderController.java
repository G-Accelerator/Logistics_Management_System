package com.example.demo.controller;

import com.example.demo.dto.PageResult;
import com.example.demo.dto.RoutePlanRequest;
import com.example.demo.dto.RoutePlanResponse;
import com.example.demo.dto.RoutePlanResponse.TrackPoint;
import com.example.demo.entity.Order;
import com.example.demo.service.AMapService;
import com.example.demo.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {
    
    private final OrderService orderService;
    private final AMapService aMapService;
    
    public OrderController(OrderService orderService, AMapService aMapService) {
        this.orderService = orderService;
        this.aMapService = aMapService;
    }
    
    @GetMapping
    public PageResult<Order> getOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String cargoType) {
        return orderService.getOrders(page, pageSize, orderNo, status, cargoType);
    }

    /**
     * 创建订单
     */
    @PostMapping
    public Order createOrder(@RequestBody Order order) {
        return orderService.createOrder(order);
    }

    /**
     * 获取单个订单
     */
    @GetMapping("/{orderNo}")
    public Order getOrder(@PathVariable String orderNo) {
        return orderService.getOrder(orderNo);
    }

    /**
     * 删除订单
     */
    @DeleteMapping("/{orderNo}")
    public boolean deleteOrder(@PathVariable String orderNo) {
        return orderService.deleteOrder(orderNo);
    }

    /**
     * 获取订单站点数据
     */
    @GetMapping("/{orderNo}/track-points")
    public List<TrackPoint> getTrackPoints(@PathVariable String orderNo) {
        return orderService.getTrackPoints(orderNo);
    }

    /**
     * 路线规划
     */
    @PostMapping("/plan-route")
    public RoutePlanResponse planRoute(@RequestBody RoutePlanRequest request) {
        return aMapService.planRoute(request);
    }
}
