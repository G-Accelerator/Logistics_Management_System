package com.example.demo.service;

import com.example.demo.dto.RoutePlanResponse.TrackPoint;
import com.example.demo.entity.ExpressCompany;
import com.example.demo.entity.Order;
import com.example.demo.repository.ExpressCompanyRepository;
import com.example.demo.repository.OrderRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.List;

/**
 * 数据迁移服务
 * 用于将JSON文件中的数据迁移到MySQL数据库
 */
@Service
public class DataMigrationService {

    private static final Logger log = LoggerFactory.getLogger(DataMigrationService.class);
    private static final String ORDERS_FILE = "data/orders.json";
    private static final String EXPRESS_COMPANIES_FILE = "data/express-companies.json";

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ExpressCompanyRepository expressCompanyRepository;

    @Value("${app.migration.enabled:true}")
    private boolean migrationEnabled;

    private final ObjectMapper objectMapper;

    public DataMigrationService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @PostConstruct
    public void init() {
        if (migrationEnabled) {
            migrateData();
        }
    }

    /**
     * 执行数据迁移
     */
    @Transactional
    public void migrateData() {
        log.info("开始数据迁移...");
        
        // 迁移快递公司数据
        migrateExpressCompanies();
        
        // 迁移订单数据
        migrateOrders();
        
        log.info("数据迁移完成");
    }

    /**
     * 迁移快递公司数据
     */
    private void migrateExpressCompanies() {
        if (expressCompanyRepository.count() > 0) {
            log.info("快递公司数据已存在，跳过迁移");
            return;
        }

        File file = new File(EXPRESS_COMPANIES_FILE);
        if (!file.exists()) {
            log.info("快递公司JSON文件不存在，跳过迁移");
            return;
        }

        try {
            List<ExpressCompany> companies = objectMapper.readValue(
                file, new TypeReference<List<ExpressCompany>>() {});
            
            for (ExpressCompany company : companies) {
                // 清除ID，让数据库自动生成
                company.setId(null);
                expressCompanyRepository.save(company);
            }
            
            log.info("成功迁移 {} 条快递公司数据", companies.size());
        } catch (Exception e) {
            log.error("迁移快递公司数据失败: {}", e.getMessage());
        }
    }

    /**
     * 迁移订单数据
     */
    private void migrateOrders() {
        if (orderRepository.count() > 0) {
            log.info("订单数据已存在，跳过迁移");
            return;
        }

        File file = new File(ORDERS_FILE);
        if (!file.exists()) {
            log.info("订单JSON文件不存在，跳过迁移");
            return;
        }

        try {
            List<JsonOrder> jsonOrders = objectMapper.readValue(
                file, new TypeReference<List<JsonOrder>>() {});
            
            for (JsonOrder jsonOrder : jsonOrders) {
                Order order = convertToEntity(jsonOrder);
                orderRepository.save(order);
            }
            
            log.info("成功迁移 {} 条订单数据", jsonOrders.size());
        } catch (Exception e) {
            log.error("迁移订单数据失败: {}", e.getMessage());
        }
    }

    /**
     * 将JSON订单转换为Entity
     */
    private Order convertToEntity(JsonOrder jsonOrder) {
        Order order = new Order();
        order.setOrderNo(jsonOrder.orderNo);
        order.setCargoName(jsonOrder.cargoName);
        order.setCargoType(jsonOrder.cargoType);
        order.setCargoWeight(jsonOrder.cargoWeight);
        order.setCargoVolume(jsonOrder.cargoVolume);
        order.setCargoQuantity(jsonOrder.cargoQuantity);
        order.setRemark(jsonOrder.remark);
        order.setExpressCompany(jsonOrder.expressCompany);
        order.setOrigin(jsonOrder.origin);
        order.setDestination(jsonOrder.destination);
        order.setSenderName(jsonOrder.senderName);
        order.setReceiverName(jsonOrder.receiverName);
        order.setSenderPhone(jsonOrder.senderPhone);
        order.setReceiverPhone(jsonOrder.receiverPhone);
        order.setStatus(jsonOrder.status);
        order.setCreateTime(jsonOrder.createTime);
        order.setTrackingNo(jsonOrder.trackingNo);
        order.setDuration(jsonOrder.duration);
        order.setShipTime(jsonOrder.shipTime);
        order.setReceiveTime(jsonOrder.receiveTime);
        order.setCancelTime(jsonOrder.cancelTime);
        order.setOriginLng(jsonOrder.originLng);
        order.setOriginLat(jsonOrder.originLat);
        order.setDestLng(jsonOrder.destLng);
        order.setDestLat(jsonOrder.destLat);
        
        // 将trackPoints转换为JSON字符串
        if (jsonOrder.trackPoints != null && !jsonOrder.trackPoints.isEmpty()) {
            try {
                order.setTrackPointsJson(objectMapper.writeValueAsString(jsonOrder.trackPoints));
            } catch (Exception e) {
                log.warn("转换trackPoints失败: {}", e.getMessage());
            }
        }
        
        return order;
    }

    /**
     * JSON订单结构（用于读取旧的JSON文件）
     */
    private static class JsonOrder {
        public Long id;
        public String orderNo;
        public String cargoName;
        public String cargoType;
        public Double cargoWeight;
        public Double cargoVolume;
        public Integer cargoQuantity;
        public String remark;
        public String expressCompany;
        public String origin;
        public String destination;
        public String senderName;
        public String receiverName;
        public String senderPhone;
        public String receiverPhone;
        public String status;
        public String createTime;
        public String trackingNo;
        public Integer duration;
        public List<TrackPoint> trackPoints;
        public java.time.LocalDateTime shipTime;
        public java.time.LocalDateTime receiveTime;
        public java.time.LocalDateTime cancelTime;
        public Double originLng;
        public Double originLat;
        public Double destLng;
        public Double destLat;
    }
}
