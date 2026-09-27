package com.example.booking.service;

import com.example.booking.dto.ReservationDto;
import com.example.booking.dto.PageResponse;
import com.example.booking.model.Reservation.Status;

public interface ReservationService {
    ReservationDto createReservation(ReservationDto reservationDto, Long userId);
    PageResponse<ReservationDto> getUserReservations(Long userId, int page, int size, String sortBy, String sortDir);
    PageResponse<ReservationDto> getUserReservationsByStatus(Long userId, Status status, int page, int size, String sortBy, String sortDir);
    PageResponse<ReservationDto> getAllReservations(int page, int size, String sortBy, String sortDir);
    PageResponse<ReservationDto> getReservationsByStatus(Status status, int page, int size, String sortBy, String sortDir);
    ReservationDto updateReservationStatus(Long id, Status status);
    void cancelReservation(Long id);
}