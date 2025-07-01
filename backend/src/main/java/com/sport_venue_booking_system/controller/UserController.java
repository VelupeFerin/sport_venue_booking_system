package com.sport_venue_booking_system.controller;

import com.sport_venue_booking_system.common.ResultCode;
import com.sport_venue_booking_system.dto.ApiResponse;
import com.sport_venue_booking_system.dto.OrderVerificationResponse;
import com.sport_venue_booking_system.dto.UserUpdateRequest;
import com.sport_venue_booking_system.entity.User;
import com.sport_venue_booking_system.service.OrderService;
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

    @GetMapping("/info")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ApiResponse<User> getUserInfo() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            // 添加日志记录
            System.out.println("Getting user info for username: " + username);
            
            User user = userService.getUserInfo(username);
            if (user == null) {
                System.out.println("User not found for username: " + username);
                return ApiResponse.error(ResultCode.USER_NOT_FOUND, "用户不存在");
            }
            
            // 不返回密码信息
            user.setPassword(null);
            System.out.println("Successfully retrieved user info for: " + username);
            return ApiResponse.success(user);
        } catch (Exception e) {
            System.out.println("Error getting user info: " + e.getMessage());
            e.printStackTrace();
            return ApiResponse.error(ResultCode.FAIL, "获取用户信息失败: " + e.getMessage());
        }
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ApiResponse<String> updateUserInfo(@RequestBody UserUpdateRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();
            
            // 添加日志记录
            System.out.println("Updating user info for username: " + currentUsername);
            System.out.println("Request data: " + request);
            
            String result = userService.updateUserInfo(currentUsername, request);
            if (result == null) {
                System.out.println("User info updated successfully for: " + currentUsername);
                return ApiResponse.success("用户信息更新成功");
            } else if (result.startsWith("NEW_TOKEN:")) {
                // 用户名发生变化，返回新的token
                String newToken = result.substring(10);
                System.out.println("Username changed, new token generated for: " + currentUsername);
                return ApiResponse.success("用户信息更新成功", newToken);
            } else {
                System.out.println("User info update failed: " + result);
                return ApiResponse.error(ResultCode.FAIL, result);
            }
        } catch (Exception e) {
            System.out.println("Error updating user info: " + e.getMessage());
            e.printStackTrace();
            return ApiResponse.error(ResultCode.FAIL, "更新用户信息失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/orders")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ApiResponse<Object> getUserOrders(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            User user = userService.getUserInfo(username);
            if (user == null) {
                return ApiResponse.error(ResultCode.USER_NOT_FOUND, "用户不存在");
            }
            
            // 如果提供了分页参数，使用分页查询
            if (page != null && size != null) {
                org.springframework.data.domain.Page<OrderVerificationResponse> ordersPage = 
                    orderService.getUserOrders(user.getId(), page, size);
                return ApiResponse.success(ordersPage);
            } else {
                // 保持向后兼容，返回所有订单
                List<OrderVerificationResponse> orders = orderService.getUserOrders(user.getId());
                return ApiResponse.success(orders);
            }
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


} 