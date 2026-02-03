package com.example.demo.repository;

import com.example.demo.entity.OperationLog;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 操作日志仓储类
 * 使用内存存储操作日志（与现有 OrderService 保持一致的存储方式）
 */
@Repository
public class OperationLogRepository {
    private final ConcurrentHashMap<Long, OperationLog> logs = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    /**
     * 保存操作日志
     */
    public OperationLog save(OperationLog log) {
        if (log.getId() == null) {
            log.setId(idGenerator.getAndIncrement());
        }
        logs.put(log.getId(), log);
        return log;
    }

    /**
     * 根据订单号查询操作日志
     */
    public List<OperationLog> findByOrderNo(String orderNo) {
        return logs.values().stream()
                .filter(log -> orderNo.equals(log.getOrderNo()))
                .sorted((a, b) -> b.getOperateTime().compareTo(a.getOperateTime()))
                .collect(Collectors.toList());
    }

    /**
     * 查询所有操作日志
     */
    public List<OperationLog> findAll() {
        return new ArrayList<>(logs.values());
    }
}
