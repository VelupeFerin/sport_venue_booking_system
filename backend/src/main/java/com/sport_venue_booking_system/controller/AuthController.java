package com.sport_venue_booking_system.controller;

import com.sport_venue_booking_system.common.ResultCode;
import com.sport_venue_booking_system.dto.ApiResponse;
import com.sport_venue_booking_system.dto.LoginRequest;
import com.sport_venue_booking_system.dto.LoginResponse;
import com.sport_venue_booking_system.dto.RegisterRequest;
import com.sport_venue_booking_system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ApiResponse<String> register(@RequestBody RegisterRequest request) {
        try {
            String error = userService.register(request);
            if (error != null) {
                return ApiResponse.error(ResultCode.USERNAME_ALREADY_EXIST, error);
            }
            return ApiResponse.success("注册成功", null);
        } catch (Exception e) {
            return ApiResponse.error(ResultCode.FAIL, "注册失败: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = userService.login(request);
            return ApiResponse.success("登录成功", response);
        } catch (RuntimeException e) {
            return ApiResponse.error(ResultCode.PASSWORD_ERROR, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(ResultCode.FAIL, "登录失败: " + e.getMessage());
        }
    }
} 