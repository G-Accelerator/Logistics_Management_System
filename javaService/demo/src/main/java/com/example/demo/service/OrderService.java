package com.example.demo.service;

import com.example.demo.dto.ExportRequest;
import com.example.demo.dto.ImportError;
import com.example.demo.dto.ImportResultDTO;
import com.example.demo.dto.PageResult;
import com.example.demo.dto.RoutePlanResponse.TrackPoint;
import com.example.demo.entity.OperationLog;
import com.example.demo.entity.Order;
import com.example.demo.repository.OperationLogRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.util.ExcelUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.criteria.Predicate;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    
    // 手机号正则：11位数字
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{11}$");
    
    // 有效的货物类型枚举（代码形式）
    private static final Set<String> VALID_CARGO_TYPES = Set.of(
        "normal", "fragile", "dangerous", "cold", "document"
    );
    
    // 有效的快递公司枚举（代码形式）
    private static final Set<String> VALID_EXPRESS_COMPANIES = Set.of(
        "sf", "zto", "yto", "yd", "sto", "jd", "ems", "deppon", "jitu", "best"
    );

    private final ObjectMapper objectMapper;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private ExcelUtil excelUtil;
    
    @Autowired
    private AMapService aMapService;
    
    @Autowired
    private OperationLogRepository operationLogRepository;
    
    @Autowired
    private ExpressCompanyService expressCompanyService;

    public OrderService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * 创建订单
     */
    @Transactional
    public Order createOrder(Order order) {
        order.setOrderNo(generateOrderNo());
        order.setStatus("pending");
        order.setCreateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        
        Order saved = orderRepository.save(order);
        
        // 记录订单创建日志
        OperationLog opLog = new OperationLog(saved.getOrderNo(), "create", null, "pending", "system");
        operationLogRepository.save(opLog);
        
        log.info("创建订单: {}", saved.getOrderNo());
        return saved;
    }

    /**
     * 查询订单列表（使用JPA Specification动态查询）
     */
    public PageResult<Order> getOrders(int page, int pageSize, String orderNo,
                                       String trackingNo, String status, String cargoType, 
                                       String cargoName, String expressCompany, String senderName, 
                                       String receiverName, String receiverPhone) {
        
        Specification<Order> spec = buildSpecification(orderNo, trackingNo, status, cargoType, 
            cargoName, expressCompany, senderName, receiverName, receiverPhone, null);
        
        PageRequest pageRequest = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "id"));
        Page<Order> pageResult = orderRepository.findAll(spec, pageRequest);
        
        // 转换trackPointsJson为trackPoints
        List<Order> orders = pageResult.getContent().stream()
            .peek(this::convertTrackPointsFromJson)
            .toList();
        
        return new PageResult<>(orders, (int) pageResult.getTotalElements());
    }
    
    /**
     * 构建动态查询条件
     */
    private Specification<Order> buildSpecification(String orderNo, String trackingNo, String status,
                                                     String cargoType, String cargoName, String expressCompany,
                                                     String senderName, String receiverName, String receiverPhone,
                                                     String senderPhone) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (orderNo != null && !orderNo.isEmpty()) {
                predicates.add(cb.like(root.get("orderNo"), "%" + orderNo + "%"));
            }
            if (trackingNo != null && !trackingNo.isEmpty()) {
                predicates.add(cb.like(root.get("trackingNo"), "%" + trackingNo + "%"));
            }
            if (status != null && !status.isEmpty()) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (cargoType != null && !cargoType.isEmpty()) {
                predicates.add(cb.equal(root.get("cargoType"), cargoType));
            }
            if (cargoName != null && !cargoName.isEmpty()) {
                predicates.add(cb.like(root.get("cargoName"), "%" + cargoName + "%"));
            }
            if (expressCompany != null && !expressCompany.isEmpty()) {
                predicates.add(cb.equal(root.get("expressCompany"), expressCompany));
            }
            if (senderName != null && !senderName.isEmpty()) {
                predicates.add(cb.like(root.get("senderName"), "%" + senderName + "%"));
            }
            if (receiverName != null && !receiverName.isEmpty()) {
                predicates.add(cb.like(root.get("receiverName"), "%" + receiverName + "%"));
            }
            if (receiverPhone != null && !receiverPhone.isEmpty()) {
                predicates.add(cb.equal(root.get("receiverPhone"), receiverPhone));
            }
            if (senderPhone != null && !senderPhone.isEmpty()) {
                predicates.add(cb.equal(root.get("senderPhone"), senderPhone));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * 根据订单号获取订单
     */
    public Order getOrder(String orderNo) {
        return orderRepository.findByOrderNo(orderNo)
            .map(this::convertTrackPointsFromJson)
            .orElse(null);
    }

    /**
     * 根据运单号获取订单
     */
    public Order getOrderByTrackingNo(String trackingNo) {
        return orderRepository.findByTrackingNo(trackingNo)
            .map(this::convertTrackPointsFromJson)
            .orElse(null);
    }

    /**
     * 根据订单号获取站点数据
     */
    public List<TrackPoint> getTrackPoints(String orderNo) {
        Order order = getOrder(orderNo);
        if (order == null) {
            return List.of();
        }
        return parseTrackPoints(order.getTrackPointsJson());
    }

    /**
     * 更新订单
     */
    @Transactional
    public Order updateOrder(Order order) {
        if (order == null || order.getOrderNo() == null) {
            return null;
        }
        Order saved = orderRepository.save(order);
        log.info("更新订单: {}", saved.getOrderNo());
        return saved;
    }

    /**
     * 删除订单
     */
    @Transactional
    public boolean deleteOrder(String orderNo) {
        Optional<Order> order = orderRepository.findByOrderNo(orderNo);
        if (order.isPresent()) {
            orderRepository.delete(order.get());
            log.info("删除订单: {}", orderNo);
            return true;
        }
        return false;
    }

    /**
     * 批量删除订单
     */
    @Transactional
    public int batchDeleteOrders(List<String> orderNos) {
        int deleted = 0;
        for (String orderNo : orderNos) {
            if (deleteOrder(orderNo)) {
                deleted++;
            }
        }
        log.info("批量删除订单: {} 条", deleted);
        return deleted;
    }

    private String generateOrderNo() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = String.format("%04d", new Random().nextInt(10000));
        return "ORD" + date + random;
    }

    /**
     * 获取订单统计
     */
    public Map<String, Object> getStats() {
        long total = orderRepository.count();
        long pending = orderRepository.countByStatus("pending");
        long shipping = orderRepository.countByStatus("shipping");
        long completed = orderRepository.countByStatus("completed");
        
        return Map.of(
            "total", total,
            "pending", pending,
            "shipping", shipping,
            "completed", completed
        );
    }

    /**
     * 根据手机号获取订单统计
     */
    public Map<String, Object> getStatsByPhone(String phone, String type) {
        List<Order> filtered;
        if ("sender".equals(type)) {
            filtered = orderRepository.findBySenderPhone(phone);
        } else {
            filtered = orderRepository.findByReceiverPhone(phone);
        }
        
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

    /**
     * 根据发货人手机号查询订单（卖家专用）
     */
    public PageResult<Order> getOrdersBySenderPhone(int page, int pageSize, String orderNo,
                                                     String trackingNo, String status, 
                                                     String cargoName, String senderPhone) {
        Specification<Order> spec = buildSpecification(orderNo, trackingNo, status, null, 
            cargoName, null, null, null, null, senderPhone);
        
        PageRequest pageRequest = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "id"));
        Page<Order> pageResult = orderRepository.findAll(spec, pageRequest);
        
        List<Order> orders = pageResult.getContent().stream()
            .peek(this::convertTrackPointsFromJson)
            .toList();
        
        return new PageResult<>(orders, (int) pageResult.getTotalElements());
    }

    // ==================== TrackPoints JSON转换 ====================
    
    /**
     * 填充订单的快递公司名称
     */
    private Order fillExpressCompanyName(Order order) {
        if (order.getExpressCompany() != null && !order.getExpressCompany().isEmpty()) {
            var company = expressCompanyService.getByCode(order.getExpressCompany());
            if (company != null) {
                order.setExpressCompanyName(company.getName());
            } else {
                order.setExpressCompanyName(order.getExpressCompany());
            }
        }
        return order;
    }
    
    /**
     * 将trackPointsJson转换为List<TrackPoint>并设置到transient字段
     */
    private Order convertTrackPointsFromJson(Order order) {
        // 填充快递公司名称
        fillExpressCompanyName(order);
        return order;
    }
    
    /**
     * 解析trackPointsJson为List<TrackPoint>
     */
    public List<TrackPoint> parseTrackPoints(String json) {
        if (json == null || json.isEmpty()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<TrackPoint>>() {});
        } catch (Exception e) {
            log.error("解析trackPoints失败: {}", e.getMessage());
            return List.of();
        }
    }
    
    /**
     * 将List<TrackPoint>转换为JSON字符串
     */
    public String serializeTrackPoints(List<TrackPoint> trackPoints) {
        if (trackPoints == null || trackPoints.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(trackPoints);
        } catch (Exception e) {
            log.error("序列化trackPoints失败: {}", e.getMessage());
            return null;
        }
    }

    // ==================== 导入导出功能 ====================

    /**
     * 导入订单
     */
    @Transactional
    public ImportResultDTO importOrders(MultipartFile file) throws IOException {
        List<Order> parsedOrders = excelUtil.parseExcel(file);
        
        ImportResultDTO result = new ImportResultDTO();
        result.setTotal(parsedOrders.size());
        
        int successCount = 0;
        List<ImportError> errors = new ArrayList<>();
        
        for (int i = 0; i < parsedOrders.size(); i++) {
            int rowNum = i + 2;
            Order order = parsedOrders.get(i);
            
            if (order.getOrderNo() != null && !order.getOrderNo().isEmpty()) {
                if (orderRepository.existsByOrderNo(order.getOrderNo())) {
                    errors.add(new ImportError(rowNum, "订单号", order.getOrderNo(), "订单已存在"));
                    continue;
                }
            }
            
            List<ImportError> rowErrors = validateOrder(order, rowNum);
            
            if (rowErrors.isEmpty()) {
                importCreateOrder(order);
                successCount++;
            } else {
                errors.addAll(rowErrors);
            }
        }
        
        result.setSuccess(successCount);
        result.setFailed(parsedOrders.size() - successCount);
        result.setErrors(errors);
        
        log.info("订单导入完成: 总数={}, 成功={}, 失败={}", 
            result.getTotal(), result.getSuccess(), result.getFailed());
        
        return result;
    }
    
    private Order importCreateOrder(Order order) {
        order.setStatus("pending");
        order.setCreateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        return orderRepository.save(order);
    }

    private List<ImportError> validateOrder(Order order, int rowNum) {
        List<ImportError> errors = new ArrayList<>();
        
        if (isBlank(order.getOrderNo())) {
            errors.add(new ImportError(rowNum, "订单号", null, "订单号不能为空"));
        }
        if (isBlank(order.getCargoName())) {
            errors.add(new ImportError(rowNum, "货物名称", null, "货物名称不能为空"));
        }
        if (isBlank(order.getOrigin())) {
            errors.add(new ImportError(rowNum, "发货地址", null, "发货地址不能为空"));
        }
        if (isBlank(order.getDestination())) {
            errors.add(new ImportError(rowNum, "收货地址", null, "收货地址不能为空"));
        }
        if (isBlank(order.getSenderName())) {
            errors.add(new ImportError(rowNum, "发货人", null, "发货人不能为空"));
        }
        if (isBlank(order.getReceiverName())) {
            errors.add(new ImportError(rowNum, "收货人", null, "收货人不能为空"));
        }
        if (isBlank(order.getSenderPhone())) {
            errors.add(new ImportError(rowNum, "发货人电话", null, "发货人电话不能为空"));
        } else if (!PHONE_PATTERN.matcher(order.getSenderPhone()).matches()) {
            errors.add(new ImportError(rowNum, "发货人电话", order.getSenderPhone(), "格式错误"));
        }
        if (isBlank(order.getReceiverPhone())) {
            errors.add(new ImportError(rowNum, "收货人电话", null, "收货人电话不能为空"));
        } else if (!PHONE_PATTERN.matcher(order.getReceiverPhone()).matches()) {
            errors.add(new ImportError(rowNum, "收货人电话", order.getReceiverPhone(), "格式错误"));
        }
        if (!isBlank(order.getCargoType()) && !VALID_CARGO_TYPES.contains(order.getCargoType())) {
            errors.add(new ImportError(rowNum, "货物类型", order.getCargoType(), "无效"));
        }
        if (!isBlank(order.getExpressCompany()) && !VALID_EXPRESS_COMPANIES.contains(order.getExpressCompany())) {
            errors.add(new ImportError(rowNum, "快递公司", order.getExpressCompany(), "无效"));
        }
        
        return errors;
    }

    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 导出订单
     */
    public List<Order> exportOrders(ExportRequest request) {
        List<Order> result;
        
        if (request.getIds() != null && !request.getIds().isEmpty()) {
            result = orderRepository.findAllById(request.getIds());
        } else if (request.getFilters() != null) {
            ExportRequest.OrderQueryParams f = request.getFilters();
            Specification<Order> spec = buildSpecification(f.getOrderNo(), null, f.getStatus(),
                f.getCargoType(), f.getCargoName(), f.getExpressCompany(), f.getSenderName(),
                f.getReceiverName(), f.getReceiverPhone(), null);
            result = orderRepository.findAll(spec);
        } else {
            result = orderRepository.findAll();
        }
        
        return result;
    }
}
