package com.example.booking.service;

import com.example.booking.dto.ReservationDto;
import com.example.booking.dto.PageResponse;
import com.example.booking.model.Reservation;
import com.example.booking.model.Reservation.Status;
import com.example.booking.model.Resource;
import com.example.booking.model.User;
import com.example.booking.repository.ReservationRepository;
import com.example.booking.repository.ResourceRepository;
import com.example.booking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReservationServiceImpl implements ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public ReservationDto createReservation(ReservationDto reservationDto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Resource resource = resourceRepository.findById(reservationDto.getResourceId())
                .orElseThrow(() -> new RuntimeException("Resource not found"));

        if (!resource.getIsAvailable()) {
            throw new RuntimeException("Resource is not available");
        }

        if (reservationDto.getStartTime().isAfter(reservationDto.getEndTime())) {
            throw new RuntimeException("Start time must be before end time");
        }

        List<Status> activeStatuses = List.of(Status.PENDING, Status.CONFIRMED);
        List<Reservation> conflictingReservations = reservationRepository
                .findUpcomingReservationsByResource(resource.getId(), activeStatuses, LocalDateTime.now());

        for (Reservation existing : conflictingReservations) {
            if (reservationDto.getStartTime().isBefore(existing.getEndTime()) &&
                reservationDto.getEndTime().isAfter(existing.getStartTime())) {
                throw new RuntimeException("Resource is already reserved for this time slot");
            }
        }

        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setResource(resource);
        reservation.setStartTime(reservationDto.getStartTime());
        reservation.setEndTime(reservationDto.getEndTime());
        reservation.setTotalPrice(reservationDto.getTotalPrice());

        Reservation saved = reservationRepository.save(reservation);
        return toDto(saved);
    }

    @Override
    public PageResponse<ReservationDto> getUserReservations(Long userId, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Reservation> reservations = reservationRepository.findByUserId(userId, pageable);
        return toPageResponse(reservations);
    }

    @Override
    public PageResponse<ReservationDto> getUserReservationsByStatus(Long userId, Status status, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Reservation> reservations = reservationRepository.findByUserIdAndStatus(userId, status, pageable);
        return toPageResponse(reservations);
    }

    @Override
    public PageResponse<ReservationDto> getAllReservations(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Reservation> reservations = reservationRepository.findAll(pageable);
        return toPageResponse(reservations);
    }

    @Override
    public PageResponse<ReservationDto> getReservationsByStatus(Status status, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Reservation> reservations = reservationRepository.findByStatus(status, pageable);
        return toPageResponse(reservations);
    }

    @Override
    public ReservationDto updateReservationStatus(Long id, Status status) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));
        reservation.setStatus(status);
        Reservation updated = reservationRepository.save(reservation);
        return toDto(updated);
    }

    @Override
    public void cancelReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));
        reservation.setStatus(Status.CANCELLED);
        reservationRepository.save(reservation);
    }

    private ReservationDto toDto(Reservation r) {
        ReservationDto dto = new ReservationDto();
        dto.setId(r.getId());
        dto.setResourceId(r.getResource().getId());
        dto.setStartTime(r.getStartTime());
        dto.setEndTime(r.getEndTime());
        dto.setTotalPrice(r.getTotalPrice());
        dto.setStatus(r.getStatus());
        return dto;
    }

    private PageResponse<ReservationDto> toPageResponse(Page<Reservation> page) {
        List<ReservationDto> content = page.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.isEmpty()
        );
    }
}