package com.sport_venue_booking_system.dto;

import lombok.Data;

@Data
public class UserUpdateRequest {
    private String username;
    private String oldPassword;
    private String newPassword;
    private String phone;
} 