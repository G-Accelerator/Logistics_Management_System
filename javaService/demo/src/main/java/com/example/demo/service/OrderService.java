package com.example.demo.service;

import com.example.demo.dto.ExportRequest;
import com.example.demo.dto.ImportError;
import com.example.demo.dto.ImportResultDTO;
import com.example.demo.dto.PageResult;
import com.example.demo.dto.RoutePlanResponse.TrackPoint;
import com.example.demo.entity.Order;
import com.example.demo.util.ExcelUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
import java.util.regex.Pattern;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private static final String DATA_FILE = "data/orders.json";
    
    // 手机号正则：11位数字
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{11}$");
    
    // 有效的货物类型枚举（中文名称）
    private static final Set<String> VALID_CARGO_TYPES = Set.of(
        "普通", "易碎", "贵重", "生鲜", "文件"
    );
    
    // 有效的货物类型内部代码
    private static final Set<String> VALID_CARGO_TYPE_CODES = Set.of(
        "normal", "fragile", "dangerous", "cold", "document"
    );
    
    // 有效的快递公司枚举（中文名称）
    private static final Set<String> VALID_EXPRESS_COMPANIES = Set.of(
        "顺丰速运", "中通快递", "圆通速递", "韵达快递", "申通快递",
        "京东物流", "邮政EMS", "德邦快递", "极兔速递", "百世快递"
    );
    
    // 有效的快递公司内部代码
    private static final Set<String> VALID_EXPRESS_COMPANY_CODES = Set.of(
        "sf", "zto", "yto", "yd", "sto", "jd", "ems", "deppon", "jitu", "best"
    );

    private final ObjectMapper objectMapper;
    private final List<Order> orders = Collections.synchronizedList(new ArrayList<>());
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    @Autowired
    private ExcelUtil excelUtil;
    
    @Autowired
    private AMapService aMapService;

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
     * 根据运单号获取订单
     */
    public Order getOrderByTrackingNo(String trackingNo) {
        return orders.stream()
            .filter(o -> trackingNo.equals(o.getTrackingNo()))
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

    /**
     * 批量删除订单
     */
    public int batchDeleteOrders(List<String> orderNos) {
        int deleted = 0;
        for (String orderNo : orderNos) {
            if (orders.removeIf(o -> o.getOrderNo().equals(orderNo))) {
                deleted++;
            }
        }
        if (deleted > 0) {
            saveToFile();
            log.info("批量删除订单: {} 条", deleted);
        }
        return deleted;
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
     * 根据手机号获取订单统计
     * @param phone 手机号
     * @param type "receiver" 或 "sender"
     */
    public Map<String, Object> getStatsByPhone(String phone, String type) {
        List<Order> filtered;
        if ("sender".equals(type)) {
            filtered = orders.stream()
                .filter(o -> phone.equals(o.getSenderPhone()))
                .toList();
        } else {
            filtered = orders.stream()
                .filter(o -> phone.equals(o.getReceiverPhone()))
                .toList();
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
                                                     String status, String cargoName, String senderPhone) {
        List<Order> filtered = orders.stream()
            .filter(o -> senderPhone.equals(o.getSenderPhone()))
            .filter(o -> orderNo == null || orderNo.isEmpty() || o.getOrderNo().contains(orderNo))
            .filter(o -> status == null || status.isEmpty() || o.getStatus().equals(status))
            .filter(o -> cargoName == null || cargoName.isEmpty() || 
                (o.getCargoName() != null && o.getCargoName().contains(cargoName)))
            .toList();

        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, filtered.size());

        List<Order> pageData = start < filtered.size() ? filtered.subList(start, end) : List.of();
        return new PageResult<>(pageData, filtered.size());
    }

    // ==================== 导入导出功能 ====================

    /**
     * 导入订单
     * 解析 Excel 文件，验证数据，批量创建订单
     * 
     * @param file 上传的 Excel 文件
     * @return 导入结果，包含成功数、失败数和错误详情
     */
    public ImportResultDTO importOrders(MultipartFile file) throws IOException {
        // 解析 Excel 文件
        List<Order> parsedOrders = excelUtil.parseExcel(file);
        
        ImportResultDTO result = new ImportResultDTO();
        result.setTotal(parsedOrders.size());
        
        int successCount = 0;
        List<ImportError> errors = new ArrayList<>();
        
        // 逐行验证并创建订单
        for (int i = 0; i < parsedOrders.size(); i++) {
            int rowNum = i + 2; // Excel 行号（从第2行开始，第1行是表头）
            Order order = parsedOrders.get(i);
            
            // 检查订单号是否已存在
            if (order.getOrderNo() != null && !order.getOrderNo().isEmpty()) {
                Order existing = getOrder(order.getOrderNo());
                if (existing != null) {
                    errors.add(new ImportError(rowNum, "订单号", order.getOrderNo(), "订单已存在"));
                    continue;
                }
            }
            
            // 验证订单数据
            List<ImportError> rowErrors = validateOrder(order, rowNum);
            
            if (rowErrors.isEmpty()) {
                // 验证通过，创建订单（保留原订单号，生成轨迹数据）
                importCreateOrder(order);
                successCount++;
            } else {
                // 验证失败，记录错误
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
    
    /**
     * 导入创建订单（使用原订单号）
     */
    private Order importCreateOrder(Order order) {
        order.setId(idGenerator.getAndIncrement());
        order.setStatus("pending");
        order.setCreateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        
        orders.add(0, order);
        saveToFile();
        
        log.info("导入订单: {}", order.getOrderNo());
        return order;
    }

    /**
     * 验证订单数据
     * 
     * @param order 订单对象
     * @param rowNum Excel 行号
     * @return 错误列表，为空表示验证通过
     */
    private List<ImportError> validateOrder(Order order, int rowNum) {
        List<ImportError> errors = new ArrayList<>();
        
        // 0. 验证订单号（必填）
        if (isBlank(order.getOrderNo())) {
            errors.add(new ImportError(rowNum, "订单号", null, "订单号不能为空"));
        }
        
        // 1. 验证必填字段
        if (isBlank(order.getCargoName())) {
            errors.add(new ImportError(rowNum, "货物名称", null, "货物名称不能为空"));
        } else if (order.getCargoName().length() > 50) {
            errors.add(new ImportError(rowNum, "货物名称", order.getCargoName(), "货物名称不能超过50个字符"));
        }
        
        if (isBlank(order.getOrigin())) {
            errors.add(new ImportError(rowNum, "发货地址", null, "发货地址不能为空"));
        } else if (order.getOrigin().length() > 200) {
            errors.add(new ImportError(rowNum, "发货地址", order.getOrigin(), "发货地址不能超过200个字符"));
        }
        
        if (isBlank(order.getDestination())) {
            errors.add(new ImportError(rowNum, "收货地址", null, "收货地址不能为空"));
        } else if (order.getDestination().length() > 200) {
            errors.add(new ImportError(rowNum, "收货地址", order.getDestination(), "收货地址不能超过200个字符"));
        }
        
        if (isBlank(order.getSenderName())) {
            errors.add(new ImportError(rowNum, "发货人", null, "发货人不能为空"));
        } else if (order.getSenderName().length() > 20) {
            errors.add(new ImportError(rowNum, "发货人", order.getSenderName(), "发货人姓名不能超过20个字符"));
        }
        
        if (isBlank(order.getReceiverName())) {
            errors.add(new ImportError(rowNum, "收货人", null, "收货人不能为空"));
        } else if (order.getReceiverName().length() > 20) {
            errors.add(new ImportError(rowNum, "收货人", order.getReceiverName(), "收货人姓名不能超过20个字符"));
        }
        
        // 2. 验证手机号格式（11位数字）
        if (isBlank(order.getSenderPhone())) {
            errors.add(new ImportError(rowNum, "发货人电话", null, "发货人电话不能为空"));
        } else if (!PHONE_PATTERN.matcher(order.getSenderPhone()).matches()) {
            errors.add(new ImportError(rowNum, "发货人电话", order.getSenderPhone(), "发货人电话格式错误，应为11位数字"));
        }
        
        if (isBlank(order.getReceiverPhone())) {
            errors.add(new ImportError(rowNum, "收货人电话", null, "收货人电话不能为空"));
        } else if (!PHONE_PATTERN.matcher(order.getReceiverPhone()).matches()) {
            errors.add(new ImportError(rowNum, "收货人电话", order.getReceiverPhone(), "收货人电话格式错误，应为11位数字"));
        }
        
        // 3. 验证货物类型枚举值（同时支持中文名称和内部代码）
        if (!isBlank(order.getCargoType()) && 
            !VALID_CARGO_TYPES.contains(order.getCargoType()) && 
            !VALID_CARGO_TYPE_CODES.contains(order.getCargoType())) {
            errors.add(new ImportError(rowNum, "货物类型", order.getCargoType(), 
                "货物类型无效，有效值：" + String.join("、", VALID_CARGO_TYPES)));
        }
        
        // 4. 验证快递公司枚举值（同时支持中文名称和内部代码）
        if (!isBlank(order.getExpressCompany()) && 
            !VALID_EXPRESS_COMPANIES.contains(order.getExpressCompany()) &&
            !VALID_EXPRESS_COMPANY_CODES.contains(order.getExpressCompany())) {
            errors.add(new ImportError(rowNum, "快递公司", order.getExpressCompany(), 
                "快递公司无效，有效值：" + String.join("、", VALID_EXPRESS_COMPANIES)));
        }
        
        // 5. 验证数值字段
        if (order.getCargoWeight() != null) {
            if (order.getCargoWeight() <= 0) {
                errors.add(new ImportError(rowNum, "重量", String.valueOf(order.getCargoWeight()), "重量必须为正数"));
            } else if (order.getCargoWeight() > 9999.99) {
                errors.add(new ImportError(rowNum, "重量", String.valueOf(order.getCargoWeight()), "重量不能超过9999.99kg"));
            }
        }
        
        if (order.getCargoVolume() != null && order.getCargoVolume() <= 0) {
            errors.add(new ImportError(rowNum, "体积", String.valueOf(order.getCargoVolume()), "体积必须为正数"));
        }
        
        if (order.getCargoQuantity() != null && order.getCargoQuantity() <= 0) {
            errors.add(new ImportError(rowNum, "数量", String.valueOf(order.getCargoQuantity()), "数量必须为正整数"));
        }
        
        // 6. 验证备注长度
        if (order.getRemark() != null && order.getRemark().length() > 500) {
            errors.add(new ImportError(rowNum, "备注", order.getRemark().substring(0, 50) + "...", "备注不能超过500个字符"));
        }
        
        return errors;
    }

    /**
     * 判断字符串是否为空或空白
     */
    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 导出订单
     * 根据指定的订单ID列表或筛选条件导出订单
     * 
     * @param request 导出请求，包含订单ID列表或筛选条件
     * @return 导出的订单列表
     */
    public List<Order> exportOrders(ExportRequest request) {
        List<Order> result;
        
        if (request.getIds() != null && !request.getIds().isEmpty()) {
            // 按指定ID导出
            Set<Long> idSet = new HashSet<>(request.getIds());
            result = orders.stream()
                .filter(o -> idSet.contains(o.getId()))
                .toList();
            log.info("按ID导出订单: 请求数量={}, 实际导出={}", request.getIds().size(), result.size());
        } else if (request.getFilters() != null) {
            // 按筛选条件导出
            ExportRequest.OrderQueryParams filters = request.getFilters();
            result = orders.stream()
                .filter(o -> isBlank(filters.getOrderNo()) || 
                    (o.getOrderNo() != null && o.getOrderNo().contains(filters.getOrderNo())))
                .filter(o -> isBlank(filters.getStatus()) || 
                    filters.getStatus().equals(o.getStatus()))
                .filter(o -> isBlank(filters.getCargoType()) || 
                    filters.getCargoType().equals(o.getCargoType()))
                .filter(o -> isBlank(filters.getCargoName()) || 
                    (o.getCargoName() != null && o.getCargoName().contains(filters.getCargoName())))
                .filter(o -> isBlank(filters.getExpressCompany()) || 
                    filters.getExpressCompany().equals(o.getExpressCompany()))
                .filter(o -> isBlank(filters.getSenderName()) || 
                    (o.getSenderName() != null && o.getSenderName().contains(filters.getSenderName())))
                .filter(o -> isBlank(filters.getReceiverName()) || 
                    (o.getReceiverName() != null && o.getReceiverName().contains(filters.getReceiverName())))
                .filter(o -> isBlank(filters.getReceiverPhone()) || 
                    filters.getReceiverPhone().equals(o.getReceiverPhone()))
                .toList();
            log.info("按筛选条件导出订单: 导出数量={}", result.size());
        } else {
            // 导出全部订单
            result = new ArrayList<>(orders);
            log.info("导出全部订单: 数量={}", result.size());
        }
        
        return result;
    }
}
