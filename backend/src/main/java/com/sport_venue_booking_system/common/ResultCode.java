package com.sport_venue_booking_system.common;

import lombok.Getter;

@Getter
public enum ResultCode {
    SUCCESS(200, "操作成功"),
    FAIL(400, "操作失败"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),
    
    // 用户相关错误
    USERNAME_ALREADY_EXIST(1001, "用户名已存在"),
    USER_NOT_FOUND(1002, "用户不存在"),
    PASSWORD_ERROR(1003, "密码错误"),
    INVALID_OLD_PASSWORD(1004, "原密码错误"),
    
    // 订单相关错误
    ORDER_NOT_FOUND(2001, "订单不存在"),
    ORDER_ALREADY_VERIFIED(2002, "订单已经核验"),
    ORDER_VERIFICATION_FAILED(2003, "订单核验失败");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

} 