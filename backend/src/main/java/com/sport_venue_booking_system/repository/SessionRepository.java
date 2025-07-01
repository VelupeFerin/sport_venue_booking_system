package com.sport_venue_booking_system.repository;

import com.sport_venue_booking_system.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {
    
    /**
     * 根据场地名称查找场次
     */
    List<Session> findByCourtNameOrderByStartTime(String courtName);
    
    /**
     * 根据场地名称和开始时间查找场次
     */
    Session findByCourtNameAndStartTime(String courtName, LocalDateTime startTime);
    
    /**
     * 查找指定日期范围内的场次
     */
    List<Session> findByStartTimeBetweenOrderByStartTime(LocalDateTime start, LocalDateTime end);
    
    /**
     * 查找指定日期和场地名称的场次
     */
    @Query("SELECT s FROM Session s WHERE s.courtName = :courtName AND s.startTime >= :startOfDay AND s.startTime < :endOfDay ORDER BY s.startTime")
    List<Session> findByCourtNameAndDate(@Param("courtName") String courtName, 
                                        @Param("startOfDay") LocalDateTime startOfDay, 
                                        @Param("endOfDay") LocalDateTime endOfDay);
    
    /**
     * 查找可预订的场次（未预订且激活的）
     */
    List<Session> findByIsBookedFalseAndIsActiveTrueOrderByStartTime();
    
    /**
     * 查找指定日期范围内的可预订场次
     */
    List<Session> findByStartTimeBetweenAndIsBookedFalseAndIsActiveTrueOrderByStartTime(
            LocalDateTime start, LocalDateTime end);
    
    /**
     * 删除过期场次
     */
    @Modifying
    @Query("DELETE FROM Session s WHERE s.startTime < :currentTime")
    void deleteExpiredSessions(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * 检查指定场地和时间段是否存在场次
     */
    boolean existsByCourtNameAndStartTime(String courtName, LocalDateTime startTime);
    
    /**
     * 统计指定日期范围内的场次数量
     */
    @Query("SELECT COUNT(s) FROM Session s WHERE s.startTime BETWEEN :startTime AND :endTime")
    long countSessionsBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
} 