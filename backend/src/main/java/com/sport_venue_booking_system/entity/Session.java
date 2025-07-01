package com.sport_venue_booking_system.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "session")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Session {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "court_name", nullable = false, length = 10)
    @JsonProperty("court_name")
    private String courtName;
    
    @Column(name = "start_time", nullable = false)
    @JsonProperty("start_time")
    private LocalDateTime startTime;
    
    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal price;
    
    @Column(name = "is_active", nullable = false)
    @JsonProperty("is_active")
    private Boolean isActive = true;
    
    @Column(name = "is_booked", nullable = false)
    @JsonProperty("is_booked")
    private Boolean isBooked = false;
    
    @Column(columnDefinition = "TEXT")
    private String note;
} 