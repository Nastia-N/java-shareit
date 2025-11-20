package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

public interface BookingService {

    BookingDto createBooking(BookingDto bookingDto, Long bookerId);

    BookingDto approveBooking(Long bookingId, Long ownerId, boolean approved);

    BookingDto getBookingById(Long bookingId, Long userId);

    List<BookingDto> getUserBookings(Long userId, BookingState state, String sortBy, String direction);

    List<BookingDto> getOwnerBookings(Long ownerId, BookingState state, String sortBy, String direction);

}
