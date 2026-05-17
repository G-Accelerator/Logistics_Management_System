package com.example.demo.service;

import com.example.demo.dto.SmsTemplateDTO;
import com.example.demo.entity.SmsTemplate;
import com.example.demo.entity.SmsTemplateType;
import com.example.demo.repository.SmsTemplateRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SmsTemplateService implements ApplicationRunner {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SmsTemplateRepository smsTemplateRepository;

    public SmsTemplateService(SmsTemplateRepository smsTemplateRepository) {
        this.smsTemplateRepository = smsTemplateRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        ensureDefault(SmsTemplateType.OVERSPEED,
            "【物流预警】车牌{plateNumber}当前{currentSpeedKmh}km/h，超过限速{speedLimitKmh}km/h，订单{orderNo}，请立即减速。");
        ensureDefault(SmsTemplateType.OFFLINE,
            "【物流预警】车牌{plateNumber}驾驶员{driverName}设备离线，订单{orderNo}，请检查终端。");
        ensureDefault(SmsTemplateType.REGULAR,
            "【物流通知】{driverName}您好，订单{orderNo}运输中，如有疑问请联系调度。");
    }

    private void ensureDefault(SmsTemplateType type, String content) {
        if (smsTemplateRepository.findByType(type).isEmpty()) {
            SmsTemplate t = new SmsTemplate();
            t.setType(type);
            t.setContent(content);
            t.setUpdateTime(LocalDateTime.now());
            smsTemplateRepository.save(t);
        }
    }

    public List<SmsTemplateDTO> listAll() {
        return Arrays.stream(SmsTemplateType.values())
            .map(type -> smsTemplateRepository.findByType(type)
                .map(this::toDto)
                .orElseThrow(() -> new IllegalStateException("缺少模板: " + type)))
            .collect(Collectors.toList());
    }

    @Transactional
    public SmsTemplateDTO update(SmsTemplateType type, String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("模板内容不能为空");
        }
        if (content.length() > 2000) {
            throw new IllegalArgumentException("模板内容不能超过2000字");
        }
        SmsTemplate t = smsTemplateRepository.findByType(type)
            .orElseThrow(() -> new IllegalArgumentException("模板不存在"));
        t.setContent(content.trim());
        t.setUpdateTime(LocalDateTime.now());
        return toDto(smsTemplateRepository.save(t));
    }

    public String getContent(SmsTemplateType type) {
        return smsTemplateRepository.findByType(type)
            .map(SmsTemplate::getContent)
            .orElseThrow(() -> new IllegalStateException("缺少模板: " + type));
    }

    public static SmsTemplateType parseType(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("模板类型不能为空");
        }
        try {
            return SmsTemplateType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("无效的模板类型: " + raw);
        }
    }

    private SmsTemplateDTO toDto(SmsTemplate t) {
        SmsTemplateDTO dto = new SmsTemplateDTO();
        dto.setType(t.getType().name());
        dto.setContent(t.getContent());
        if (t.getUpdateTime() != null) {
            dto.setUpdateTime(t.getUpdateTime().format(FMT));
        }
        return dto;
    }
}
