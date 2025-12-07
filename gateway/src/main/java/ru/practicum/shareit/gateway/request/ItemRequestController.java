package ru.practicum.shareit.gateway.request;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.gateway.request.dto.ItemRequestCreateDto;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
@Validated
@Slf4j
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<Object> createRequest(@RequestBody @Valid ItemRequestCreateDto requestDto,
                                                @RequestHeader(USER_ID_HEADER) Long requestorId) {
        log.info("Создание запроса на вещь: пользователь={}, описание='{}'",
                requestorId, requestDto.getDescription());
        return itemRequestClient.createRequest(requestDto, requestorId);
    }

    @GetMapping
    public ResponseEntity<Object> getUserRequests(@RequestHeader(USER_ID_HEADER) Long requestorId) {
        log.info("Получение запросов пользователя: ID={}", requestorId);
        return itemRequestClient.getUserRequests(requestorId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequests(@RequestHeader(USER_ID_HEADER) Long userId,
                                                 @RequestParam(defaultValue = "0") int from,
                                                 @RequestParam(defaultValue = "10") int size) {
        log.info("Получение всех запросов других пользователей: пользователь={}, from={}, size={}",
                userId, from, size);
        return itemRequestClient.getAllRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequestById(@PathVariable Long requestId,
                                                 @RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Получение запроса: ID={}, пользователь={}", requestId, userId);
        return itemRequestClient.getRequestById(requestId, userId);
    }
}