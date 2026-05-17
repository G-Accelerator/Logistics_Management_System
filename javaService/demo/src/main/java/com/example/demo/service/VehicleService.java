package com.example.demo.service;

import com.example.demo.dto.PageResult;
import com.example.demo.dto.VehicleMonitoringDTO;
import com.example.demo.entity.Order;
import com.example.demo.entity.Vehicle;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
public class VehicleService {

    private static final int SPEED_MIN = 1;
    private static final int SPEED_MAX = 200;
    private static final String PHONE_PATTERN = "^1[3-9]\\d{9}$";
    private static final Set<String> ALLOWED_VEHICLE_TYPES = Set.of("大货车", "中货车", "小货车");

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private OrderRepository orderRepository;

    public PageResult<Vehicle> list(int page, int pageSize) {
        PageRequest pr = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<Vehicle> result = vehicleRepository.findAll(pr);
        return new PageResult<>(result.getContent(), (int) result.getTotalElements());
    }

    @Transactional
    public Vehicle create(Vehicle vehicle) {
        validate(vehicle, null);
        normalizeStrings(vehicle);
        vehicle.setId(null);
        vehicle.setCreateTime(LocalDateTime.now());
        vehicle.setUpdateTime(LocalDateTime.now());
        if (vehicle.getEnabled() == null) {
            vehicle.setEnabled(true);
        }
        if (vehicle.getOnline() == null) {
            vehicle.setOnline(true);
        }
        return vehicleRepository.save(vehicle);
    }

    @Transactional
    public Vehicle update(Long id, Vehicle incoming) {
        Vehicle existing = vehicleRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("车辆不存在"));
        validate(incoming, id);
        existing.setPlateNumber(incoming.getPlateNumber().trim());
        existing.setVehicleType(incoming.getVehicleType().trim());
        existing.setDriverName(incoming.getDriverName().trim());
        existing.setDriverPhone(incoming.getDriverPhone().trim());
        existing.setSpeedLimitKmh(incoming.getSpeedLimitKmh());
        existing.setEnabled(incoming.getEnabled() != null ? incoming.getEnabled() : true);
        if (incoming.getOnline() != null) {
            existing.setOnline(incoming.getOnline());
        }
        existing.setRemark(incoming.getRemark() != null ? incoming.getRemark().trim() : null);
        existing.setUpdateTime(LocalDateTime.now());
        return vehicleRepository.save(existing);
    }

    /**
     * 发货可选：启用且当前无运输中单占用的车辆
     */
    public List<Vehicle> listAvailableForShip() {
        return vehicleRepository.findByEnabledTrueOrderByPlateNumberAsc().stream()
            .filter(v -> !orderRepository.existsByVehicleIdAndStatus(v.getId(), OrderStatusService.STATUS_SHIPPING))
            .toList();
    }

    /**
     * 运输中且已绑车的车辆运行信息（离线、超速优先）
     */
    public List<VehicleMonitoringDTO> listMonitoring() {
        List<Order> shipping = orderRepository.findByStatus(OrderStatusService.STATUS_SHIPPING);
        List<VehicleMonitoringDTO> rows = new ArrayList<>();
        for (Order o : shipping) {
            if (o.getVehicleId() == null) {
                continue;
            }
            Vehicle v = vehicleRepository.findById(o.getVehicleId()).orElse(null);
            if (v == null) {
                continue;
            }
            VehicleMonitoringDTO dto = new VehicleMonitoringDTO();
            dto.setVehicleId(v.getId());
            dto.setPlateNumber(v.getPlateNumber());
            dto.setVehicleType(v.getVehicleType());
            dto.setDriverName(v.getDriverName());
            dto.setDriverPhone(v.getDriverPhone());
            dto.setSpeedLimitKmh(v.getSpeedLimitKmh());
            dto.setOnline(v.getOnline());
            dto.setOrderNo(o.getOrderNo());
            dto.setTrackingNo(o.getTrackingNo());
            int cur = o.getCurrentSpeedKmh() != null ? o.getCurrentSpeedKmh() : 0;
            dto.setCurrentSpeedKmh(cur);
            boolean offline = !Boolean.TRUE.equals(v.getOnline());
            dto.setOfflineAlert(offline);
            dto.setOverspeed(cur > v.getSpeedLimitKmh());
            rows.add(dto);
        }
        rows.sort(Comparator
            .comparingInt((VehicleMonitoringDTO d) -> monitoringPriority(d))
            .reversed()
            .thenComparing(VehicleMonitoringDTO::getPlateNumber, Comparator.nullsLast(String::compareTo)));
        return rows;
    }

    private static int monitoringPriority(VehicleMonitoringDTO d) {
        int p = 0;
        if (d.isOfflineAlert()) {
            p += 100;
        }
        if (d.isOverspeed()) {
            p += 10;
        }
        return p;
    }

    @Transactional
    public void delete(Long id) {
        if (!vehicleRepository.existsById(id)) {
            throw new IllegalArgumentException("车辆不存在");
        }
        vehicleRepository.deleteById(id);
    }

    private void normalizeStrings(Vehicle v) {
        v.setPlateNumber(v.getPlateNumber().trim());
        v.setVehicleType(v.getVehicleType().trim());
        v.setDriverName(v.getDriverName().trim());
        v.setDriverPhone(v.getDriverPhone().trim());
        if (v.getRemark() != null) {
            v.setRemark(v.getRemark().trim());
            if (v.getRemark().isEmpty()) {
                v.setRemark(null);
            }
        }
    }

    private void validate(Vehicle v, Long excludeId) {
        if (v.getPlateNumber() == null || v.getPlateNumber().isBlank()) {
            throw new IllegalArgumentException("车牌不能为空");
        }
        String plate = v.getPlateNumber().trim();
        if (plate.length() > 20) {
            throw new IllegalArgumentException("车牌长度不能超过20个字符");
        }
        if (v.getVehicleType() == null || v.getVehicleType().isBlank()) {
            throw new IllegalArgumentException("车辆类型不能为空");
        }
        String vehicleType = v.getVehicleType().trim();
        if (!ALLOWED_VEHICLE_TYPES.contains(vehicleType)) {
            throw new IllegalArgumentException("车辆类型只能为大货车、中货车、小货车之一");
        }
        if (v.getDriverName() == null || v.getDriverName().isBlank()) {
            throw new IllegalArgumentException("驾驶员姓名不能为空");
        }
        if (v.getDriverPhone() == null || !v.getDriverPhone().trim().matches(PHONE_PATTERN)) {
            throw new IllegalArgumentException("驾驶员电话格式不正确");
        }
        if (v.getSpeedLimitKmh() == null
            || v.getSpeedLimitKmh() < SPEED_MIN
            || v.getSpeedLimitKmh() > SPEED_MAX) {
            throw new IllegalArgumentException("限速必须在 " + SPEED_MIN + "-" + SPEED_MAX + " km/h 之间");
        }

        if (excludeId == null) {
            if (vehicleRepository.existsByPlateNumber(plate)) {
                throw new IllegalArgumentException("车牌已存在");
            }
        } else {
            if (vehicleRepository.existsByPlateNumberAndIdNot(plate, excludeId)) {
                throw new IllegalArgumentException("车牌已存在");
            }
        }
    }
}
