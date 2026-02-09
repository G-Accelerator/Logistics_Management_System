package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.BatchOrderRequest;
import com.example.demo.dto.BatchResult;
import com.example.demo.dto.ExportRequest;
import com.example.demo.dto.ImportError;
import com.example.demo.dto.ImportResultDTO;
import com.example.demo.dto.PageResult;
import com.example.demo.dto.RoutePlanRequest;
import com.example.demo.dto.RoutePlanResponse;
import com.example.demo.dto.RoutePlanResponse.TrackPoint;
import com.example.demo.dto.ShipRequest;
import com.example.demo.dto.StationInfo;
import com.example.demo.entity.OperationLog;
import com.example.demo.entity.Order;
import com.example.demo.service.AMapService;
import com.example.demo.service.AuthService;
import com.example.demo.service.OrderService;
import com.example.demo.service.OrderStatusService;
import com.example.demo.service.StationStatusService;
import com.example.demo.util.ExcelUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private final AuthService authService;
    private final ExcelUtil excelUtil;
    
    public OrderController(OrderService orderService, AMapService aMapService, 
                          OrderStatusService orderStatusService,
                          StationStatusService stationStatusService,
                          AuthService authService,
                          ExcelUtil excelUtil) {
        this.orderService = orderService;
        this.aMapService = aMapService;
        this.orderStatusService = orderStatusService;
        this.stationStatusService = stationStatusService;
        this.authService = authService;
        this.excelUtil = excelUtil;
    }
    
    /**
     * 获取订单统计
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        Map<String, Object> stats = orderService.getStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * 买家订单列表（根据token自动获取手机号过滤）
     */
    @GetMapping("/buyer")
    public ResponseEntity<ApiResponse<PageResult<Order>>> getBuyerOrders(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String cargoName) {
        
        String phone = getPhoneFromToken(authorization);
        if (phone == null || phone.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, "未登录或非买家用户"));
        }
        
        PageResult<Order> result = orderService.getOrders(page, pageSize, orderNo, status, null, 
            cargoName, null, null, null, phone);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 买家订单统计（根据token自动获取手机号过滤）
     */
    @GetMapping("/buyer/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBuyerStats(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        
        String phone = getPhoneFromToken(authorization);
        if (phone == null || phone.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, "未登录或非买家用户"));
        }
        
        Map<String, Object> stats = orderService.getStatsByPhone(phone, "receiver");
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * 卖家订单列表（根据token自动获取手机号过滤）
     */
    @GetMapping("/seller")
    public ResponseEntity<ApiResponse<PageResult<Order>>> getSellerOrders(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String cargoName) {
        
        String phone = getPhoneFromToken(authorization);
        if (phone == null || phone.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, "未登录或非卖家用户"));
        }
        
        PageResult<Order> result = orderService.getOrdersBySenderPhone(page, pageSize, orderNo, status, cargoName, phone);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 卖家订单统计（根据token自动获取手机号过滤）
     */
    @GetMapping("/seller/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSellerStats(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        
        String phone = getPhoneFromToken(authorization);
        if (phone == null || phone.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, "未登录或非卖家用户"));
        }
        
        Map<String, Object> stats = orderService.getStatsByPhone(phone, "sender");
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * 从token获取手机号
     */
    private String getPhoneFromToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        String token = authorization.substring(7);
        try {
            return authService.getUserInfo(token).getPhone();
        } catch (Exception e) {
            return null;
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResult<Order>>> getOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String trackingNo,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String cargoType,
            @RequestParam(required = false) String cargoName,
            @RequestParam(required = false) String expressCompany,
            @RequestParam(required = false) String senderName,
            @RequestParam(required = false) String receiverName,
            @RequestParam(required = false) String receiverPhone) {
        PageResult<Order> result = orderService.getOrders(page, pageSize, orderNo, trackingNo, status, cargoType, 
            cargoName, expressCompany, senderName, receiverName, receiverPhone);
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
     * 获取单个订单（支持订单号或运单号查询）
     */
    @GetMapping("/{queryValue}")
    public ResponseEntity<ApiResponse<Order>> getOrder(
            @PathVariable String queryValue,
            @RequestParam(defaultValue = "orderNo") String queryType) {
        Order order;
        if ("trackingNo".equals(queryType)) {
            order = orderService.getOrderByTrackingNo(queryValue);
        } else {
            order = orderService.getOrder(queryValue);
        }
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
     * 批量删除订单
     */
    @DeleteMapping("/batch")
    public ResponseEntity<ApiResponse<Map<String, Object>>> batchDeleteOrders(@RequestBody List<String> orderNos) {
        if (orderNos == null || orderNos.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "请选择要删除的订单"));
        }
        
        int deleted = orderService.batchDeleteOrders(orderNos);
        Map<String, Object> result = new HashMap<>();
        result.put("total", orderNos.size());
        result.put("deleted", deleted);
        result.put("failed", orderNos.size() - deleted);
        
        return ResponseEntity.ok(ApiResponse.success("批量删除完成", result));
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
    System.out.println("[DEVTOOLS] Hot reload test v2 - planRoute called");
        RoutePlanResponse response = aMapService.planRoute(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ==================== 状态变更 API ====================

    /**
     * 发货操作
     * PUT /api/orders/{orderNo}/ship
     */
    @PutMapping("/{orderNo}/ship")
    public ResponseEntity<ApiResponse<Order>> shipOrder(
            @PathVariable String orderNo,
            @RequestBody ShipRequest request) {
        try {
            Order order = orderStatusService.ship(orderNo, request.getTrackPoints(), request.getDuration());
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

    // ==================== 导入导出 API ====================

    /**
     * 下载导入模板
     * GET /api/orders/template
     */
    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate() {
        try {
            byte[] templateBytes = excelUtil.generateTemplate();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            String filename = URLEncoder.encode("订单导入模板.xlsx", StandardCharsets.UTF_8);
            headers.setContentDispositionFormData("attachment", filename);
            
            return new ResponseEntity<>(templateBytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 导入订单
     * POST /api/orders/import
     */
    @PostMapping("/import")
    public ResponseEntity<ApiResponse<ImportResultDTO>> importOrders(@RequestParam("file") MultipartFile file) {
        // 验证文件类型
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".xlsx")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(400, "请上传 .xlsx 格式的文件"));
        }
        
        // 验证文件大小（10MB）
        if (file.getSize() > 10 * 1024 * 1024) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(400, "文件大小不能超过 10MB"));
        }
        
        try {
            ImportResultDTO result = orderService.importOrders(file);
            String message = String.format("导入完成：总数 %d，成功 %d，失败 %d", 
                result.getTotal(), result.getSuccess(), result.getFailed());
            return ResponseEntity.ok(ApiResponse.success(message, result));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(400, "文件格式错误，请使用模板"));
        }
    }

    /**
     * 导出订单
     * POST /api/orders/export
     */
    @PostMapping("/export")
    public ResponseEntity<byte[]> exportOrders(@RequestBody(required = false) ExportRequest request) {
        try {
            // 如果请求体为空，导出全部订单
            if (request == null) {
                request = new ExportRequest();
            }
            
            List<Order> orders = orderService.exportOrders(request);
            byte[] excelBytes = excelUtil.exportOrders(orders);
            
            // 生成文件名：订单数据_yyyyMMdd.xlsx
            String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String filename = URLEncoder.encode("订单数据_" + dateStr + ".xlsx", StandardCharsets.UTF_8);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            
            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 下载导入失败记录
     * POST /api/orders/import/errors
     */
    @PostMapping("/import/errors")
    public ResponseEntity<byte[]> downloadErrors(@RequestBody List<ImportError> errors) {
        try {
            byte[] excelBytes = excelUtil.exportErrors(errors);
            
            // 生成文件名：导入失败记录_yyyyMMdd.xlsx
            String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String filename = URLEncoder.encode("导入失败记录_" + dateStr + ".xlsx", StandardCharsets.UTF_8);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            
            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
