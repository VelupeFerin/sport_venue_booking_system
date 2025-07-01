package com.sport_venue_booking_system.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_session")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "order_id", nullable = false)
    private Long orderId;
    
    @Column(name = "court_name", nullable = false, length = 10)
    private String courtName;
    
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    
    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal price;
} 