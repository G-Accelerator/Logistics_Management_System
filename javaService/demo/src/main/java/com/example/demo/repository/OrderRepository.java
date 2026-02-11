package com.example.demo.repository;

import com.example.demo.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    
    Optional<Order> findByOrderNo(String orderNo);
    
    Optional<Order> findByTrackingNo(String trackingNo);
    
    List<Order> findByStatus(String status);
    
    List<Order> findBySenderPhone(String senderPhone);
    
    List<Order> findByReceiverPhone(String receiverPhone);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    long countByStatus(@Param("status") String status);
    
    @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
    List<Object[]> countGroupByStatus();
    
    boolean existsByOrderNo(String orderNo);
    
    void deleteByOrderNo(String orderNo);
}
