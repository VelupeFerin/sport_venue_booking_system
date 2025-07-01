package com.sport_venue_booking_system.repository;

import com.sport_venue_booking_system.entity.OrderSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderSessionRepository extends JpaRepository<OrderSession, Long> {
    List<OrderSession> findByOrderId(Long orderId);
} 