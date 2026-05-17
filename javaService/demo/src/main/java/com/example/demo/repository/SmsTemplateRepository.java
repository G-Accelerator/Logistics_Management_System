package com.example.demo.repository;

import com.example.demo.entity.SmsTemplate;
import com.example.demo.entity.SmsTemplateType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SmsTemplateRepository extends JpaRepository<SmsTemplate, Long> {

    Optional<SmsTemplate> findByType(SmsTemplateType type);
}
