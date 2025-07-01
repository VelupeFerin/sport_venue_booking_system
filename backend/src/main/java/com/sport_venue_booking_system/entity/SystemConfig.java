package com.sport_venue_booking_system.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "system_config")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfig {
    @Id
    @Column(name = "config_key", length = 50)
    private String configKey;
    
    @Column(name = "config_value", nullable = false, length = 100)
    private String configValue;
    
    @Column(nullable = false, length = 200)
    private String description;
} 