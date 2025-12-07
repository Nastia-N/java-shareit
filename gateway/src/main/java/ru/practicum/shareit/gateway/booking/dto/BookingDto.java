package ru.practicum.shareit.gateway.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDto {
    private Long id;

    @NotNull(message = "Дата начала не может быть пустой")
    @FutureOrPresent(message = "Дата начала должна быть в будущем или настоящем")
    private LocalDateTime start;

    @NotNull(message = "Дата окончания не может быть пустой")
    @Future(message = "Дата окончания должна быть в будущем")
    private LocalDateTime end;

    private BookingStatus status;
    private BookerDto booker;
    private ItemDto item;

    @NotNull(message = "ID предмета не может быть пустым")
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
