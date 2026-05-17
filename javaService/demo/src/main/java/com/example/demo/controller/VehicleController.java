package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.PageResult;
import com.example.demo.dto.VehicleMonitoringDTO;
import com.example.demo.dto.VehicleNotifyRequest;
import com.example.demo.entity.SmsTemplateType;
import com.example.demo.entity.Vehicle;
import com.example.demo.service.SmsSimulationService;
import com.example.demo.service.SmsTemplateService;
import com.example.demo.service.VehicleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@CrossOrigin(origins = "*")
public class VehicleController {

    private final VehicleService vehicleService;
    private final SmsSimulationService smsSimulationService;

    public VehicleController(VehicleService vehicleService, SmsSimulationService smsSimulationService) {
        this.vehicleService = vehicleService;
        this.smsSimulationService = smsSimulationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResult<Vehicle>>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        PageResult<Vehicle> result = vehicleService.list(page, pageSize);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/available-for-ship")
    public ResponseEntity<ApiResponse<List<Vehicle>>> availableForShip() {
        return ResponseEntity.ok(ApiResponse.success(vehicleService.listAvailableForShip()));
    }

    @GetMapping("/monitoring")
    public ResponseEntity<ApiResponse<List<VehicleMonitoringDTO>>> monitoring() {
        return ResponseEntity.ok(ApiResponse.success(vehicleService.listMonitoring()));
    }

    @PostMapping("/notify")
    public ResponseEntity<ApiResponse<String>> notify(@RequestBody VehicleNotifyRequest body) {
        try {
            SmsTemplateType type = SmsTemplateService.parseType(body.getTemplateType());
            String sent = smsSimulationService.sendManual(
                body.getVehicleId(), body.getOrderNo(), type, body.getContent());
            return ResponseEntity.ok(ApiResponse.success("消息已发送（已输出至服务端控制台）", sent));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400, e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400, e.getMessage()));
        }
    }

    @GetMapping("/notify/preview")
    public ResponseEntity<ApiResponse<String>> notifyPreview(
        @RequestParam Long vehicleId,
        @RequestParam String orderNo,
        @RequestParam String templateType
    ) {
        try {
            SmsTemplateType type = SmsTemplateService.parseType(templateType);
            String preview = smsSimulationService.preview(type, vehicleId, orderNo);
            return ResponseEntity.ok(ApiResponse.success(preview));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400, e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Vehicle>> create(@RequestBody Vehicle vehicle) {
        try {
            Vehicle created = vehicleService.create(vehicle);
            return ResponseEntity.ok(ApiResponse.success("创建成功", created));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400, e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Vehicle>> update(
            @PathVariable Long id,
            @RequestBody Vehicle vehicle) {
        try {
            Vehicle updated = vehicleService.update(id, vehicle);
            return ResponseEntity.ok(ApiResponse.success("更新成功", updated));
        } catch (IllegalArgumentException e) {
            int code = e.getMessage().contains("不存在") ? 404 : 400;
            HttpStatus status = code == 404 ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status)
                .body(ApiResponse.error(code, e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        try {
            vehicleService.delete(id);
            return ResponseEntity.ok(ApiResponse.success("删除成功", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(404, e.getMessage()));
        }
    }
}
