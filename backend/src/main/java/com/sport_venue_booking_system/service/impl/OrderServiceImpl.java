package com.sport_venue_booking_system.service.impl;

import com.sport_venue_booking_system.dto.OrderVerificationResponse;
import com.sport_venue_booking_system.entity.Order;
import com.sport_venue_booking_system.entity.OrderSession;
import com.sport_venue_booking_system.entity.User;
import com.sport_venue_booking_system.entity.Session;
import com.sport_venue_booking_system.repository.OrderRepository;
import com.sport_venue_booking_system.repository.OrderSessionRepository;
import com.sport_venue_booking_system.repository.UserRepository;
import com.sport_venue_booking_system.service.OrderService;
import com.sport_venue_booking_system.service.SessionService;
import com.sport_venue_booking_system.service.SystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private OrderSessionRepository orderSessionRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SessionService sessionService;
    
    @Autowired
    private SystemConfigService systemConfigService;
    
    @Override
    public OrderVerificationResponse getOrderForVerification(Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return null;
        }
        
        User user = userRepository.findById(order.getUserId()).orElse(null);
        if (user == null) {
            return null;
        }
        
        List<OrderSession> orderSessions = orderSessionRepository.findByOrderId(orderId);
        if (orderSessions.isEmpty()) {
            return null;
        }
        
        // 构建用户信息
        OrderVerificationResponse.UserInfo userInfo = new OrderVerificationResponse.UserInfo(
                user.getUsername(),
                user.getPhone()
        );
        
        // 构建所有场次信息
        List<OrderVerificationResponse.SessionInfo> sessionInfos = orderSessions.stream()
                .map(orderSession -> new OrderVerificationResponse.SessionInfo(
                        orderSession.getCourtName(),
                        orderSession.getStartTime(),
                        orderSession.getPrice()
                ))
                .toList();
        
        // 构建响应对象
        return new OrderVerificationResponse(
                orderId.toString(), // 使用订单ID作为订单号
                order.getTotalPrice(),
                order.getCreateTime(),
                order.getVerifyTime(),
                order.getStatus().name(),
                userInfo,
                sessionInfos
        );
    }
    
    @Override
    @Transactional
    public boolean verifyOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return false;
        }
        
        if (order.getStatus() != Order.OrderStatus.pending) {
            return false;
        }
        
        order.setStatus(Order.OrderStatus.completed);
        order.setVerifyTime(LocalDateTime.now());
        orderRepository.save(order);
        
        return true;
    }
    
    @Override
    public long getTotalOrders() {
        return orderRepository.count();
    }
    
    @Override
    public long getTodayOrders() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
        return orderRepository.countByCreateTimeBetween(startOfDay, endOfDay);
    }
    
    @Override
    public long getPendingVerificationOrders() {
        return orderRepository.countByStatus(Order.OrderStatus.pending);
    }
    
    @Override
    public List<OrderVerificationResponse> getUserOrders(Long userId) {
        List<Order> userOrders = orderRepository.findByUserIdOrderByCreateTimeDesc(userId);
        
        return userOrders.stream()
                .map(order -> {
                    User user = userRepository.findById(order.getUserId()).orElse(null);
                    if (user == null) {
                        return null;
                    }
                    
                    List<OrderSession> orderSessions = orderSessionRepository.findByOrderId(order.getId());
                    
                    // 构建用户信息
                    OrderVerificationResponse.UserInfo userInfo = new OrderVerificationResponse.UserInfo(
                            user.getUsername(),
                            user.getPhone()
                    );
                    
                    // 构建所有场次信息
                    List<OrderVerificationResponse.SessionInfo> sessionInfos = orderSessions.stream()
                            .map(orderSession -> new OrderVerificationResponse.SessionInfo(
                                    orderSession.getCourtName(),
                                    orderSession.getStartTime(),
                                    orderSession.getPrice()
                            ))
                            .toList();
                    
                    // 构建响应对象
                    return new OrderVerificationResponse(
                            order.getId().toString(),
                            order.getTotalPrice(),
                            order.getCreateTime(),
                            order.getVerifyTime(),
                            order.getStatus().name(),
                            userInfo,
                            sessionInfos
                    );
                })
                .filter(response -> response != null)
                .toList();
    }
    
    @Override
    @Transactional
    public boolean cancelUserOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return false;
        }
        
        // 验证订单是否属于该用户
        if (!order.getUserId().equals(userId)) {
            return false;
        }
        
        // 只有待确认状态的订单可以取消
        if (order.getStatus() != Order.OrderStatus.pending) {
            return false;
        }
        
        // 获取订单的所有场次
        List<OrderSession> orderSessions = orderSessionRepository.findByOrderId(orderId);
        if (orderSessions.isEmpty()) {
            return false;
        }
        
        // 检查退订时间限制
        String cancelTimeLimitStr = systemConfigService.getConfigValue("cancel_time_limit");
        int cancelTimeLimit = 4; // 默认4小时
        if (cancelTimeLimitStr != null) {
            try {
                cancelTimeLimit = Integer.parseInt(cancelTimeLimitStr);
            } catch (NumberFormatException e) {
                // 使用默认值
            }
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        // 检查是否有场次已经接近开始时间
        for (OrderSession orderSession : orderSessions) {
            LocalDateTime sessionStartTime = orderSession.getStartTime();
            long hoursUntilStart = java.time.Duration.between(now, sessionStartTime).toHours();
            
            if (hoursUntilStart < cancelTimeLimit) {
                String errorMessage;
                if (hoursUntilStart < 0) {
                    errorMessage = "场次已开始，无法取消订单";
                } else {
                    errorMessage = "距场次开始时间不足 " + cancelTimeLimit + " 小时，无法取消订单";
                }
                throw new RuntimeException(errorMessage);
            }
        }
        
        // 取消订单，将场次标记为可预订
        order.setStatus(Order.OrderStatus.cancelled);
        orderRepository.save(order);
        
        // 将相关场次标记为可预订
        for (OrderSession orderSession : orderSessions) {
            Session session = sessionService.getSessionByCourtNameAndStartTime(
                orderSession.getCourtName(), 
                orderSession.getStartTime()
            );
            if (session != null) {
                session.setIsBooked(false);
                sessionService.updateSession(session.getId(), session);
            }
        }
        
        return true;
    }
    
    @Override
    @Transactional
    public Order createOrder(Long userId, List<Long> sessionIds) {
        // 验证用户是否存在
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 验证场次是否存在且可预订
        List<Session> sessions = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;
        
        for (Long sessionId : sessionIds) {
            Session session = sessionService.getSessionById(sessionId);
            if (session == null) {
                throw new RuntimeException("场次不存在: " + sessionId);
            }
            if (session.getIsBooked()) {
                throw new RuntimeException("场次已被预订: " + session.getCourtName() + " " + session.getStartTime());
            }
            if (!session.getIsActive()) {
                throw new RuntimeException("场次不可预订: " + session.getCourtName() + " " + session.getStartTime());
            }
            sessions.add(session);
            totalPrice = totalPrice.add(session.getPrice());
        }
        
        // 检查订单场次数限制
        String maxOrderSessionsStr = systemConfigService.getConfigValue("max_order_sessions");
        int maxOrderSessions = 3; // 默认值
        if (maxOrderSessionsStr != null) {
            try {
                maxOrderSessions = Integer.parseInt(maxOrderSessionsStr);
            } catch (NumberFormatException e) {
                // 使用默认值
            }
        }
        
        if (sessionIds.size() > maxOrderSessions) {
            throw new RuntimeException("订单场次数超过限制，最多可预订 " + maxOrderSessions + " 个场次");
        }
        
        // 检查用户是否有未核验的订单
        List<Order> pendingOrders = orderRepository.findByUserIdAndStatusOrderByCreateTimeDesc(userId, Order.OrderStatus.pending);
        if (!pendingOrders.isEmpty()) {
            throw new RuntimeException("您有未核验的订单，请等待核验完成后再预订");
        }
        
        // 创建订单
        Order order = new Order();
        order.setUserId(userId);
        order.setTotalPrice(totalPrice);
        order.setCreateTime(LocalDateTime.now());
        order.setStatus(Order.OrderStatus.pending);
        
        Order savedOrder = orderRepository.save(order);
        
        // 创建订单场次快照
        List<OrderSession> orderSessions = new ArrayList<>();
        for (Session session : sessions) {
            OrderSession orderSession = new OrderSession();
            orderSession.setOrderId(savedOrder.getId());
            orderSession.setCourtName(session.getCourtName());
            orderSession.setStartTime(session.getStartTime());
            orderSession.setPrice(session.getPrice());
            orderSessions.add(orderSession);
            
            // 标记场次为已预订
            session.setIsBooked(true);
            sessionService.updateSession(session.getId(), session);
        }
        
        orderSessionRepository.saveAll(orderSessions);
        
        return savedOrder;
    }
} 