package com.sport_venue_booking_system.service;

import com.sport_venue_booking_system.entity.Session;

import java.time.LocalDateTime;
import java.util.List;

public interface SessionService {
    
    /**
     * 获取所有场次
     */
    List<Session> getAllSessions();
    
    /**
     * 根据ID获取场次
     */
    Session getSessionById(Long id);
    
    /**
     * 根据场地名称获取场次
     */
    List<Session> getSessionsByCourtName(String courtName);
    
    /**
     * 根据场地名称和开始时间获取场次
     */
    Session getSessionByCourtNameAndStartTime(String courtName, LocalDateTime startTime);
    
    /**
     * 获取指定日期范围内的场次
     */
    List<Session> getSessionsBetween(LocalDateTime start, LocalDateTime end);
    
    /**
     * 获取可预订的场次
     */
    List<Session> getAvailableSessions();
    
    /**
     * 获取指定日期范围内的可预订场次
     */
    List<Session> getAvailableSessionsBetween(LocalDateTime start, LocalDateTime end);
    
    /**
     * 创建场次
     */
    Session createSession(Session session);
    
    /**
     * 更新场次
     */
    Session updateSession(Long id, Session session);
    
    /**
     * 删除场次
     */
    void deleteSession(Long id);
    
    /**
     * 标记场次为已预订
     */
    Session markSessionAsBooked(Long id);
    
    /**
     * 标记场次为未预订
     */
    Session markSessionAsAvailable(Long id);
    
    /**
     * 根据模板生成次日场次
     */
    boolean generateNextDaySessions();
    
    /**
     * 清除过期场次
     */
    void clearExpiredSessions();
    
    /**
     * 检查场次表是否为空
     */
    boolean isSessionTableEmpty();
    
    /**
     * 初始化场次（如果表为空则生成场次）
     */
    void initializeSessionsIfEmpty();
} 