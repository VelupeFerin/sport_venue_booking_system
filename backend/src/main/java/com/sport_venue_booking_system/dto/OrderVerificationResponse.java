package com.sport_venue_booking_system.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderVerificationResponse {
    private String orderNumber;
    private BigDecimal totalPrice;
    private LocalDateTime createTime;
    private LocalDateTime verifyTime;
    private String status;
    private UserInfo user;
    private List<SessionInfo> sessions;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private String username;
        private String phone;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionInfo {
        private String courtName;
        private LocalDateTime startTime;
        private BigDecimal price;
    }
} 