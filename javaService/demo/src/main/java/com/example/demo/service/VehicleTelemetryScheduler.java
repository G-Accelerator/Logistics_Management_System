package com.example.demo.service;

import com.example.demo.entity.Vehicle;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.VehicleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

/**
 * 模拟车辆在线状态（默认关闭；在线状态由发货默认在线 + 运单页手动维护）
 */
@Component
@ConditionalOnProperty(name = "app.vehicle.telemetry.enabled", havingValue = "true")
public class VehicleTelemetryScheduler {

    private static final Logger log = LoggerFactory.getLogger(VehicleTelemetryScheduler.class);
    private static final Random RANDOM = new Random();

    private final VehicleRepository vehicleRepository;
    private final OrderRepository orderRepository;
    private final SmsSimulationService smsSimulationService;

    public VehicleTelemetryScheduler(
        VehicleRepository vehicleRepository,
        OrderRepository orderRepository,
        SmsSimulationService smsSimulationService
    ) {
        this.vehicleRepository = vehicleRepository;
        this.orderRepository = orderRepository;
        this.smsSimulationService = smsSimulationService;
    }

    @Scheduled(fixedDelayString = "${app.vehicle.telemetry.delay-ms:45000}")
    @Transactional
    public void refreshVehicleOnlineFlags() {
        List<Vehicle> vehicles = vehicleRepository.findAll();
        for (Vehicle v : vehicles) {
            boolean inUse = orderRepository.existsByVehicleIdAndStatus(v.getId(), OrderStatusService.STATUS_SHIPPING);
            if (!inUse) {
                v.setOnline(false);
            } else {
                v.setOnline(RANDOM.nextDouble() < 0.85);
            }
        }
        vehicleRepository.saveAll(vehicles);
        for (Vehicle v : vehicles) {
            if (orderRepository.existsByVehicleIdAndStatus(v.getId(), OrderStatusService.STATUS_SHIPPING)) {
                smsSimulationService.evaluateAlerts(v.getId());
            }
        }
        log.trace("车辆在线状态已刷新，共 {} 辆", vehicles.size());
    }
}
