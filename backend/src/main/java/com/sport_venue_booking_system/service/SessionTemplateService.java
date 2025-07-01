package com.sport_venue_booking_system.service;

import com.sport_venue_booking_system.entity.SessionTemplate;

import java.util.List;

public interface SessionTemplateService {
    
    List<SessionTemplate> getAllTemplates();
    
    SessionTemplate getTemplateById(Long id);
    
    SessionTemplate createTemplate(SessionTemplate template);
    
    SessionTemplate updateTemplate(Long id, SessionTemplate template);
    
    void deleteTemplate(Long id);
    
    List<SessionTemplate> getTemplatesByCourtName(String courtName);
    
    boolean existsByCourtNameAndStartTime(String courtName, java.time.LocalTime startTime);
} 