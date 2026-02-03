package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.BatchOrderRequest;
import com.example.demo.dto.BatchResult;
import com.example.demo.dto.PageResult;
import com.example.demo.dto.RoutePlanRequest;
import com.example.demo.dto.RoutePlanResponse;
import com.example.demo.dto.RoutePlanResponse.TrackPoint;
import com.example.demo.dto.StationInfo;
import com.example.demo.entity.OperationLog;
import com.example.demo.entity.Order;
import com.example.demo.service.AMapService;
import com.example.demo.service.OrderService;
import com.example.demo.service.OrderStatusService;
import com.example.demo.service.StationStatusService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {
    
    private final OrderService orderService;
    private final AMapService aMapService;
    private final OrderStatusService orderStatusService;
    private final StationStatusService stationStatusService;
    
    public OrderController(OrderService orderService, AMapService aMapService, 
                          OrderStatusService orderStatusService,
                          StationStatusService stationStatusService) {
        this.orderService = orderService;
        this.aMapService = aMapService;
        this.orderStatusService = orderStatusService;
        this.stationStatusService = stationStatusService;
    }
    
    /**
     * 获取订单统计
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        Map<String, Object> stats = orderService.getStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResult<Order>>> getOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String cargoType,
            @RequestParam(required = false) String cargoName,
            @RequestParam(required = false) String expressCompany,
            @RequestParam(required = false) String senderName,
            @RequestParam(required = false) String receiverName) {
        PageResult<Order> result = orderService.getOrders(page, pageSize, orderNo, status, cargoType, 
            cargoName, expressCompany, senderName, receiverName);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 创建订单
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Order>> createOrder(@RequestBody Order order) {
        Order created = orderService.createOrder(order);
        return ResponseEntity.ok(ApiResponse.success("订单创建成功", created));
    }

    /**
     * 获取单个订单
     */
    @GetMapping("/{orderNo}")
    public ResponseEntity<ApiResponse<Order>> getOrder(@PathVariable String orderNo) {
        Order order = orderService.getOrder(orderNo);
        if (order == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, "订单不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    /**
     * 删除订单
     */
    @DeleteMapping("/{orderNo}")
    public ResponseEntity<ApiResponse<Boolean>> deleteOrder(@PathVariable String orderNo) {
        boolean deleted = orderService.deleteOrder(orderNo);
        if (deleted) {
            return ResponseEntity.ok(ApiResponse.success("订单删除成功", true));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, "订单不存在"));
        }
    }

    /**
     * 获取订单站点数据
     */
    @GetMapping("/{orderNo}/track-points")
    public ResponseEntity<ApiResponse<List<TrackPoint>>> getTrackPoints(@PathVariable String orderNo) {
        List<TrackPoint> trackPoints = orderService.getTrackPoints(orderNo);
        return ResponseEntity.ok(ApiResponse.success(trackPoints));
    }

    /**
     * 路线规划
     */
    @PostMapping("/plan-route")
    public ResponseEntity<ApiResponse<RoutePlanResponse>> planRoute(@RequestBody RoutePlanRequest request) {
        RoutePlanResponse response = aMapService.planRoute(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ==================== 状态变更 API ====================

    /**
     * 发货操作
     * PUT /api/orders/{orderNo}/ship
     */
    @PutMapping("/{orderNo}/ship")
    public ResponseEntity<ApiResponse<Order>> shipOrder(@PathVariable String orderNo) {
        try {
            Order order = orderStatusService.ship(orderNo);
            return ResponseEntity.ok(ApiResponse.success("发货成功", order));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 签收操作
     * PUT /api/orders/{orderNo}/receive
     */
    @PutMapping("/{orderNo}/receive")
    public ResponseEntity<ApiResponse<Order>> receiveOrder(@PathVariable String orderNo) {
        try {
            Order order = orderStatusService.receive(orderNo);
            return ResponseEntity.ok(ApiResponse.success("签收成功", order));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(400, e.getMessage()));
        }
    }

    /**
     * 取消操作
     * PUT /api/orders/{orderNo}/cancel
     */
    @PutMapping("/{orderNo}/cancel")
    public ResponseEntity<ApiResponse<Order>> cancelOrder(@PathVariable String orderNo) {
        try {
            Order order = orderStatusService.cancel(orderNo);
            return ResponseEntity.ok(ApiResponse.success("取消成功", order));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(400, e.getMessage()));
        }
    }

    // ==================== 批量操作 API ====================

    /**
     * 批量发货
     * PUT /api/orders/batch/ship
     */
    @PutMapping("/batch/ship")
    public ResponseEntity<ApiResponse<BatchResult>> batchShip(@RequestBody BatchOrderRequest request) {
        BatchResult result = orderStatusService.batchShip(request.getOrderNos());
        if (result.isSuccess()) {
            return ResponseEntity.ok(ApiResponse.success(result.getMessage(), result));
        } else if (result.getSuccessCount() > 0) {
            // 部分成功
            return ResponseEntity.status(HttpStatus.MULTI_STATUS)
                    .body(new ApiResponse<>(207, result.getMessage(), result));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(400, result.getMessage()));
        }
    }

    /**
     * 批量签收
     * PUT /api/orders/batch/receive
     */
    @PutMapping("/batch/receive")
    public ResponseEntity<ApiResponse<BatchResult>> batchReceive(@RequestBody BatchOrderRequest request) {
        BatchResult result = orderStatusService.batchReceive(request.getOrderNos());
        if (result.isSuccess()) {
            return ResponseEntity.ok(ApiResponse.success(result.getMessage(), result));
        } else if (result.getSuccessCount() > 0) {
            // 部分成功
            return ResponseEntity.status(HttpStatus.MULTI_STATUS)
                    .body(new ApiResponse<>(207, result.getMessage(), result));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(400, result.getMessage()));
        }
    }

    // ==================== 操作日志 API ====================

    /**
     * 获取订单操作日志
     * GET /api/orders/{orderNo}/logs
     */
    @GetMapping("/{orderNo}/logs")
    public ResponseEntity<ApiResponse<List<OperationLog>>> getOrderLogs(@PathVariable String orderNo) {
        // 先检查订单是否存在
        Order order = orderService.getOrder(orderNo);
        if (order == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, String.format("订单 %s 不存在", orderNo)));
        }
        
        List<OperationLog> logs = orderStatusService.getOperationLogs(orderNo);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    // ==================== 站点状态 API ====================

    /**
     * 获取订单站点状态
     * GET /api/orders/{orderNo}/stations
     */
    @GetMapping("/{orderNo}/stations")
    public ResponseEntity<ApiResponse<List<StationInfo>>> getStationStatus(@PathVariable String orderNo) {
        try {
            List<StationInfo> stations = stationStatusService.getStationStatus(orderNo);
            return ResponseEntity.ok(ApiResponse.success(stations));
        } catch (IllegalArgumentException e) {
            int statusCode = e.getMessage().contains("不存在") ? 404 : 400;
            return ResponseEntity.status(statusCode == 404 ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(statusCode, e.getMessage()));
        }
    }

    /**
     * 标记单个站点到达
     * PUT /api/orders/{orderNo}/stations/{stationIndex}/arrive
     */
    @PutMapping("/{orderNo}/stations/{stationIndex}/arrive")
    public ResponseEntity<ApiResponse<Map<String, Object>>> markStationArrived(
            @PathVariable String orderNo,
            @PathVariable int stationIndex) {
        try {
            StationInfo station = stationStatusService.markStationArrived(orderNo, stationIndex);
            Map<String, Object> result = Map.of(
                "success", true,
                "message", "站点到达标记成功",
                "stationIndex", station.getIndex(),
                "arrivalTime", station.getArrivalTime() != null ? station.getArrivalTime() : ""
            );
            return ResponseEntity.ok(ApiResponse.success("站点到达标记成功", result));
        } catch (IllegalArgumentException e) {
            int statusCode = e.getMessage().contains("不存在") ? 404 : 400;
            Map<String, Object> result = Map.of(
                "success", false,
                "message", e.getMessage()
            );
            return ResponseEntity.status(statusCode == 404 ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(statusCode, e.getMessage()));
        }
    }

    /**
     * 标记全部站点到达
     * PUT /api/orders/{orderNo}/stations/arrive-all
     */
    @PutMapping("/{orderNo}/stations/arrive-all")
    public ResponseEntity<ApiResponse<Map<String, Object>>> markAllStationsArrived(@PathVariable String orderNo) {
        try {
            int arrivedCount = stationStatusService.markAllStationsArrived(orderNo);
            Map<String, Object> result = Map.of(
                "success", true,
                "message", arrivedCount > 0 
                    ? String.format("已标记 %d 个站点到达", arrivedCount)
                    : "所有站点已到达",
                "arrivedCount", arrivedCount
            );
            return ResponseEntity.ok(ApiResponse.success(result.get("message").toString(), result));
        } catch (IllegalArgumentException e) {
            int statusCode = e.getMessage().contains("不存在") ? 404 : 400;
            Map<String, Object> result = Map.of(
                "success", false,
                "message", e.getMessage(),
                "arrivedCount", 0
            );
            return ResponseEntity.status(statusCode == 404 ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(statusCode, e.getMessage()));
        }
    }

    /**
     * 标记到达至指定站点
     * PUT /api/orders/{orderNo}/stations/arrive-to/{targetIndex}
     */
    @PutMapping("/{orderNo}/stations/arrive-to/{targetIndex}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> markStationsArrivedTo(
            @PathVariable String orderNo,
            @PathVariable int targetIndex) {
        try {
            int arrivedCount = stationStatusService.markStationsArrivedTo(orderNo, targetIndex);
            Map<String, Object> result = Map.of(
                "success", true,
                "message", arrivedCount > 0 
                    ? String.format("已标记 %d 个站点到达至站点 %d", arrivedCount, targetIndex)
                    : "目标站点及之前站点已全部到达",
                "arrivedCount", arrivedCount,
                "targetIndex", targetIndex
            );
            return ResponseEntity.ok(ApiResponse.success(result.get("message").toString(), result));
        } catch (IllegalArgumentException e) {
            int statusCode = e.getMessage().contains("不存在") ? 404 : 400;
            Map<String, Object> result = Map.of(
                "success", false,
                "message", e.getMessage(),
                "arrivedCount", 0
            );
            return ResponseEntity.status(statusCode == 404 ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(statusCode, e.getMessage()));
        }
    }
}
