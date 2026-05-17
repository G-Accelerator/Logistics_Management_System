package com.example.demo.service;

import com.example.demo.dto.OrderParticipantDTO;
import com.example.demo.dto.PageResult;
import com.example.demo.entity.Order;
import com.example.demo.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 从订单表聚合买家/卖家及订单数量统计
 */
@Service
public class OrderParticipantService {

    private final OrderRepository orderRepository;

    public OrderParticipantService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public PageResult<OrderParticipantDTO> listParticipants(
            String role,
            String keyword,
            String status,
            int page,
            int pageSize) {
        boolean seller = "seller".equalsIgnoreCase(role);
        if (!seller && !"buyer".equalsIgnoreCase(role)) {
            throw new IllegalArgumentException("role 须为 buyer 或 seller");
        }

        List<Order> orders = orderRepository.findAll();
        Map<String, List<Order>> grouped = orders.stream()
                .filter(o -> {
                    String phone = seller ? o.getSenderPhone() : o.getReceiverPhone();
                    return phone != null && !phone.isBlank();
                })
                .collect(Collectors.groupingBy(o ->
                        seller ? o.getSenderPhone().trim() : o.getReceiverPhone().trim()));

        List<OrderParticipantDTO> all = new ArrayList<>();
        for (Map.Entry<String, List<Order>> entry : grouped.entrySet()) {
            all.add(toDto(entry.getKey(), entry.getValue(), seller));
        }

        String kw = keyword != null ? keyword.trim().toLowerCase(Locale.ROOT) : "";
        if (!kw.isEmpty()) {
            all = all.stream()
                    .filter(p -> p.getPhone().toLowerCase(Locale.ROOT).contains(kw)
                            || (p.getDisplayName() != null
                                    && p.getDisplayName().toLowerCase(Locale.ROOT).contains(kw)))
                    .collect(Collectors.toList());
        }

        if (status != null && !status.isBlank()) {
            all = all.stream()
                    .filter(p -> countByStatus(p, status) > 0)
                    .collect(Collectors.toList());
        }

        all.sort(Comparator
                .comparingLong(OrderParticipantDTO::getTotalCount).reversed()
                .thenComparing(
                        OrderParticipantDTO::getLastOrderTime,
                        Comparator.nullsLast(Comparator.reverseOrder())));

        int total = all.size();
        int from = Math.max(0, (page - 1) * pageSize);
        int to = Math.min(from + pageSize, total);
        List<OrderParticipantDTO> pageData =
                from >= total ? List.of() : all.subList(from, to);

        return new PageResult<>(pageData, total);
    }

    private OrderParticipantDTO toDto(String phone, List<Order> list, boolean seller) {
        Order latest = list.stream()
                .max(Comparator.comparing(Order::getId, Comparator.nullsLast(Long::compareTo)))
                .orElse(list.get(0));
        String name = seller ? latest.getSenderName() : latest.getReceiverName();
        if (name == null || name.isBlank()) {
            name = "—";
        }

        long pending = list.stream().filter(o -> "pending".equals(o.getStatus())).count();
        long shipping = list.stream().filter(o -> "shipping".equals(o.getStatus())).count();
        long completed = list.stream().filter(o -> "completed".equals(o.getStatus())).count();
        long cancelled = list.stream().filter(o -> "cancelled".equals(o.getStatus())).count();

        String lastTime = list.stream()
                .map(Order::getCreateTime)
                .filter(t -> t != null && !t.isBlank())
                .max(String::compareTo)
                .orElse("");

        return new OrderParticipantDTO(
                phone, name, pending, shipping, completed, cancelled, list.size(), lastTime);
    }

    private long countByStatus(OrderParticipantDTO p, String status) {
        return switch (status) {
            case "pending" -> p.getPendingCount();
            case "shipping" -> p.getShippingCount();
            case "completed" -> p.getCompletedCount();
            case "cancelled" -> p.getCancelledCount();
            default -> 0;
        };
    }
}
