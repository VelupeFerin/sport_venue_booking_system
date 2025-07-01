package com.sport_venue_booking_system.repository;

import com.sport_venue_booking_system.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findById(Long id);
    
    // 统计查询方法
    long countByCreateTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    long countByStatus(Order.OrderStatus status);
    
    // 用户订单查询方法
    List<Order> findByUserIdOrderByCreateTimeDesc(Long userId);
    
    // 根据用户ID和状态查询订单
    List<Order> findByUserIdAndStatusOrderByCreateTimeDesc(Long userId, Order.OrderStatus status);
} 