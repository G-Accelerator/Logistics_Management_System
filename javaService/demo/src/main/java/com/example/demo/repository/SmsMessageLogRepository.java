package com.example.demo.repository;

import com.example.demo.entity.SmsMessageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SmsMessageLogRepository
    extends JpaRepository<SmsMessageLog, Long>, JpaSpecificationExecutor<SmsMessageLog> {
}
