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
    
    // 用户相关错误 (1000-1099)
    USERNAME_ALREADY_EXIST(1001, "用户名已存在"),
    USER_NOT_FOUND(1002, "用户不存在"),
    PASSWORD_ERROR(1003, "密码错误"),
    INVALID_OLD_PASSWORD(1004, "原密码错误"),
    USER_PERMISSION_DENIED(1005, "用户权限不足"),
    PHONE_NUMBER_INVALID(1006, "手机号格式不正确"),
    USERNAME_INVALID(1007, "用户名格式不正确"),
    
    // 订单相关错误 (2000-2099)
    ORDER_NOT_FOUND(2001, "订单不存在"),
    ORDER_ALREADY_VERIFIED(2002, "订单已经核验"),
    ORDER_VERIFICATION_FAILED(2004, "订单核验失败"),
    ORDER_EXCEED_MAX_SESSIONS(2005, "订单场次数量超过最大限制"),
    ORDER_HAS_UNVERIFIED_ORDERS(2006, "存在未核验订单，无法继续预订"),
    ORDER_CANCEL_TIME_LIMIT(2007, "距离开始时间不足，无法退订"),
    ORDER_ALREADY_CANCELLED(2008, "订单已退订"),
    ORDER_SESSION_UNAVAILABLE(2009, "订单包含不可预订的场次"),
    
    // 场次相关错误 (3000-3099)
    SESSION_NOT_FOUND(3001, "场次不存在"),
    SESSION_ALREADY_BOOKED(3002, "场次已被预订"),
    SESSION_EXPIRED(3003, "场次已过期"),
    SESSION_UNAVAILABLE(3004, "场次不可预订"),
    SESSION_GENERATION_FAILED(3005, "场次自动生成失败"),
    SESSION_TEMPLATE_NOT_FOUND(3006, "场次模板不存在"),
    SESSION_TIME_CONFLICT(3007, "场次时间冲突"),
    SESSION_OUT_OF_BUSINESS_HOURS(3008, "场次时间超出营业时间"),
    
    // 场次模板相关错误 (4000-4099)
    SESSION_TEMPLATE_ALREADY_EXISTS(4001, "场次模板已存在"),
    SESSION_TEMPLATE_INVALID(4002, "场次模板配置无效"),
    SESSION_TEMPLATE_IMPORT_FAILED(4003, "场次模板导入失败"),
    SESSION_TEMPLATE_EXPORT_FAILED(4004, "场次模板导出失败"),
    SESSION_TEMPLATE_BATCH_UPDATE_FAILED(4005, "场次模板批量更新失败"),
    
    // 系统配置相关错误 (5000-5099)
    SYSTEM_CONFIG_NOT_FOUND(5001, "系统配置不存在"),
    SYSTEM_CONFIG_INVALID(5002, "系统配置参数无效"),
    SYSTEM_CONFIG_UPDATE_FAILED(5003, "系统配置更新失败"),
    VENUE_NAME_EMPTY(5004, "场馆名称不能为空"),
    MAX_ORDER_SESSIONS_INVALID(5005, "最大订单场次数配置无效"),
    CANCEL_TIME_REQUIREMENT_INVALID(5006, "退订时间要求配置无效"),
    BUSINESS_HOURS_INVALID(5007, "营业时间配置无效"),
    
    // 数据验证相关错误 (6000-6099)
    PARAMETER_MISSING(6001, "缺少必要参数"),
    PARAMETER_INVALID(6002, "参数格式不正确"),
    DATA_VALIDATION_FAILED(6003, "数据验证失败"),
    DUPLICATE_DATA(6004, "数据重复"),
    
    // 业务逻辑相关错误 (7000-7099)
    BUSINESS_RULE_VIOLATION(7001, "违反业务规则"),
    OPERATION_NOT_ALLOWED(7002, "操作不被允许"),
    RESOURCE_IN_USE(7003, "资源正在使用中"),
    INSUFFICIENT_PERMISSIONS(7004, "权限不足"),
    
    // 外部服务相关错误 (8000-8099)
    EXTERNAL_SERVICE_ERROR(8001, "外部服务错误"),
    NETWORK_ERROR(8002, "网络连接错误"),
    SERVICE_UNAVAILABLE(8003, "服务不可用");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

} 