package com.sport_venue_booking_system.dto;

import com.sport_venue_booking_system.common.ResultCode;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private Integer code;
    private String message;
    private T data;
    
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, ResultCode.SUCCESS.getCode(), "操作成功", data);
    }
    
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, ResultCode.SUCCESS.getCode(), message, data);
    }
    
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, ResultCode.FAIL.getCode(), message, null);
    }
    
    public static <T> ApiResponse<T> error(ResultCode resultCode, String message) {
        return new ApiResponse<>(false, resultCode.getCode(), message, null);
    }
    
    public static <T> ApiResponse<T> error(ResultCode resultCode) {
        return new ApiResponse<>(false, resultCode.getCode(), resultCode.getMessage(), null);
    }
} 