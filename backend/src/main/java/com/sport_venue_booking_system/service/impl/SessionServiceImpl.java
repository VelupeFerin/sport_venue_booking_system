package com.sport_venue_booking_system.service.impl;

import com.sport_venue_booking_system.entity.Session;
import com.sport_venue_booking_system.entity.SessionTemplate;
import com.sport_venue_booking_system.repository.SessionRepository;
import com.sport_venue_booking_system.repository.SessionTemplateRepository;
import com.sport_venue_booking_system.service.SessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SessionServiceImpl implements SessionService {
    
    private static final Logger logger = LoggerFactory.getLogger(SessionServiceImpl.class);
    
    @Autowired
    private SessionRepository sessionRepository;
    
    @Autowired
    private SessionTemplateRepository sessionTemplateRepository;
    
    @Override
    public List<Session> getAllSessions() {
        return sessionRepository.findAll();
    }
    
    @Override
    public Session getSessionById(Long id) {
        return sessionRepository.findById(id).orElse(null);
    }
    
    @Override
    public List<Session> getSessionsByCourtName(String courtName) {
        return sessionRepository.findByCourtNameOrderByStartTime(courtName);
    }
    
    @Override
    public Session getSessionByCourtNameAndStartTime(String courtName, LocalDateTime startTime) {
        return sessionRepository.findByCourtNameAndStartTime(courtName, startTime);
    }
    
    @Override
    public List<Session> getSessionsBetween(LocalDateTime start, LocalDateTime end) {
        return sessionRepository.findByStartTimeBetweenOrderByStartTime(start, end);
    }
    
    @Override
    public List<Session> getAvailableSessions() {
        return sessionRepository.findByIsBookedFalseAndIsActiveTrueOrderByStartTime();
    }
    
    @Override
    public List<Session> getAvailableSessionsBetween(LocalDateTime start, LocalDateTime end) {
        return sessionRepository.findByStartTimeBetweenAndIsBookedFalseAndIsActiveTrueOrderByStartTime(start, end);
    }
    
    @Override
    @Transactional
    public Session createSession(Session session) {
        // 检查是否已存在相同场地和时间的场次
        if (sessionRepository.existsByCourtNameAndStartTime(session.getCourtName(), session.getStartTime())) {
            throw new RuntimeException("该场地和时间段已存在场次");
        }
        return sessionRepository.save(session);
    }
    
    @Override
    @Transactional
    public Session updateSession(Long id, Session session) {
        Session existingSession = sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("场次不存在"));
        
        // 检查是否与其他场次冲突（除了自己）
        if (!existingSession.getCourtName().equals(session.getCourtName()) || 
            !existingSession.getStartTime().equals(session.getStartTime())) {
            if (sessionRepository.existsByCourtNameAndStartTime(session.getCourtName(), session.getStartTime())) {
                throw new RuntimeException("该场地和时间段已存在场次");
            }
        }
        
        existingSession.setCourtName(session.getCourtName());
        existingSession.setStartTime(session.getStartTime());
        existingSession.setPrice(session.getPrice());
        existingSession.setIsActive(session.getIsActive());
        existingSession.setIsBooked(session.getIsBooked());
        existingSession.setNote(session.getNote());
        
        return sessionRepository.save(existingSession);
    }
    
    @Override
    @Transactional
    public void deleteSession(Long id) {
        if (!sessionRepository.existsById(id)) {
            throw new RuntimeException("场次不存在");
        }
        sessionRepository.deleteById(id);
    }
    
    @Override
    @Transactional
    public Session markSessionAsBooked(Long id) {
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("场次不存在"));
        session.setIsBooked(true);
        return sessionRepository.save(session);
    }
    
    @Override
    @Transactional
    public Session markSessionAsAvailable(Long id) {
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("场次不存在"));
        session.setIsBooked(false);
        return sessionRepository.save(session);
    }
    
    @Override
    @Transactional
    public boolean generateNextDaySessions() {
        try {
            logger.info("开始生成次日场次...");
            
            // 获取所有场次模板（包括不激活的，以保留备注信息）
            List<SessionTemplate> templates = sessionTemplateRepository.findAll();
            if (templates.isEmpty()) {
                logger.warn("没有找到场次模板，无法生成场次");
                return false;
            }
            
            // 计算次日日期
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            List<Session> newSessions = new ArrayList<>();
            
            // 根据模板生成场次（包括不激活的模板）
            for (SessionTemplate template : templates) {
                LocalDateTime sessionStartTime = LocalDateTime.of(tomorrow, template.getStartTime());
                
                // 检查是否已存在该场次
                if (!sessionRepository.existsByCourtNameAndStartTime(template.getCourtName(), sessionStartTime)) {
                    Session session = new Session();
                    session.setCourtName(template.getCourtName());
                    session.setStartTime(sessionStartTime);
                    session.setPrice(template.getPrice());
                    session.setIsActive(template.getIsActive()); // 保持模板的激活状态
                    session.setIsBooked(false);
                    session.setNote(template.getNote()); // 保留备注信息
                    
                    newSessions.add(session);
                }
            }
            
            // 批量保存场次
            if (!newSessions.isEmpty()) {
                sessionRepository.saveAll(newSessions);
                logger.info("成功生成 {} 个次日场次", newSessions.size());
            } else {
                logger.info("次日场次已存在，无需重复生成");
            }
            
            return true;
            
        } catch (Exception e) {
            logger.error("生成次日场次失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public void clearExpiredSessions() {
        try {
            LocalDateTime currentTime = LocalDateTime.now();
            sessionRepository.deleteExpiredSessions(currentTime);
            logger.info("已清除过期场次");
        } catch (Exception e) {
            logger.error("清除过期场次失败: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public boolean isSessionTableEmpty() {
        return sessionRepository.count() == 0;
    }
    
    @Override
    @Transactional
    public void initializeSessionsIfEmpty() {
        logger.info("开始初始化场次（直接生成，跳过已存在的场次）...");
        
        // 清除过期场次
        clearExpiredSessions();
        
        // 生成次日场次
        boolean success = generateNextDaySessions();
        if (success) {
            logger.info("场次初始化完成");
        } else {
            logger.error("场次初始化失败");
        }
    }
} 