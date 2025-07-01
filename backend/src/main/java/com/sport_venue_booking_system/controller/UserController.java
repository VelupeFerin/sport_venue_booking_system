package com.sport_venue_booking_system.controller;

import com.sport_venue_booking_system.common.ResultCode;
import com.sport_venue_booking_system.dto.ApiResponse;
import com.sport_venue_booking_system.dto.OrderVerificationResponse;
import com.sport_venue_booking_system.dto.UserUpdateRequest;
import com.sport_venue_booking_system.entity.User;
import com.sport_venue_booking_system.service.OrderService;
import com.sport_venue_booking_system.service.SystemConfigService;
import com.sport_venue_booking_system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import com.sport_venue_booking_system.dto.CreateOrderRequest;
import com.sport_venue_booking_system.entity.Order;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private SystemConfigService systemConfigService;

    @GetMapping("/info")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ApiResponse<User> getUserInfo() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            User user = userService.getUserInfo(username);
            if (user == null) {
                return ApiResponse.error(ResultCode.USER_NOT_FOUND, "用户不存在");
            }
            
            // 不返回密码信息
            user.setPassword(null);
            return ApiResponse.success(user);
        } catch (Exception e) {
            return ApiResponse.error(ResultCode.FAIL, "获取用户信息失败: " + e.getMessage());
        }
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ApiResponse<String> updateUserInfo(@RequestBody UserUpdateRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();
            
            String result = userService.updateUserInfo(currentUsername, request);
            if (result == null) {
                return ApiResponse.success("用户信息更新成功");
            } else if (result.startsWith("NEW_TOKEN:")) {
                // 用户名发生变化，返回新的token
                String newToken = result.substring(10);
                return ApiResponse.success("用户信息更新成功", newToken);
            } else {
                return ApiResponse.error(ResultCode.FAIL, result);
            }
        } catch (Exception e) {
            return ApiResponse.error(ResultCode.FAIL, "更新用户信息失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/orders")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ApiResponse<List<OrderVerificationResponse>> getUserOrders() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            User user = userService.getUserInfo(username);
            if (user == null) {
                return ApiResponse.error(ResultCode.USER_NOT_FOUND, "用户不存在");
            }
            
            List<OrderVerificationResponse> orders = orderService.getUserOrders(user.getId());
            return ApiResponse.success(orders);
        } catch (Exception e) {
            return ApiResponse.error(ResultCode.FAIL, "获取订单列表失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/orders/{orderId}/cancel")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ApiResponse<String> cancelOrder(@PathVariable String orderId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            User user = userService.getUserInfo(username);
            if (user == null) {
                return ApiResponse.error(ResultCode.USER_NOT_FOUND, "用户不存在");
            }
            
            // 将字符串订单ID转换为Long类型
            Long orderIdLong;
            try {
                orderIdLong = Long.parseLong(orderId);
            } catch (NumberFormatException e) {
                return ApiResponse.error(ResultCode.FAIL, "订单ID格式错误");
            }
            
            boolean success = orderService.cancelUserOrder(orderIdLong, user.getId());
            if (success) {
                return ApiResponse.success("订单取消成功");
            } else {
                return ApiResponse.error(ResultCode.FAIL, "订单取消失败");
            }
        } catch (Exception e) {
            return ApiResponse.error(ResultCode.FAIL, "取消订单失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/orders")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> createOrder(@RequestBody CreateOrderRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            User user = userService.getUserInfo(username);
            if (user == null) {
                return ApiResponse.error(ResultCode.USER_NOT_FOUND, "用户不存在");
            }
            
            if (request.getSessionIds() == null || request.getSessionIds().isEmpty()) {
                return ApiResponse.error(ResultCode.FAIL, "请选择要预订的场次");
            }
            
            Order order = orderService.createOrder(user.getId(), request.getSessionIds());
            
            Map<String, Object> result = Map.of(
                "orderId", order.getId(),
                "totalPrice", order.getTotalPrice(),
                "message", "订单创建成功"
            );
            
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error(ResultCode.FAIL, "创建订单失败: " + e.getMessage());
        }
    }

    // 获取营业时间配置（公开API，无需认证）
    @GetMapping("/business-hours")
    public ApiResponse<Map<String, String>> getBusinessHours() {
        try {
            Map<String, String> configs = systemConfigService.getAllConfigs();
            // 返回营业时间、取消时间限制和最大场次数相关的配置
            Map<String, String> businessConfig = Map.of(
                "business_hours", configs.getOrDefault("business_hours", "09:00-21:00"),
                "cancel_time_limit", configs.getOrDefault("cancel_time_limit", "4"),
                "max_order_sessions", configs.getOrDefault("max_order_sessions", "3")
            );
            return ApiResponse.success(businessConfig);
        } catch (Exception e) {
            return ApiResponse.error(ResultCode.INTERNAL_ERROR, "获取营业时间配置失败: " + e.getMessage());
        }
    }
} 