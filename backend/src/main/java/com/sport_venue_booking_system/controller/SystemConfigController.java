package com.sport_venue_booking_system.controller;

import com.sport_venue_booking_system.common.ResultCode;
import com.sport_venue_booking_system.dto.ApiResponse;
import com.sport_venue_booking_system.service.SystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/config")
@CrossOrigin(origins = "*")
public class SystemConfigController {
    
    @Autowired
    private SystemConfigService systemConfigService;
    
    /**
     * 获取所有系统配置（公开API，所有用户都可以访问）
     */
    @GetMapping
    public ApiResponse<Map<String, String>> getAllConfigs() {
        try {
            Map<String, String> configs = systemConfigService.getAllConfigs();
            return ApiResponse.success(configs);
        } catch (Exception e) {
            return ApiResponse.error(ResultCode.INTERNAL_ERROR, "获取系统配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新系统配置（仅管理员可访问）
     */
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> updateConfigs(@RequestBody Map<String, String> configs) {
        try {
            systemConfigService.updateConfigs(configs);
            return ApiResponse.success("系统配置更新成功");
        } catch (Exception e) {
            return ApiResponse.error(ResultCode.INTERNAL_ERROR, "更新系统配置失败: " + e.getMessage());
        }
    }
} 