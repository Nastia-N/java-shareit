package ru.practicum.shareit.gateway.request.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.gateway.item.dto.ItemDto;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ItemRequestDto {
    private Long id;
    private String description;
    private Long requestorId;
    private LocalDateTime created;
    private List<ItemDto> items;
}
