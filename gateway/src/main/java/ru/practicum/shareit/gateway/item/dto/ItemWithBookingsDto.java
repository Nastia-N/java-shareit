package ru.practicum.shareit.gateway.item.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.gateway.booking.dto.BookingInfoDto;

import java.util.List;

@Data
@Builder
public class ItemWithBookingsDto {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private BookingInfoDto lastBooking;
    private BookingInfoDto nextBooking;
    private List<CommentDto> comments;
}
