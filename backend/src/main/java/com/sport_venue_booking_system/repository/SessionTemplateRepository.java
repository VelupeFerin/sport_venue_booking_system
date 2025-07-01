package com.sport_venue_booking_system.repository;

import com.sport_venue_booking_system.entity.SessionTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionTemplateRepository extends JpaRepository<SessionTemplate, Long> {
    
    List<SessionTemplate> findByCourtNameOrderByStartTime(String courtName);
    
    List<SessionTemplate> findByIsActiveTrue();
    
    Optional<SessionTemplate> findByCourtNameAndStartTime(String courtName, java.time.LocalTime startTime);
    
    boolean existsByCourtNameAndStartTime(String courtName, java.time.LocalTime startTime);
} 