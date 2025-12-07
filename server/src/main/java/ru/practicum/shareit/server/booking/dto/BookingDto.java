package ru.practicum.shareit.server.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.server.booking.model.BookingStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDto {
    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private BookingStatus status;
    private BookerDto booker;
    private ItemDto item;
    private Long itemId;
    private Long bookerId;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BookerDto {
        private Long id;
        private String name;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ItemDto {
        private Long id;
        private String name;
    }
}