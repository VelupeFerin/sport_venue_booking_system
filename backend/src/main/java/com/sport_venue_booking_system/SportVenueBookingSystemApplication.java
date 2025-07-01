package com.sport_venue_booking_system;

import com.sport_venue_booking_system.service.SessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // 启用定时任务
public class SportVenueBookingSystemApplication implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(SportVenueBookingSystemApplication.class);
    
    @Autowired
    private SessionService sessionService;
    
    public static void main(String[] args) {
        SpringApplication.run(SportVenueBookingSystemApplication.class, args);
    }
    
    @Override
    public void run(String... args) throws Exception {
        logger.info("应用启动完成，开始初始化场次...");
        
        try {
            // 初始化场次，如果完全相同的场次已存在则跳过
            sessionService.initializeSessionsIfEmpty();
            logger.info("场次初始化完成");
        } catch (Exception e) {
            logger.error("场次初始化失败: {}", e.getMessage(), e);
        }
    }
}
