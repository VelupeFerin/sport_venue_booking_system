package com.sport_venue_booking_system.service.impl;

import com.sport_venue_booking_system.entity.SessionTemplate;
import com.sport_venue_booking_system.repository.SessionTemplateRepository;
import com.sport_venue_booking_system.service.SessionTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;

@Service
public class SessionTemplateServiceImpl implements SessionTemplateService {
    
    @Autowired
    private SessionTemplateRepository sessionTemplateRepository;
    
    @Override
    public List<SessionTemplate> getAllTemplates() {
        return sessionTemplateRepository.findAll();
    }
    
    @Override
    public SessionTemplate getTemplateById(Long id) {
        return sessionTemplateRepository.findById(id).orElse(null);
    }
    
    @Override
    public SessionTemplate createTemplate(SessionTemplate template) {
        // 检查是否已存在相同场地和时间的模板
        if (sessionTemplateRepository.existsByCourtNameAndStartTime(template.getCourtName(), template.getStartTime())) {
            throw new RuntimeException("该场地和时间段已存在模板");
        }
        return sessionTemplateRepository.save(template);
    }
    
    @Override
    public SessionTemplate updateTemplate(Long id, SessionTemplate template) {
        SessionTemplate existingTemplate = sessionTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("模板不存在"));
        
        // 检查是否与其他模板冲突（除了自己）
        if (!existingTemplate.getCourtName().equals(template.getCourtName()) || 
            !existingTemplate.getStartTime().equals(template.getStartTime())) {
            if (sessionTemplateRepository.existsByCourtNameAndStartTime(template.getCourtName(), template.getStartTime())) {
                throw new RuntimeException("该场地和时间段已存在模板");
            }
        }
        
        existingTemplate.setCourtName(template.getCourtName());
        existingTemplate.setStartTime(template.getStartTime());
        existingTemplate.setPrice(template.getPrice());
        existingTemplate.setIsActive(template.getIsActive());
        existingTemplate.setNote(template.getNote());
        
        return sessionTemplateRepository.save(existingTemplate);
    }
    
    @Override
    public void deleteTemplate(Long id) {
        if (!sessionTemplateRepository.existsById(id)) {
            throw new RuntimeException("模板不存在");
        }
        sessionTemplateRepository.deleteById(id);
    }
    
    @Override
    public List<SessionTemplate> getTemplatesByCourtName(String courtName) {
        return sessionTemplateRepository.findByCourtNameOrderByStartTime(courtName);
    }
    
    @Override
    public boolean existsByCourtNameAndStartTime(String courtName, LocalTime startTime) {
        return sessionTemplateRepository.existsByCourtNameAndStartTime(courtName, startTime);
    }
} 