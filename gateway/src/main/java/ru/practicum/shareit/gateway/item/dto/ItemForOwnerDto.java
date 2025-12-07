package ru.practicum.shareit.gateway.item.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.gateway.booking.dto.BookingInfoDto;

@Data
@Builder
public class ItemForOwnerDto {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private BookingInfoDto lastBooking;
    private BookingInfoDto nextBooking;
}
