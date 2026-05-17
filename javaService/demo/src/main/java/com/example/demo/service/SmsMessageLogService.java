package com.example.demo.service;

import com.example.demo.dto.PageResult;
import com.example.demo.dto.SmsMessageLogDTO;
import com.example.demo.entity.Order;
import com.example.demo.entity.SmsMessageLog;
import com.example.demo.entity.SmsTemplateType;
import com.example.demo.entity.Vehicle;
import com.example.demo.repository.SmsMessageLogRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SmsMessageLogService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final Map<SmsTemplateType, String> TYPE_LABELS = Map.of(
        SmsTemplateType.OVERSPEED, "超速预警",
        SmsTemplateType.OFFLINE, "离线预警",
        SmsTemplateType.REGULAR, "常规通知"
    );

    private final SmsMessageLogRepository smsMessageLogRepository;

    public SmsMessageLogService(SmsMessageLogRepository smsMessageLogRepository) {
        this.smsMessageLogRepository = smsMessageLogRepository;
    }

    @Transactional
    public void record(
        SmsTemplateType templateType,
        String triggerSource,
        Vehicle vehicle,
        Order order,
        String content
    ) {
        SmsMessageLog log = new SmsMessageLog();
        log.setTemplateType(templateType);
        log.setTriggerSource(triggerSource);
        log.setRecipientPhone(vehicle.getDriverPhone());
        log.setPlateNumber(vehicle.getPlateNumber());
        log.setVehicleId(vehicle.getId());
        log.setContent(content);
        log.setSentAt(LocalDateTime.now());
        if (order != null) {
            log.setOrderNo(order.getOrderNo());
        }
        smsMessageLogRepository.save(log);
    }

    public PageResult<SmsMessageLogDTO> query(
        int page,
        int pageSize,
        String templateType,
        String triggerSource,
        String orderNo,
        String plateNumber,
        String recipientPhone,
        LocalDateTime startTime,
        LocalDateTime endTime
    ) {
        Specification<SmsMessageLog> spec = buildSpec(
            templateType, triggerSource, orderNo, plateNumber, recipientPhone, startTime, endTime);
        PageRequest pr = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "sentAt"));
        Page<SmsMessageLog> result = smsMessageLogRepository.findAll(spec, pr);
        List<SmsMessageLogDTO> rows = result.getContent().stream().map(this::toDto).toList();
        return new PageResult<>(rows, (int) result.getTotalElements());
    }

    private Specification<SmsMessageLog> buildSpec(
        String templateType,
        String triggerSource,
        String orderNo,
        String plateNumber,
        String recipientPhone,
        LocalDateTime startTime,
        LocalDateTime endTime
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (templateType != null && !templateType.isBlank()) {
                try {
                    SmsTemplateType type = SmsTemplateType.valueOf(templateType.trim().toUpperCase());
                    predicates.add(cb.equal(root.get("templateType"), type));
                } catch (IllegalArgumentException ignored) {
                    predicates.add(cb.disjunction());
                }
            }
            if (triggerSource != null && !triggerSource.isBlank()) {
                predicates.add(cb.like(root.get("triggerSource"), "%" + triggerSource.trim() + "%"));
            }
            if (orderNo != null && !orderNo.isBlank()) {
                predicates.add(cb.like(root.get("orderNo"), "%" + orderNo.trim() + "%"));
            }
            if (plateNumber != null && !plateNumber.isBlank()) {
                predicates.add(cb.like(root.get("plateNumber"), "%" + plateNumber.trim() + "%"));
            }
            if (recipientPhone != null && !recipientPhone.isBlank()) {
                predicates.add(cb.like(root.get("recipientPhone"), "%" + recipientPhone.trim() + "%"));
            }
            if (startTime != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("sentAt"), startTime));
            }
            if (endTime != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("sentAt"), endTime));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private SmsMessageLogDTO toDto(SmsMessageLog log) {
        SmsMessageLogDTO dto = new SmsMessageLogDTO();
        dto.setId(log.getId());
        dto.setTemplateType(log.getTemplateType().name());
        dto.setTemplateTypeLabel(TYPE_LABELS.getOrDefault(log.getTemplateType(), log.getTemplateType().name()));
        dto.setTriggerSource(log.getTriggerSource());
        dto.setRecipientPhone(log.getRecipientPhone());
        dto.setPlateNumber(log.getPlateNumber());
        dto.setOrderNo(log.getOrderNo());
        dto.setVehicleId(log.getVehicleId());
        dto.setContent(log.getContent());
        if (log.getSentAt() != null) {
            dto.setSentAt(log.getSentAt().format(FMT));
        }
        return dto;
    }
}
