package com.example.booking.controller;

import com.example.booking.dto.ReservationDto;
import com.example.booking.dto.PageResponse;
import com.example.booking.model.Reservation;
import com.example.booking.model.Reservation.Status;
import com.example.booking.security.UserPrincipal;
import com.example.booking.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ReservationController {
    @Autowired
    private ReservationService reservationService;

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ReservationDto> createReservation(
            @Valid @RequestBody ReservationDto reservationDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = ((UserPrincipal) userDetails).getId();
        ReservationDto createdReservation = reservationService.createReservation(reservationDto, userId);
        return ResponseEntity.ok(createdReservation);
    }

    @GetMapping("/my-reservations")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<PageResponse<ReservationDto>> getMyReservations(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startTime") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Long userId = ((UserPrincipal) userDetails).getId();
        PageResponse<ReservationDto> reservations = reservationService.getUserReservations(userId, page, size, sortBy, sortDir);
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/my-reservations/status/{status}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<PageResponse<ReservationDto>> getMyReservationsByStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Status status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startTime") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Long userId = ((UserPrincipal) userDetails).getId();
        PageResponse<ReservationDto> reservations = reservationService.getUserReservationsByStatus(userId, status, page, size, sortBy, sortDir);
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<ReservationDto>> getAllReservations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startTime") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        PageResponse<ReservationDto> reservations = reservationService.getAllReservations(page, size, sortBy, sortDir);
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<ReservationDto>> getReservationsByStatus(
            @PathVariable Status status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startTime") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        PageResponse<ReservationDto> reservations = reservationService.getReservationsByStatus(status, page, size, sortBy, sortDir);
        return ResponseEntity.ok(reservations);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReservationDto> updateReservationStatus(
            @PathVariable Long id,
            @RequestParam Status status) {
        
        ReservationDto updatedReservation = reservationService.updateReservationStatus(id, status);
        return ResponseEntity.ok(updatedReservation);
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Void> cancelReservation(@PathVariable Long id) {
        reservationService.cancelReservation(id);
        return ResponseEntity.noContent().build();
    }
}