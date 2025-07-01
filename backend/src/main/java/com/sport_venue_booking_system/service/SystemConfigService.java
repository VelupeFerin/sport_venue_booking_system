package com.sport_venue_booking_system.service;

import com.sport_venue_booking_system.entity.SystemConfig;

import java.util.List;
import java.util.Map;

public interface SystemConfigService {
    
    Map<String, String> getAllConfigs();
    
    String getConfigValue(String configKey);
    
    void updateConfig(String configKey, String configValue);
    
    void updateConfigs(Map<String, String> configs);
    
    List<SystemConfig> getAllConfigEntities();
} 