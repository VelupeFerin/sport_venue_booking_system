package com.sport_venue_booking_system.repository;

import com.sport_venue_booking_system.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // 统计查询方法
    long countByCreateTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    long countByStatus(Order.OrderStatus status);
    
    // 用户订单查询方法
    List<Order> findByUserIdOrderByCreateTimeDesc(Long userId);
    
    // 分页查询用户订单
    Page<Order> findByUserIdOrderByCreateTimeDesc(Long userId, Pageable pageable);
    
    // 根据用户ID和状态查询订单
    List<Order> findByUserIdAndStatusOrderByCreateTimeDesc(Long userId, Order.OrderStatus status);
} 