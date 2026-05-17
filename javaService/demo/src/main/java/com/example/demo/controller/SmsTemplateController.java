package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.SmsTemplateDTO;
import com.example.demo.dto.SmsTemplateUpdateRequest;
import com.example.demo.entity.SmsTemplateType;
import com.example.demo.service.SmsTemplateService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/system/sms-templates")
@CrossOrigin(origins = "*")
public class SmsTemplateController {

    private final SmsTemplateService smsTemplateService;

    public SmsTemplateController(SmsTemplateService smsTemplateService) {
        this.smsTemplateService = smsTemplateService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SmsTemplateDTO>>> list() {
        return ResponseEntity.ok(ApiResponse.success(smsTemplateService.listAll()));
    }

    @PutMapping("/{type}")
    public ResponseEntity<ApiResponse<SmsTemplateDTO>> update(
        @PathVariable String type,
        @RequestBody SmsTemplateUpdateRequest body
    ) {
        try {
            SmsTemplateType templateType = SmsTemplateService.parseType(type);
            SmsTemplateDTO updated = smsTemplateService.update(templateType, body.getContent());
            return ResponseEntity.ok(ApiResponse.success("保存成功", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400, e.getMessage()));
        }
    }
}
