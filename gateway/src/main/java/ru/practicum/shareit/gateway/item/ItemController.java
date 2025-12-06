package ru.practicum.shareit.gateway.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.gateway.item.dto.CommentDto;
import ru.practicum.shareit.gateway.item.dto.ItemDto;

@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
@Validated
@Slf4j
public class ItemController {
    private final ItemClient itemClient;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<Object> createItem(@RequestBody @Valid ItemDto itemDto,
                                             @RequestHeader(USER_ID_HEADER) Long ownerId) {
        log.info("Создание вещи: владелец={}, название='{}'", ownerId, itemDto.getName());
        return itemClient.createItem(itemDto, ownerId);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@PathVariable Long itemId,
                                             @RequestBody ItemDto itemDto,
                                             @RequestHeader(USER_ID_HEADER) Long ownerId) {
        log.info("Обновление вещи: ID={}, владелец={}", itemId, ownerId);
        return itemClient.updateItem(itemId, itemDto, ownerId);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(@PathVariable Long itemId,
                                              @RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Получение вещи: ID={}, пользователь={}", itemId, userId);
        return itemClient.getItemById(itemId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getItemsByOwner(@RequestHeader(USER_ID_HEADER) Long ownerId) {
        log.info("Получение вещей владельца: ID={}", ownerId);
        return itemClient.getItemsByOwner(ownerId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(@RequestParam String text,
                                              @RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Поиск вещей: текст='{}', пользователь={}", text, userId);
        return itemClient.searchItems(text, userId);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@PathVariable Long itemId,
                                             @RequestHeader(USER_ID_HEADER) Long authorId,
                                             @RequestBody @Valid CommentDto commentDto) {
        log.info("Добавление комментария: вещь={}, автор={}", itemId, authorId);
        return itemClient.addComment(itemId, authorId, commentDto);
    }
}