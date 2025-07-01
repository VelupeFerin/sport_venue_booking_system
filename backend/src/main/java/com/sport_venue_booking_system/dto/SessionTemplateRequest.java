package com.sport_venue_booking_system.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SessionTemplateRequest {
    @JsonProperty("courtName")
    private String courtName;
    
    @JsonProperty("startTime")
    private String startTime;
    
    private BigDecimal price;
    
    @JsonProperty("isActive")
    private Boolean isActive;
    
    private String note;
} 