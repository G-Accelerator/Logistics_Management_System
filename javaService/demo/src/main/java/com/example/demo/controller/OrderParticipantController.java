package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.OrderParticipantDTO;
import com.example.demo.dto.PageResult;
import com.example.demo.service.OrderParticipantService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/system/order-participants")
@CrossOrigin(origins = "*")
public class OrderParticipantController {

    private final OrderParticipantService orderParticipantService;

    public OrderParticipantController(OrderParticipantService orderParticipantService) {
        this.orderParticipantService = orderParticipantService;
    }

    /**
     * 从订单聚合买家/卖家列表及订单数量统计
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResult<OrderParticipantDTO>>> list(
            @RequestParam String role,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            PageResult<OrderParticipantDTO> result =
                    orderParticipantService.listParticipants(role, keyword, status, page, pageSize);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(400, e.getMessage()));
        }
    }
}
