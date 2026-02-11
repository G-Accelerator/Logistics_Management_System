package com.example.demo.repository;

import com.example.demo.entity.OperationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {
    
    List<OperationLog> findByOrderNoOrderByOperateTimeDesc(String orderNo);
    
    List<OperationLog> findByAction(String action);
}
