package com.sport_venue_booking_system.service;

import com.sport_venue_booking_system.dto.OrderVerificationResponse;
import com.sport_venue_booking_system.entity.Order;

import java.util.List;

public interface OrderService {
    OrderVerificationResponse getOrderForVerification(Long orderId);
    boolean verifyOrder(Long orderId);
    
    // 统计数据方法
    long getTotalOrders();
    long getTodayOrders();
    long getPendingVerificationOrders();
    
    // 用户订单相关方法
    List<OrderVerificationResponse> getUserOrders(Long userId);
    boolean cancelUserOrder(Long orderId, Long userId);
    
    // 创建订单方法
    Order createOrder(Long userId, List<Long> sessionIds);
} 