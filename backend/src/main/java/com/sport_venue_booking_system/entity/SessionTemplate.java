package com.sport_venue_booking_system.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;

@Entity
@Table(name = "session_template")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "court_name", nullable = false, length = 10)
    @JsonProperty("court_name")
    private String courtName;
    
    @Column(name = "start_time", nullable = false)
    @JsonProperty("start_time")
    private LocalTime startTime;
    
    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal price;
    
    @Column(name = "is_active", nullable = false)
    @JsonProperty("is_active")
    private Boolean isActive = true;
    
    @Column(columnDefinition = "TEXT")
    private String note;
} 