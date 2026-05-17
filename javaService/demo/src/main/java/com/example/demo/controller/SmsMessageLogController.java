package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.PageResult;
import com.example.demo.dto.SmsMessageLogDTO;
import com.example.demo.service.SmsMessageLogService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/sms-messages")
@CrossOrigin(origins = "*")
public class SmsMessageLogController {

    private final SmsMessageLogService smsMessageLogService;

    public SmsMessageLogController(SmsMessageLogService smsMessageLogService) {
        this.smsMessageLogService = smsMessageLogService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResult<SmsMessageLogDTO>>> list(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int pageSize,
        @RequestParam(required = false) String templateType,
        @RequestParam(required = false) String triggerSource,
        @RequestParam(required = false) String orderNo,
        @RequestParam(required = false) String plateNumber,
        @RequestParam(required = false) String recipientPhone,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime
    ) {
        PageResult<SmsMessageLogDTO> result = smsMessageLogService.query(
            page, pageSize, templateType, triggerSource, orderNo, plateNumber, recipientPhone, startTime, endTime);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
