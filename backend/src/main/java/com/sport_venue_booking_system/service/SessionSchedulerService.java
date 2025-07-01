package com.sport_venue_booking_system.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class SessionSchedulerService {
    
    private static final Logger logger = LoggerFactory.getLogger(SessionSchedulerService.class);
    private static final int MAX_RETRY_ATTEMPTS = 3;
    
    @Autowired
    private SessionService sessionService;
    
    /**
     * 每日0点自动生成次日场次
     * 根据所有场次模板生成场次，包括不开放的场次（以保留备注信息）
     * 如果生成失败，最多重试3次
     */
    @Scheduled(cron = "0 0 0 * * ?") // 每天0点执行
    public void generateNextDaySessionsScheduled() {
        logger.info("开始执行每日场次生成任务（包括不开放的场次）...");
        
        int attemptCount = 0;
        boolean success = false;
        
        while (attemptCount < MAX_RETRY_ATTEMPTS && !success) {
            attemptCount++;
            logger.info("第 {} 次尝试生成次日场次", attemptCount);
            
            try {
                // 清除过期场次
                sessionService.clearExpiredSessions();
                
                // 生成次日场次
                success = sessionService.generateNextDaySessions();
                
                if (success) {
                    logger.info("场次生成成功，共尝试 {} 次", attemptCount);
                } else {
                    logger.warn("场次生成失败，第 {} 次尝试", attemptCount);
                    if (attemptCount < MAX_RETRY_ATTEMPTS) {
                        // 等待5秒后重试
                        Thread.sleep(5000);
                    }
                }
                
            } catch (Exception e) {
                logger.error("场次生成过程中发生异常，第 {} 次尝试: {}", attemptCount, e.getMessage(), e);
                if (attemptCount < MAX_RETRY_ATTEMPTS) {
                    try {
                        // 等待5秒后重试
                        Thread.sleep(5000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        logger.error("重试等待被中断", ie);
                        break;
                    }
                }
            }
        }
        
        if (!success) {
            logger.error("场次生成最终失败，已尝试 {} 次", MAX_RETRY_ATTEMPTS);
        }
    }
} 