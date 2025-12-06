package ru.practicum.shareit.server.booking.service;

import ru.practicum.shareit.server.booking.dto.BookingDto;
import ru.practicum.shareit.server.booking.model.BookingState;

import java.util.List;

public interface BookingService {

    BookingDto createBooking(BookingDto bookingDto, Long bookerId);

    BookingDto approveBooking(Long bookingId, Long ownerId, boolean approved);

    BookingDto getBookingById(Long bookingId, Long userId);

    List<BookingDto> getUserBookings(Long userId, BookingState state, String sortBy, String direction);

    List<BookingDto> getOwnerBookings(Long ownerId, BookingState state, String sortBy, String direction);

}