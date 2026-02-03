package com.example.demo.service;

import com.example.demo.dto.BatchResult;
import com.example.demo.entity.OperationLog;
import com.example.demo.entity.Order;
import com.example.demo.repository.OperationLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 订单状态转换服务
 * 实现状态转换规则验证和状态变更操作
 */
@Service
public class OrderStatusService {

    private static final Logger log = LoggerFactory.getLogger(OrderStatusService.class);

    // 状态常量
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_SHIPPING = "shipping";
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_CANCELLED = "cancelled";

    // 操作类型常量
    public static final String ACTION_SHIP = "ship";
    public static final String ACTION_RECEIVE = "receive";
    public static final String ACTION_CANCEL = "cancel";

    // 状态转换规则：key为当前状态，value为允许转换到的目标状态集合
    private static final Map<String, Set<String>> VALID_TRANSITIONS = Map.of(
        STATUS_PENDING, Set.of(STATUS_SHIPPING, STATUS_CANCELLED),
        STATUS_SHIPPING, Set.of(STATUS_COMPLETED, STATUS_CANCELLED),
        STATUS_COMPLETED, Set.of(STATUS_CANCELLED),
        STATUS_CANCELLED, Set.of()
    );

    private final OrderService orderService;
    private final OperationLogRepository operationLogRepository;

    public OrderStatusService(OrderService orderService, OperationLogRepository operationLogRepository) {
        this.orderService = orderService;
        this.operationLogRepository = operationLogRepository;
    }


    /**
     * 检查状态转换是否合法
     * @param fromStatus 当前状态
     * @param toStatus 目标状态
     * @return 是否允许转换
     */
    public boolean canTransition(String fromStatus, String toStatus) {
        if (fromStatus == null || toStatus == null) {
            return false;
        }
        Set<String> allowedTargets = VALID_TRANSITIONS.get(fromStatus);
        return allowedTargets != null && allowedTargets.contains(toStatus);
    }

    /**
     * 发货操作：pending -> shipping
     * @param orderNo 订单号
     * @return 更新后的订单
     * @throws IllegalStateException 如果状态转换非法
     * @throws IllegalArgumentException 如果订单不存在
     */
    public Order ship(String orderNo) {
        Order order = getOrderOrThrow(orderNo);
        String fromStatus = order.getStatus();

        if (!canTransition(fromStatus, STATUS_SHIPPING)) {
            throw new IllegalStateException(
                String.format("订单 %s 当前状态为 %s，不允许发货操作", orderNo, fromStatus));
        }

        order.setStatus(STATUS_SHIPPING);
        order.setShipTime(LocalDateTime.now());

        // 持久化订单变更
        orderService.updateOrder(order);

        // 记录操作日志
        saveOperationLog(orderNo, ACTION_SHIP, fromStatus, STATUS_SHIPPING);

        log.info("订单 {} 发货成功，状态从 {} 变更为 {}", orderNo, fromStatus, STATUS_SHIPPING);
        return order;
    }

    /**
     * 签收操作：shipping -> completed
     * @param orderNo 订单号
     * @return 更新后的订单
     * @throws IllegalStateException 如果状态转换非法
     * @throws IllegalArgumentException 如果订单不存在
     */
    public Order receive(String orderNo) {
        Order order = getOrderOrThrow(orderNo);
        String fromStatus = order.getStatus();

        if (!canTransition(fromStatus, STATUS_COMPLETED)) {
            throw new IllegalStateException(
                String.format("订单 %s 当前状态为 %s，不允许签收操作", orderNo, fromStatus));
        }

        order.setStatus(STATUS_COMPLETED);
        order.setReceiveTime(LocalDateTime.now());

        // 持久化订单变更
        orderService.updateOrder(order);

        // 记录操作日志
        saveOperationLog(orderNo, ACTION_RECEIVE, fromStatus, STATUS_COMPLETED);

        log.info("订单 {} 签收成功，状态从 {} 变更为 {}", orderNo, fromStatus, STATUS_COMPLETED);
        return order;
    }

