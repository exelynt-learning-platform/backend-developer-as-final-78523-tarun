package com.example.booking.repository;

import com.example.booking.model.Reservation;
import com.example.booking.model.Reservation.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Page<Reservation> findByUserId(Long userId, Pageable pageable);
    
    Page<Reservation> findByUserIdAndStatus(Long userId, Status status, Pageable pageable);
    
    Page<Reservation> findByStatus(Status status, Pageable pageable);
    
    @Query("SELECT r FROM Reservation r WHERE r.resource.id = :resourceId AND " +
           "r.status IN :statuses AND r.endTime > :currentTime ORDER BY r.startTime ASC")
    List<Reservation> findUpcomingReservationsByResource(
        @Param("resourceId") Long resourceId,
        @Param("statuses") List<Status> statuses,
        @Param("currentTime") LocalDateTime currentTime
    );
    
    boolean existsByResourceIdAndStartTimeLessThanEqualAndEndTimeGreaterThan(
        Long resourceId, LocalDateTime startTime, LocalDateTime endTime
    );
}