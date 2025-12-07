package ru.practicum.shareit.server.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.server.booking.dto.BookingDto;
import ru.practicum.shareit.server.booking.model.BookingState;
import ru.practicum.shareit.server.booking.service.BookingService;

import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {
    private final BookingService bookingService;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public BookingDto createBooking(@RequestBody BookingDto bookingDto,
                                    @RequestHeader(USER_ID_HEADER) Long bookerId) {
        log.info("Создание бронирования: пользователь={}, вещь={}", bookerId, bookingDto.getItemId());
        return bookingService.createBooking(bookingDto, bookerId);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approveBooking(@PathVariable Long bookingId,
                                     @RequestHeader(USER_ID_HEADER) Long ownerId,
                                     @RequestParam Boolean approved) {
        log.info("{} бронирования {} владельцем {}",
                approved ? "Подтверждение" : "Отклонение", bookingId, ownerId);
        return bookingService.approveBooking(bookingId, ownerId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBookingById(@PathVariable Long bookingId,
                                     @RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Получение бронирования {} пользователем {}", bookingId, userId);
        return bookingService.getBookingById(bookingId, userId);
    }

    @GetMapping
    public List<BookingDto> getUserBookings(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @RequestParam(defaultValue = "ALL") BookingState state,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {
        log.info("Получение бронирований пользователя {}: состояние={}, from={}, size={}",
                userId, state, from, size);
        return bookingService.getUserBookings(userId, state, "start", "DESC");
    }

    @GetMapping("/owner")
    public List<BookingDto> getOwnerBookings(
            @RequestHeader(USER_ID_HEADER) Long ownerId,
            @RequestParam(defaultValue = "ALL") BookingState state,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {
        log.info("Получение бронирований владельца {}: состояние={}, from={}, size={}",
                ownerId, state, from, size);
        return bookingService.getOwnerBookings(ownerId, state, "start", "DESC");
    }
}
