package ru.practicum.shareit.server.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.server.request.dto.ItemRequestDto;
import ru.practicum.shareit.server.request.service.ItemRequestService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
@Slf4j
public class ItemRequestController {
    private final ItemRequestService itemRequestService;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public ItemRequestDto createRequest(@RequestBody ItemRequestDto requestDto,
                                        @RequestHeader(USER_ID_HEADER) Long requestorId) {
        log.info("Создание запроса на вещь: пользователь={}, описание='{}'",
                requestorId, requestDto.getDescription());
        return itemRequestService.createRequest(requestDto, requestorId);
    }

    @GetMapping
    public List<ItemRequestDto> getUserRequests(@RequestHeader(USER_ID_HEADER) Long requestorId) {
        log.info("Получение запросов пользователя: ID={}", requestorId);
        return itemRequestService.getUserRequests(requestorId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllRequests(@RequestHeader(USER_ID_HEADER) Long userId,
                                               @RequestParam(defaultValue = "0") int from,
                                               @RequestParam(defaultValue = "10") int size) {
        log.info("Получение всех запросов других пользователей: пользователь={}, from={}, size={}",
                userId, from, size);
        return itemRequestService.getAllRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getRequestById(@PathVariable Long requestId,
                                         @RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Получение запроса: ID={}, пользователь={}", requestId, userId);
        return itemRequestService.getRequestById(requestId, userId);
    }
}