    /**
     * 取消操作：any -> cancelled
     * @param orderNo 订单号
     * @return 更新后的订单
     * @throws IllegalStateException 如果状态转换非法（已取消的订单不能再取消）
     * @throws IllegalArgumentException 如果订单不存在
     */
    public Order cancel(String orderNo) {
        Order order = getOrderOrThrow(orderNo);
        String fromStatus = order.getStatus();

        if (!canTransition(fromStatus, STATUS_CANCELLED)) {
            throw new IllegalStateException(
                String.format("订单 %s 当前状态为 %s，不允许取消操作", orderNo, fromStatus));
        }

        order.setStatus(STATUS_CANCELLED);
        order.setCancelTime(LocalDateTime.now());

        // 持久化订单变更
        orderService.updateOrder(order);

        // 记录操作日志
        saveOperationLog(orderNo, ACTION_CANCEL, fromStatus, STATUS_CANCELLED);

        log.info("订单 {} 取消成功，状态从 {} 变更为 {}", orderNo, fromStatus, STATUS_CANCELLED);
        return order;
    }


    /**
     * 批量发货
     * @param orderNos 订单号列表
     * @return 批量操作结果
     */
    public BatchResult batchShip(List<String> orderNos) {
        if (orderNos == null || orderNos.isEmpty()) {
            BatchResult result = new BatchResult(false, 0, new ArrayList<>());
            result.setMessage("订单号列表不能为空");
            return result;
        }

        int successCount = 0;
        List<String> failedOrders = new ArrayList<>();

        for (String orderNo : orderNos) {
            try {
                ship(orderNo);
                successCount++;
            } catch (Exception e) {
                log.warn("批量发货失败，订单号: {}，原因: {}", orderNo, e.getMessage());
                failedOrders.add(orderNo);
            }
        }

        boolean allSuccess = failedOrders.isEmpty();
        BatchResult result = new BatchResult(allSuccess, successCount, failedOrders);
        
        if (allSuccess) {
            result.setMessage(String.format("批量发货成功，共处理 %d 个订单", successCount));
        } else {
            result.setMessage(String.format("批量发货完成，成功 %d 个，失败 %d 个", 
                successCount, failedOrders.size()));
        }

        log.info("批量发货完成: 成功 {} 个，失败 {} 个", successCount, failedOrders.size());
        return result;
    }

    /**
     * 批量签收
     * @param orderNos 订单号列表
     * @return 批量操作结果
     */
    public BatchResult batchReceive(List<String> orderNos) {
        if (orderNos == null || orderNos.isEmpty()) {
            BatchResult result = new BatchResult(false, 0, new ArrayList<>());
            result.setMessage("订单号列表不能为空");
            return result;
        }

        int successCount = 0;
        List<String> failedOrders = new ArrayList<>();

        for (String orderNo : orderNos) {
            try {
                receive(orderNo);
                successCount++;
            } catch (Exception e) {
                log.warn("批量签收失败，订单号: {}，原因: {}", orderNo, e.getMessage());
                failedOrders.add(orderNo);
            }
        }

        boolean allSuccess = failedOrders.isEmpty();
        BatchResult result = new BatchResult(allSuccess, successCount, failedOrders);
        
        if (allSuccess) {
            result.setMessage(String.format("批量签收成功，共处理 %d 个订单", successCount));
        } else {
            result.setMessage(String.format("批量签收完成，成功 %d 个，失败 %d 个", 
                successCount, failedOrders.size()));
        }

        log.info("批量签收完成: 成功 {} 个，失败 {} 个", successCount, failedOrders.size());
        return result;
    }

    /**
     * 获取订单，如果不存在则抛出异常
     */
    private Order getOrderOrThrow(String orderNo) {
        Order order = orderService.getOrder(orderNo);
        if (order == null) {
            throw new IllegalArgumentException(String.format("订单 %s 不存在", orderNo));
        }
        return order;
    }

    /**
     * 保存操作日志
     */
    private void saveOperationLog(String orderNo, String action, String fromStatus, String toStatus) {
        OperationLog log = new OperationLog(orderNo, action, fromStatus, toStatus, "system");
        operationLogRepository.save(log);
    }

    /**
     * 获取订单操作日志
     * @param orderNo 订单号
     * @return 操作日志列表
     */
    public List<OperationLog> getOperationLogs(String orderNo) {
        return operationLogRepository.findByOrderNo(orderNo);
    }
}
