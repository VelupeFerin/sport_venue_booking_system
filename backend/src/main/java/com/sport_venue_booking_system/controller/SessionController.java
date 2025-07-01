package com.sport_venue_booking_system.controller;

import com.sport_venue_booking_system.dto.ApiResponse;
import com.sport_venue_booking_system.entity.Session;
import com.sport_venue_booking_system.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@CrossOrigin(origins = "*")
public class SessionController {
    
    @Autowired
    private SessionService sessionService;
    
    /**
     * 获取所有场次
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Session>>> getAllSessions() {
        try {
            List<Session> sessions = sessionService.getAllSessions();
            return ResponseEntity.ok(ApiResponse.success(sessions));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("获取场次失败: " + e.getMessage()));
        }
    }
    
    /**
     * 根据ID获取场次
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Session>> getSessionById(@PathVariable Long id) {
        try {
            Session session = sessionService.getSessionById(id);
            if (session != null) {
                return ResponseEntity.ok(ApiResponse.success(session));
            } else {
                return ResponseEntity.ok(ApiResponse.error("场次不存在"));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("获取场次失败: " + e.getMessage()));
        }
    }
    
    /**
     * 根据场地名称获取场次
     */
    @GetMapping("/court/{courtName}")
    public ResponseEntity<ApiResponse<List<Session>>> getSessionsByCourtName(@PathVariable String courtName) {
        try {
            List<Session> sessions = sessionService.getSessionsByCourtName(courtName);
            return ResponseEntity.ok(ApiResponse.success(sessions));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("获取场次失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取指定日期范围内的场次
     */
    @GetMapping("/range")
    public ResponseEntity<ApiResponse<List<Session>>> getSessionsBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        try {
            List<Session> sessions = sessionService.getSessionsBetween(start, end);
            return ResponseEntity.ok(ApiResponse.success(sessions));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("获取场次失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取可预订的场次
     */
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<Session>>> getAvailableSessions() {
        try {
            List<Session> sessions = sessionService.getAvailableSessions();
            return ResponseEntity.ok(ApiResponse.success(sessions));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("获取可预订场次失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取指定日期范围内的可预订场次
     */
    @GetMapping("/available/range")
    public ResponseEntity<ApiResponse<List<Session>>> getAvailableSessionsBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        try {
            List<Session> sessions = sessionService.getAvailableSessionsBetween(start, end);
            return ResponseEntity.ok(ApiResponse.success(sessions));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("获取可预订场次失败: " + e.getMessage()));
        }
    }
    
    /**
     * 根据日期获取场次（公开API，无需认证）
     * 返回指定日期的所有场次，包括已预订和不可预订的场次
     */
    @GetMapping("/date/{date}")
    public ResponseEntity<ApiResponse<List<Session>>> getSessionsByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            // 计算日期的开始和结束时间
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
            
            List<Session> sessions = sessionService.getSessionsBetween(startOfDay, endOfDay);
            return ResponseEntity.ok(ApiResponse.success(sessions));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("获取场次失败: " + e.getMessage()));
        }
    }
    
    /**
     * 根据日期获取可预订场次（公开API，无需认证）
     * 只返回可预订的场次
     */
    @GetMapping("/available/date/{date}")
    public ResponseEntity<ApiResponse<List<Session>>> getAvailableSessionsByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            // 计算日期的开始和结束时间
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
            
            List<Session> sessions = sessionService.getAvailableSessionsBetween(startOfDay, endOfDay);
            return ResponseEntity.ok(ApiResponse.success(sessions));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("获取可预订场次失败: " + e.getMessage()));
        }
    }
    
    /**
     * 手动生成次日场次（管理员功能）
     * 根据所有场次模板生成次日场次，包括不开放的场次（以保留备注信息）
     */
    @PostMapping("/generate-next-day")
    public ResponseEntity<ApiResponse<String>> generateNextDaySessions() {
        try {
            boolean success = sessionService.generateNextDaySessions();
            if (success) {
                return ResponseEntity.ok(ApiResponse.success("次日场次生成成功"));
            } else {
                return ResponseEntity.ok(ApiResponse.error("次日场次生成失败"));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("生成场次失败: " + e.getMessage()));
        }
    }
    
    /**
     * 清除过期场次（管理员功能）
     */
    @DeleteMapping("/clear-expired")
    public ResponseEntity<ApiResponse<String>> clearExpiredSessions() {
        try {
            sessionService.clearExpiredSessions();
            return ResponseEntity.ok(ApiResponse.success("过期场次清除成功"));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("清除过期场次失败: " + e.getMessage()));
        }
    }
    
    /**
     * 标记场次为已预订
     */
    @PutMapping("/{id}/book")
    public ResponseEntity<ApiResponse<Session>> markSessionAsBooked(@PathVariable Long id) {
        try {
            Session session = sessionService.markSessionAsBooked(id);
            return ResponseEntity.ok(ApiResponse.success(session));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("标记场次失败: " + e.getMessage()));
        }
    }
    
    /**
     * 标记场次为未预订
     */
    @PutMapping("/{id}/unbook")
    public ResponseEntity<ApiResponse<Session>> markSessionAsAvailable(@PathVariable Long id) {
        try {
            Session session = sessionService.markSessionAsAvailable(id);
            return ResponseEntity.ok(ApiResponse.success(session));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("标记场次失败: " + e.getMessage()));
        }
    }
} 