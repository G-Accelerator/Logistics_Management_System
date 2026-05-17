package com.example.demo.service;

import com.example.demo.entity.Order;
import com.example.demo.entity.SmsTemplateType;
import com.example.demo.entity.Vehicle;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.VehicleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 模拟短信：渲染模板并在控制台打印；对超速/离线做边沿检测避免重复发送。
 */
@Service
public class SmsSimulationService {

    private static final Logger SMS_LOG = LoggerFactory.getLogger("com.example.demo.sms");

    private record AlertFlags(boolean offline, boolean overspeed) {}

    private final SmsTemplateService smsTemplateService;
    private final SmsMessageLogService smsMessageLogService;
    private final VehicleRepository vehicleRepository;
    private final OrderRepository orderRepository;
    private final Map<Long, AlertFlags> alertStateByVehicle = new ConcurrentHashMap<>();

    @Value("${app.sms.simulation.enabled:true}")
    private boolean simulationEnabled;

    public SmsSimulationService(
        SmsTemplateService smsTemplateService,
        SmsMessageLogService smsMessageLogService,
        VehicleRepository vehicleRepository,
        OrderRepository orderRepository
    ) {
        this.smsTemplateService = smsTemplateService;
        this.smsMessageLogService = smsMessageLogService;
        this.vehicleRepository = vehicleRepository;
        this.orderRepository = orderRepository;
    }

    /**
     * 运输中车辆状态变化后调用（车速或在线状态更新、定时刷新在线标志后）
     */
    public void evaluateAlerts(Long vehicleId) {
        if (!simulationEnabled || vehicleId == null) {
            return;
        }
        Vehicle vehicle = vehicleRepository.findById(vehicleId).orElse(null);
        if (vehicle == null) {
            return;
        }
        Order order = orderRepository
            .findFirstByVehicleIdAndStatus(vehicleId, OrderStatusService.STATUS_SHIPPING)
            .orElse(null);
        if (order == null) {
            alertStateByVehicle.remove(vehicleId);
            return;
        }
        boolean offline = !Boolean.TRUE.equals(vehicle.getOnline());
        int speed = order.getCurrentSpeedKmh() != null ? order.getCurrentSpeedKmh() : 0;
        boolean overspeed = speed > vehicle.getSpeedLimitKmh();

        AlertFlags prev = alertStateByVehicle.getOrDefault(vehicleId, new AlertFlags(false, false));
        if (!prev.offline() && offline) {
            sendAuto(vehicle, order, SmsTemplateType.OFFLINE, "自动-离线");
        }
        if (!prev.overspeed() && overspeed) {
            sendAuto(vehicle, order, SmsTemplateType.OVERSPEED, "自动-超速");
        }
        alertStateByVehicle.put(vehicleId, new AlertFlags(offline, overspeed));
    }

    /**
     * 车辆监控页手动发送（content 可在模板渲染基础上修改）
     */
    public String sendManual(Long vehicleId, String orderNo, SmsTemplateType type, String content) {
        if (vehicleId == null) {
            throw new IllegalArgumentException("车辆ID不能为空");
        }
        if (orderNo == null || orderNo.isBlank()) {
            throw new IllegalArgumentException("订单号不能为空");
        }
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
            .orElseThrow(() -> new IllegalArgumentException("车辆不存在"));
        Order order = orderRepository.findByOrderNo(orderNo.trim())
            .orElseThrow(() -> new IllegalArgumentException("订单不存在"));
        if (!OrderStatusService.STATUS_SHIPPING.equals(order.getStatus())) {
            throw new IllegalStateException("仅运输中的订单可发送消息");
        }
        if (order.getVehicleId() == null || !order.getVehicleId().equals(vehicleId)) {
            throw new IllegalStateException("订单与车辆不匹配");
        }
        boolean offline = !Boolean.TRUE.equals(vehicle.getOnline());
        int speed = order.getCurrentSpeedKmh() != null ? order.getCurrentSpeedKmh() : 0;
        boolean overspeed = speed > vehicle.getSpeedLimitKmh();
        if (type == SmsTemplateType.OVERSPEED && !overspeed) {
            throw new IllegalStateException("当前车辆未超速，请选用常规模板或调整车速");
        }
        if (type == SmsTemplateType.OFFLINE && !offline) {
            throw new IllegalStateException("当前车辆未离线，请选用常规模板");
        }
        String message = resolveManualContent(type, vehicle, order, content);
        sendSimulated(vehicle, order, type, "手动-车辆监控", message);
        return message;
    }

    private String resolveManualContent(
        SmsTemplateType type,
        Vehicle vehicle,
        Order order,
        String content
    ) {
        String message = content != null ? content.trim() : "";
        if (message.isEmpty()) {
            message = render(type, vehicle, order);
        }
        if (message.isEmpty()) {
            throw new IllegalArgumentException("消息内容不能为空");
        }
        if (message.length() > 2000) {
            throw new IllegalArgumentException("消息内容不能超过2000字");
        }
        return message;
    }

    private void sendAuto(Vehicle vehicle, Order order, SmsTemplateType type, String trigger) {
        sendSimulated(vehicle, order, type, trigger, render(type, vehicle, order));
    }

    private void sendSimulated(
        Vehicle vehicle,
        Order order,
        SmsTemplateType type,
        String trigger,
        String content
    ) {
        if (!simulationEnabled) {
            return;
        }
        String phone = vehicle.getDriverPhone();
        smsMessageLogService.record(type, trigger, vehicle, order, content);
        SMS_LOG.info(
            "[模拟短信] 触发={} 类型={} 收件人={} 车牌={} 订单={} | {}",
            trigger, type.name(), phone, vehicle.getPlateNumber(), order.getOrderNo(), content);
    }

    public String render(SmsTemplateType type, Vehicle vehicle, Order order) {
        String template = smsTemplateService.getContent(type);
        int speed = order.getCurrentSpeedKmh() != null ? order.getCurrentSpeedKmh() : 0;
        return template
            .replace("{plateNumber}", nullSafe(vehicle.getPlateNumber()))
            .replace("{vehicleType}", nullSafe(vehicle.getVehicleType()))
            .replace("{driverName}", nullSafe(vehicle.getDriverName()))
            .replace("{driverPhone}", nullSafe(vehicle.getDriverPhone()))
            .replace("{speedLimitKmh}", String.valueOf(vehicle.getSpeedLimitKmh()))
            .replace("{currentSpeedKmh}", String.valueOf(speed))
            .replace("{orderNo}", nullSafe(order.getOrderNo()))
            .replace("{trackingNo}", nullSafe(order.getTrackingNo()));
    }

    public String preview(SmsTemplateType type, Long vehicleId, String orderNo) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
            .orElseThrow(() -> new IllegalArgumentException("车辆不存在"));
        Order order = orderRepository.findByOrderNo(orderNo.trim())
            .orElseThrow(() -> new IllegalArgumentException("订单不存在"));
        return render(type, vehicle, order);
    }

    private static String nullSafe(String s) {
        return s != null ? s : "";
    }
}
