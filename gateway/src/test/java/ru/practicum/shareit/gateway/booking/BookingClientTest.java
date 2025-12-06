package ru.practicum.shareit.gateway.booking;

import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import ru.practicum.shareit.gateway.booking.dto.BookingDto;
import ru.practicum.shareit.gateway.booking.dto.BookingState;

import static org.junit.jupiter.api.Assertions.assertThrows;

class BookingClientTest {

    @Test
    void allMethods_shouldBeCovered() {
        var client = new BookingClient("http://unreachable-host", new RestTemplateBuilder());
        BookingDto dto = BookingDto.builder().build();

        assertThrows(Exception.class, () -> client.getBookings(1L, BookingState.ALL, 0, 10));
        assertThrows(Exception.class, () -> client.getOwnerBookings(1L, BookingState.CURRENT, 5, 20));
        assertThrows(Exception.class, () -> client.bookItem(1L, dto));
        assertThrows(Exception.class, () -> client.approveBooking(1L, 100L, true));
        assertThrows(Exception.class, () -> client.approveBooking(1L, 100L, false));
        assertThrows(Exception.class, () -> client.getBooking(1L, 100L));
    }
}