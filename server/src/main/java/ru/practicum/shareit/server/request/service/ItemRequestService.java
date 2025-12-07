package ru.practicum.shareit.server.request.service;

import ru.practicum.shareit.server.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {

    ItemRequestDto createRequest(ItemRequestDto requestDto, Long requestorId);

    List<ItemRequestDto> getUserRequests(Long requestorId);

    List<ItemRequestDto> getAllRequests(Long userId, int from, int size);

    ItemRequestDto getRequestById(Long requestId, Long userId);

}
