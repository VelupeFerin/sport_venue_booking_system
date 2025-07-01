package com.sport_venue_booking_system.service.impl;

import com.sport_venue_booking_system.entity.SystemConfig;
import com.sport_venue_booking_system.repository.SystemConfigRepository;
import com.sport_venue_booking_system.service.SystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SystemConfigServiceImpl implements SystemConfigService {
    
    @Autowired
    private SystemConfigRepository systemConfigRepository;
    
    @Override
    public Map<String, String> getAllConfigs() {
        List<SystemConfig> configs = systemConfigRepository.findAll();
        Map<String, String> configMap = new HashMap<>();
        
        for (SystemConfig config : configs) {
            configMap.put(config.getConfigKey(), config.getConfigValue());
        }
        
        return configMap;
    }
    
    @Override
    public String getConfigValue(String configKey) {
        return systemConfigRepository.findByConfigKey(configKey)
                .map(SystemConfig::getConfigValue)
                .orElse(null);
    }
    
    @Override
    public void updateConfig(String configKey, String configValue) {
        SystemConfig config = systemConfigRepository.findByConfigKey(configKey)
                .orElse(new SystemConfig());
        
        config.setConfigKey(configKey);
        config.setConfigValue(configValue);
        
        // 保持原有的描述
        if (config.getDescription() == null) {
            switch (configKey) {
                case "venue_name":
                    config.setDescription("场馆名称");
                    break;
                case "max_order_sessions":
                    config.setDescription("单次订单最大场次数");
                    break;
                case "cancel_time_limit":
                    config.setDescription("退订时间要求(小时)");
                    break;
                case "business_hours":
                    config.setDescription("营业时间");
                    break;
                default:
                    config.setDescription("系统配置");
            }
        }
        
        systemConfigRepository.save(config);
    }
    
    @Override
    public void updateConfigs(Map<String, String> configs) {
        for (Map.Entry<String, String> entry : configs.entrySet()) {
            updateConfig(entry.getKey(), entry.getValue());
        }
    }
    
    @Override
    public List<SystemConfig> getAllConfigEntities() {
        return systemConfigRepository.findAll();
    }
} 