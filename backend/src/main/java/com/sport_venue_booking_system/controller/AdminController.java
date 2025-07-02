package com.sport_venue_booking_system.controller;

import com.sport_venue_booking_system.common.ResultCode;
import com.sport_venue_booking_system.dto.ApiResponse;
import com.sport_venue_booking_system.dto.OrderVerificationResponse;
import com.sport_venue_booking_system.dto.SessionTemplateRequest;
import com.sport_venue_booking_system.entity.SessionTemplate;
import com.sport_venue_booking_system.service.OrderService;
import com.sport_venue_booking_system.service.SessionTemplateService;
import com.sport_venue_booking_system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private SessionTemplateService sessionTemplateService;
    
    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> getStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 获取总订单数
            long totalOrders = orderService.getTotalOrders();
            stats.put("totalOrders", totalOrders);
            
            // 获取总用户数
            long totalUsers = userService.getTotalUsers();
            stats.put("totalUsers", totalUsers);
            
            // 获取今日订单数
            long todayOrders = orderService.getTodayOrders();
            stats.put("todayOrders", todayOrders);
            
            // 获取待核验订单数
            long pendingVerification = orderService.getPendingVerificationOrders();
            stats.put("pendingVerification", pendingVerification);
            
            return ApiResponse.success(stats);
        } catch (Exception e) {
            return ApiResponse.error(ResultCode.INTERNAL_ERROR, "获取统计数据失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/order/{orderId}")
    public ApiResponse<OrderVerificationResponse> getOrderForVerification(@PathVariable Long orderId) {
        OrderVerificationResponse order = orderService.getOrderForVerification(orderId);
        if (order == null) {
            return ApiResponse.error(ResultCode.ORDER_NOT_FOUND, "订单不存在");
        }
        return ApiResponse.success(order);
    }
    
    @PostMapping("/order/{orderId}/verify")
    public ApiResponse<String> verifyOrder(@PathVariable Long orderId) {
        boolean success = orderService.verifyOrder(orderId);
        if (!success) {
            return ApiResponse.error(ResultCode.ORDER_ALREADY_VERIFIED, "订单已经核验或不存在");
        }
        return ApiResponse.success("订单核验成功");
    }
    
    // 模板管理API
    @GetMapping("/templates")
    public ApiResponse<List<SessionTemplate>> getTemplates() {
        try {
            List<SessionTemplate> templates = sessionTemplateService.getAllTemplates();
            return ApiResponse.success(templates);
        } catch (Exception e) {
            return ApiResponse.error(ResultCode.INTERNAL_ERROR, "获取模板失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/templates")
    public ApiResponse<SessionTemplate> createTemplate(@RequestBody SessionTemplateRequest request) {
        try {
            SessionTemplate template = new SessionTemplate();
            template.setCourtName(request.getCourtName());
            template.setStartTime(LocalTime.parse(request.getStartTime()));
            template.setPrice(request.getPrice());
            template.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
            template.setNote(request.getNote());
            
            SessionTemplate savedTemplate = sessionTemplateService.createTemplate(template);
            return ApiResponse.success(savedTemplate);
        } catch (Exception e) {
            return ApiResponse.error(ResultCode.INTERNAL_ERROR, "创建模板失败: " + e.getMessage());
        }
    }
    
    @PutMapping("/templates/{id}")
    public ApiResponse<SessionTemplate> updateTemplate(@PathVariable Long id, @RequestBody SessionTemplateRequest request) {
        try {
            SessionTemplate template = new SessionTemplate();
            template.setCourtName(request.getCourtName());
            template.setStartTime(LocalTime.parse(request.getStartTime()));
            template.setPrice(request.getPrice());
            template.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
            template.setNote(request.getNote());
            
            SessionTemplate updatedTemplate = sessionTemplateService.updateTemplate(id, template);
            return ApiResponse.success(updatedTemplate);
        } catch (Exception e) {
            return ApiResponse.error(ResultCode.INTERNAL_ERROR, "更新模板失败: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/templates/{id}")
    public ApiResponse<String> deleteTemplate(@PathVariable Long id) {
        try {
            sessionTemplateService.deleteTemplate(id);
            return ApiResponse.success("删除模板成功");
        } catch (Exception e) {
            return ApiResponse.error(ResultCode.INTERNAL_ERROR, "删除模板失败: " + e.getMessage());
        }
    }
    

} 