package ru.practicum.shareit.gateway.booking;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.gateway.booking.dto.BookingDto;
import ru.practicum.shareit.gateway.booking.dto.BookingState;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
    private final BookingClient bookingClient;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<Object> createBooking(
            @RequestHeader(USER_ID_HEADER) long userId,
            @RequestBody @Valid BookingDto bookingDto) {
        log.info("Создание бронирования: пользователь={}, вещь={}", userId, bookingDto.getItemId());
        return bookingClient.bookItem(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approveBooking(
            @RequestHeader(USER_ID_HEADER) long userId,
            @PathVariable Long bookingId,
            @RequestParam Boolean approved) {
        log.info("{} бронирования {} пользователем {}",
                approved ? "Подтверждение" : "Отклонение", bookingId, userId);
        return bookingClient.approveBooking(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBookingById(
            @RequestHeader(USER_ID_HEADER) long userId,
            @PathVariable Long bookingId) {
        log.info("Получение бронирования {} пользователем {}", bookingId, userId);
        return bookingClient.getBooking(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getUserBookings(
            @RequestHeader(USER_ID_HEADER) long userId,
            @RequestParam(name = "state", defaultValue = "ALL") String stateParam,
            @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
            @Positive @RequestParam(defaultValue = "10") Integer size) {
        BookingState state = BookingState.from(stateParam);
        log.info("Получение бронирований пользователя {}: состояние={}, from={}, size={}",
                userId, stateParam, from, size);
        return bookingClient.getBookings(userId, state, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getOwnerBookings(
            @RequestHeader(USER_ID_HEADER) long ownerId,
            @RequestParam(name = "state", defaultValue = "ALL") String stateParam,
            @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
            @Positive @RequestParam(defaultValue = "10") Integer size) {
        BookingState state = BookingState.from(stateParam);
        log.info("Получение бронирований владельца {}: состояние={}, from={}, size={}",
                ownerId, stateParam, from, size);
        return bookingClient.getOwnerBookings(ownerId, state, from, size);
    }
}