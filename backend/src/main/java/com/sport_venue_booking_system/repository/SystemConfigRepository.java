package com.sport_venue_booking_system.repository;

import com.sport_venue_booking_system.entity.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, String> {
    
    Optional<SystemConfig> findByConfigKey(String configKey);
    
    List<SystemConfig> findByConfigKeyIn(List<String> configKeys);
} 